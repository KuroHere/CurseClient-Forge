package com.curseclient.client.module.modules.movement

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.PacketEvent
import com.curseclient.client.event.events.PlayerPacketEvent
import com.curseclient.client.event.events.TimerEvent
import com.curseclient.client.event.events.TravelEvent
import com.curseclient.client.event.listener.Nameable
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.managers.HotbarManager.sendSlotPacket
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.entity.calcIsInWater
import com.curseclient.client.utility.extension.entity.speed
import com.curseclient.client.utility.extension.settingName
import com.curseclient.client.utility.items.firstItem
import com.curseclient.client.utility.items.hotbarSlots
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.player.MovementUtils.calcMoveYaw
import com.curseclient.client.utility.player.MovementUtils.isInputting
import com.curseclient.client.utility.player.MovementUtils.verticalMovement
import com.curseclient.client.utility.player.PacketUtils.send
import com.curseclient.client.utility.player.RotationUtils.calcAngleDiff
import com.curseclient.client.utility.player.RotationUtils.normalizeAngle
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.util.EnumHand
import net.minecraft.util.math.Vec2f
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object ElytraFlight : Module(
    "ElytraFlight",
    "Allows to infinite fly using elytra",
    Category.MOVEMENT
) {
    private val page by setting("Page", Page.GENERAL)
    private val mode by setting("Mode", Mode.CONTROL)

    // general page
    private val swingSpeed by setting("Swing Speed", 1.0, 0.0, 2.0, 0.1, { page == Page.GENERAL })
    private val swingAmount by setting("Swing Amount", 0.8, 0.0, 2.0, 0.1, { page == Page.GENERAL })
    private val acceleration by setting("Acceleration", false, { page == Page.GENERAL && listOf(Mode.CONTROL, Mode.MATRIX_FIREWORK).contains(mode) })
    private val accelerateStartSpeed by setting("Start Speed", 0.5, 0.0, 1.0, 0.02, { page == Page.GENERAL && listOf(Mode.CONTROL, Mode.MATRIX_FIREWORK).contains(mode) && acceleration })
    private val accelerateTime by setting("Accelerate Time", 1.0, 0.0, 30.0, 0.02, { page == Page.GENERAL && listOf(Mode.CONTROL, Mode.MATRIX_FIREWORK).contains(mode) && acceleration })

    // Control mode
    private val speedControl by setting("Speed C", 2.0, 1.0, 10.0, 0.01, { page == Page.MODE_SETTINGS && mode == Mode.CONTROL })
    private val forwardPitchControl by setting("Forward Pitch C", -25.0, -90.0, 90.0, 1.0, { page == Page.MODE_SETTINGS && mode == Mode.CONTROL })
    private val upPitchControl by setting("Up Pitch C", 75.0, 15.0, 90.0, 1.0, { page == Page.MODE_SETTINGS && mode == Mode.CONTROL })
    private val downPitchControl by setting("Down Pitch C", 90.0, 15.0, 90.0, 1.0, { page == Page.MODE_SETTINGS && mode == Mode.CONTROL })
    private val yawSpeed by setting("Yaw Speed", 30.0, 5.0, 180.0, 5.0, { page == Page.MODE_SETTINGS && mode == Mode.CONTROL })
    private val pitchSpeed by setting("Pitch Speed", 10.0, 1.0, 90.0, 1.0, { page == Page.MODE_SETTINGS && mode == Mode.CONTROL })

    // Matrix firework
    private val speedHMatrixFirework by setting("Speed H MF", 1.5, 1.0, 3.0, 0.01, { page == Page.MODE_SETTINGS && mode == Mode.MATRIX_FIREWORK })
    private val speedVMatrixFirework by setting("Speed V MF", 0.2, 0.0, 3.0, 0.05, { page == Page.MODE_SETTINGS && mode == Mode.MATRIX_FIREWORK })
    private val glideSpeedMatrixFirework by setting("Glide Speed MF", 0.2, 0.0, 3.0, 0.05, { page == Page.MODE_SETTINGS && mode == Mode.MATRIX_FIREWORK })
    private val boostDelayMatrixFirework by setting("Boost Delay MF", 1.5, 0.5, 3.0, 0.25, { page == Page.MODE_SETTINGS && mode == Mode.MATRIX_FIREWORK })

    // takeoff page
    private val autoTakeoff by setting("Auto Takeoff", true, { page == Page.TAKEOFF })
    private val takeoffJump by setting("Takeoff Jump", true, { page == Page.TAKEOFF })
    private val takeoffFallDistance by setting("Takeoff Fall Distance", 0.1, 0.05, 0.4, 0.05, { page == Page.TAKEOFF && autoTakeoff })
    private val takeoffTimer by setting("Takeoff Timer", 0.1, 0.05, 1.0, 0.05, { page == Page.TAKEOFF && autoTakeoff })
    private val takeoffGlide by setting("Takeoff Glide", true, { page == Page.TAKEOFF && autoTakeoff })
    private val takeoffGlideAmount by setting("Takeoff Glide Amount", 0.0, 0.0, 0.1, 0.01, { page == Page.TAKEOFF && autoTakeoff && takeoffGlide })

    private var rotation: Vec2f? = null
    private val SafeClientEvent.dir get() = Math.toRadians(calcMoveYaw(yawIn = rotation?.x ?: player.rotationYaw))

    private var lastStandingTime = 0L
    private var lastBoostTime = 0L
    private var timer = 1.0

    override fun getHudInfo() = mode.settingName

    private enum class Page(override val displayName: String): Nameable {
        GENERAL("General"),
        MODE_SETTINGS("Mode Settings"),
        TAKEOFF("Takeoff")
    }

    private enum class Mode(override val displayName: String, val getSpeed: () -> Double, val move: SafeClientEvent.() -> Unit): Nameable {
        CONTROL("Control", { speedControl }, { controlMode() }),
        MATRIX_FIREWORK("Matrix Firework", { speedHMatrixFirework }, { matrixFireworkMode() })
    }

    init {
        safeListener<TimerEvent>(-99) {
            if (timer != 1.0) it.speed = timer
        }

        safeListener<PlayerPacketEvent.Data> { event ->
            rotation?.let {
                event.yaw = it.x
                event.pitch = it.y
            }
        }

        safeListener<PacketEvent.Receive> { event ->
            (event.packet as? SPacketSoundEffect)?.let { packet ->
                if (mode == Mode.MATRIX_FIREWORK &&
                    player.isElytraFlying &&
                    packet.sound == SoundEvents.ENTITY_FIREWORK_LAUNCH
                ) event.cancel()
            }
        }

        safeListener<TravelEvent> { event ->
            run {
                if (!checkElytra()) return@run

                if (player.isElytraFlying) {
                    timer = 1.0
                    event.cancel()

                    if (rotation == null)
                        rotation = Vec2f(player.rotationYaw, player.rotationPitch)

                    modifyLegSwing()
                    mode.move(this)

                    return@safeListener
                } else if (autoTakeoff) {
                    if (player.calcIsInWater() ||
                        player.isInLava)
                        return@run

                    if (player.onGround) {
                        if (takeoffJump) player.jump()
                        return@run
                    }

                    if (player.fallDistance < takeoffFallDistance)
                        return@run

                    rotation = null
                    lastStandingTime = System.currentTimeMillis()

                    event.cancel()
                    timer = takeoffTimer

                    player.motionX = 0.0
                    player.motionZ = 0.0

                    if (takeoffGlide) player.motionY = -takeoffGlideAmount

                    CPacketEntityAction(player, CPacketEntityAction.Action.START_FALL_FLYING).send()
                    return@safeListener
                }
            }

            lastStandingTime = System.currentTimeMillis()
            rotation = null
            timer = 1.0
        }
    }

    // region Movement
    private fun SafeClientEvent.controlMode() {
        val vDir = verticalMovement

        if (isInputting() || vDir != 0)
            rotation = rotate(
                rotation!!,
                Vec2f(
                    player.rotationYaw,
                    when(vDir) {
                        1 -> -upPitchControl
                        0 -> forwardPitchControl
                        else -> downPitchControl
                    }.toFloat()
                ))

        if (vDir == 0)
            rotation = Vec2f(rotation!!.x, forwardPitchControl.toFloat())

        player.motionY = 0.0

        if (!isInputting()) {
            lastStandingTime = System.currentTimeMillis()
            player.motionX = 0.0
            player.motionZ = 0.0
            return
        }

        val speed = getSpeed()
        player.motionX = -sin(dir) * speed
        player.motionZ = cos(dir) * speed
    }

    private fun SafeClientEvent.matrixFireworkMode() {
        rotation = Vec2f(player.rotationYaw, -40f * verticalMovement.toDouble().toFloat())

        if (System.currentTimeMillis() - lastBoostTime > boostDelayMatrixFirework * 1000) {
            player.hotbarSlots.firstItem(Items.FIREWORKS)?.let {
                sendSlotPacket(it.hotbarSlot)
                CPacketPlayerTryUseItem(EnumHand.MAIN_HAND).send()
                sendSlotPacket(player.inventory.currentItem)

                lastBoostTime = System.currentTimeMillis()
            }
        }

        val isBoosting = System.currentTimeMillis() - lastBoostTime <= (boostDelayMatrixFirework + 0.1) * 1000

        if (verticalMovement != 0 && isBoosting)
            player.motionY = verticalMovement.toDouble() * speedVMatrixFirework
        else
            player.motionY = -glideSpeedMatrixFirework / 10.0

        val speed = getSpeed()

        if (isInputting() && isBoosting) {
            player.motionX = -sin(dir) * speed
            player.motionZ = cos(dir) * speed
        } else {
            lastStandingTime = System.currentTimeMillis()
            player.motionX = 0.0
            player.motionZ = 0.0
        }
    }

    private fun getSpeed(): Double {
        val startSpeed = mode.getSpeed() * accelerateStartSpeed

        val timeExisted = (System.currentTimeMillis() - lastStandingTime) / 1000.0
        var speedPercentage = (timeExisted / accelerateTime).coerceIn(0.0, 1.0)
        if (!acceleration) speedPercentage = 1.0

        return lerp(startSpeed, mode.getSpeed(), speedPercentage)
    }

    // endregion

    // region Extra
    private fun SafeClientEvent.checkElytra(): Boolean {
        val slot = player.inventory.armorInventory[2]
        val dura = slot.maxDamage - slot.itemDamage
        return slot.item == Items.ELYTRA && dura > 1
    }

    private fun SafeClientEvent.modifyLegSwing() {
        player.prevLimbSwingAmount = player.limbSwingAmount
        player.limbSwing += swingSpeed.toFloat()
        val speedRatio = (player.speed / mode.getSpeed()).toFloat().coerceIn(0f, 1f)
        player.limbSwingAmount += ((speedRatio * swingAmount.toFloat()) - player.limbSwingAmount) * 0.4f
    }

    private fun rotate(from: Vec2f, to: Vec2f): Vec2f {
        val yawIn = from.x
        val pitchIn = from.y

        val yawDiff = calcAngleDiff(to.x, yawIn)
        val pitchDiff = calcAngleDiff(to.y, pitchIn)

        val yawTo = if (abs(yawDiff) <= yawSpeed) to.x
        else normalizeAngle(yawIn + yawDiff.coerceIn((-yawSpeed).toFloat(), yawSpeed.toFloat()))

        val pitchTo = if (abs(pitchDiff) <= pitchSpeed) to.y
        else normalizeAngle(pitchIn + pitchDiff.coerceIn((-pitchSpeed).toFloat(), pitchSpeed.toFloat()))

        return Vec2f(yawTo, pitchTo)
    }

    override fun onEnable() {
        lastStandingTime = System.currentTimeMillis()
        rotation = null
        timer = 1.0
    }
    // endregion
}