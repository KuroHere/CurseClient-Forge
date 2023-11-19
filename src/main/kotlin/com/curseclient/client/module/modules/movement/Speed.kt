package com.curseclient.client.module.modules.movement

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.*
import com.curseclient.client.event.listener.Nameable
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.entity.calcIsInWater
import com.curseclient.client.utility.extension.entity.speed
import com.curseclient.client.utility.extension.mixins.jumpTicks
import com.curseclient.client.utility.extension.settingName
import com.curseclient.client.utility.items.*
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.math.MathUtils.toInt
import com.curseclient.client.utility.player.MovementUtils.calcMoveRad
import com.curseclient.client.utility.player.MovementUtils.isInputting
import com.curseclient.client.utility.player.PacketUtils.send
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.*

object Speed: Module(
    "Speed",
    "Move faster",
    Category.MOVEMENT
) {
    private val mode by setting("Mode", Mode.NCP)

    // NCP
    private val ncpBaseSpeed by setting("Base Speed", 0.2873, 0.1, 0.3, 0.0001, { mode == Mode.NCP })
    private val ncpMaxSpeed by setting("Max Speed", 1.0, 0.3, 1.0, 0.0001, { mode == Mode.NCP })
    private val ncpSpeedAcceleration by setting("Speed Acceleration", 0.9937, 0.98, 1.0, 0.0001, { mode == Mode.NCP })
    private val ncpJumpSpeed by setting("Jump Speed", 0.3, 0.0, 0.5, 0.001, { mode == Mode.NCP })
    private val ncpJumpPostSpeed by setting("Jump Post Speed", 0.589, 0.1, 1.0, 0.0001, { mode == Mode.NCP })
    private val ncpJumpHeight by setting("Jump Height", 0.4, 0.1, 0.5, 0.00001, { mode == Mode.NCP })
    private val ncpResetOnJump by setting("Reset On Jump", true, { mode == Mode.NCP })
    private val ncpTimerBoost by setting("Timer Boost", 1.09, 1.0, 1.1, 0.01, { mode == Mode.NCP })

    // YPort
    private val yPortSpeed by setting("YPort Speed", 0.34, 0.27, 1.0, 0.01, { mode == Mode.YPort })
    private val yPortHeight by setting("YPort Height", 0.42, 0.01, 0.42, 0.01, { mode == Mode.YPort })

    // Matrix Elytra
    private val matrixElytraSpeed by setting("Elytra Speed", 0.5, 0.1, 1.0, 0.01, { mode == Mode.MatrixElytra })
    private val matrixElytraExtraBoost by setting("Extra Boost", 0.0, 0.0, 1.0, 0.01, { mode == Mode.MatrixElytra })

    //private val autoJump by setting("AutoJump", true)
    // NCP
    private var ncpPhase = NCPPhase.JUMP
    private var ncpSpeed = 0.2873
    private var lastDistance = 0.0

    // Matrix
    private var packetSprinting = false
    private var shouldBoost = false

    // Matrix Elytra
    private var airTicks = 0
    private var disablerTick = 0

    override fun getHudInfo() = mode.settingName

    private enum class Mode(override val displayName: String) : Nameable {
        NCP("NCP"),
        Glide("NCP Glide"),
        YPort("NCP YPort"),
        MatrixStrafe1("Matrix Strafe 1"),
        MatrixStrafe2("Matrix Strafe 2"),
        MatrixBoost("Matrix Boost"),
        MatrixElytra("Matrix Elytra")
    }

    private enum class NCPPhase {
        JUMP,
        JUMP_POST,
        SLOWDOWN
    }

    init {
        safeListener<TimerEvent> {
            if (!shouldWork() || !isInputting()) return@safeListener
            if (mode != Mode.NCP) return@safeListener

            it.speed = ncpTimerBoost
        }

        safeListener<MoveEvent> { event ->
            run {
                when(mode) {
                    Mode.NCP -> {
                        if (!shouldWork()) return@run
                        if (player.onGround && player.movementInput.jump)
                            ncpPhase = NCPPhase.JUMP

                        ncpPhase = when (ncpPhase) {
                            NCPPhase.JUMP -> {
                                if (player.onGround) {
                                    if (ncpResetOnJump) ncpSpeed = ncpBaseSpeed

                                    event.y = ncpJumpHeight
                                    ncpSpeed += ncpJumpSpeed
                                    NCPPhase.JUMP_POST
                                } else {
                                    NCPPhase.SLOWDOWN
                                }
                            }

                            NCPPhase.JUMP_POST -> {
                                ncpSpeed *= ncpJumpPostSpeed
                                NCPPhase.SLOWDOWN
                            }

                            NCPPhase.SLOWDOWN -> {
                                ncpSpeed = lastDistance * ncpSpeedAcceleration
                                NCPPhase.SLOWDOWN
                            }
                        }

                        if (player.onGround && !player.movementInput.jump)
                            ncpSpeed = ncpBaseSpeed

                        ncpSpeed = clamp(ncpSpeed, ncpBaseSpeed, ncpMaxSpeed)

                        val moveSpeed = if (isInputting()) ncpSpeed else {
                            ncpSpeed = ncpBaseSpeed

                            0.0
                        }

                        val dir = calcMoveRad()
                        event.x = -sin(dir) * moveSpeed
                        event.z = cos(dir) * moveSpeed

                        return@safeListener
                    }

                    Mode.Glide -> {
                        if (!shouldWork()) return@run

                        if (player.onGround && !mc.gameSettings.keyBindJump.isKeyDown) player.jump()

                        if (!isInputting()) return@run
                        val bb = player.entityBoundingBox.shrink(0.0625).expand(0.0, -0.55, 0.0)
                        if (world.collidesWithAnyBlock(bb) && player.motionY < 0.0) player.motionY = -0.00000001


                        val dir = calcMoveRad()

                        if (player.speed <= 0.2873) {
                            player.motionX = -sin(dir) * 0.2873
                            player.motionZ = cos(dir) * 0.2873
                        }

                        player.motionX = -sin(dir) * player.speed
                        player.motionZ = cos(dir) * player.speed
                    }

                    Mode.YPort -> {
                        if (!shouldWork()) return@run
                        val dir = calcMoveRad()

                        if (player.onGround && isInputting()) {
                            player.motionY = yPortHeight

                            player.motionX = -sin(dir) * yPortSpeed
                            player.motionZ = cos(dir) * yPortSpeed
                            return@run
                        }

                        if (player.fallDistance > yPortHeight + 0.1) return@run
                        player.motionY = -yPortHeight - 0.1
                    }

                    Mode.MatrixBoost -> {
                        if (!shouldWork()) return@run
                        if (!isInputting()) return@run

                        player.isSprinting = true

                        val dir = calcMoveRad()
                        val speed = player.speed * 0.99999 // matrix moment
                        player.motionX = -sin(dir) * speed
                        player.motionZ = cos(dir) * speed

                        if (player.motionY in 0.41..0.43 && player.movementInput.jump)
                            shouldBoost = true

                        if (player.motionY < 0.05 || speed < 0.2)
                            shouldBoost = false

                        return@safeListener
                    }

                    Mode.MatrixStrafe1 -> {
                        if (!shouldWork()) return@run
                        if (!isInputting()) return@run

                        player.isSprinting = true

                        val speed = player.speed
                        if (speed > 0.215) return@run

                        val dir = calcMoveRad()
                        player.motionX = -sin(dir) * speed
                        player.motionZ = cos(dir) * speed
                    }

                    Mode.MatrixStrafe2 -> {
                        if (!shouldWork()) return@run
                        if (!isInputting()) return@run

                        val dir = calcMoveRad()
                        val speed = player.speed * 0.99999 // matrix moment
                        player.motionX = -sin(dir) * speed
                        player.motionZ = cos(dir) * speed
                    }

                    Mode.MatrixElytra -> {
                        if (!isInputting()) return@run

                        val dir = calcMoveRad()

                        val strafeSpeed = player.speed * 0.99999 // matrix moment
                        player.motionX = -sin(dir) * strafeSpeed
                        player.motionZ = cos(dir) * strafeSpeed

                        airTicks = (airTicks + 1) * (!player.onGround).toInt()

                        if (airTicks == 6) {
                            player.inventory.firstEmpty()?.let { empty ->
                                if (ArmorSlot.CHESTPLATE.slot.stack.item != Items.ELYTRA) return@let

                                ArmorSlot.CHESTPLATE.click(1, ClickType.PICKUP)
                                empty.click(1, ClickType.PICKUP)
                            }

                            player.inventory.firstStack(Items.ELYTRA)?.let { elytra ->
                                elytra.click(1, ClickType.PICKUP)
                                ArmorSlot.CHESTPLATE.click(1, ClickType.PICKUP)

                                CPacketEntityAction(player, CPacketEntityAction.Action.START_FALL_FLYING).send()
                                CPacketEntityAction(player, CPacketEntityAction.Action.START_FALL_FLYING).send()

                                ArmorSlot.CHESTPLATE.click(1, ClickType.PICKUP)
                                elytra.click(1, ClickType.PICKUP)

                                disablerTick = player.ticksExisted
                            }

                            val speed = matrixElytraExtraBoost * 1.96
                            if (speed > player.speed && player.ticksExisted - disablerTick < 10) {
                                player.motionX = -sin(dir) * speed
                                player.motionZ = cos(dir) * speed
                            }
                        }

                        if (airTicks == 1 && player.ticksExisted - disablerTick < 10) {
                            player.motionX = -sin(dir) * matrixElytraSpeed * 1.96
                            player.motionZ = cos(dir) * matrixElytraSpeed * 1.96
                        }
                    }
                }
            }

            ncpSpeed = ncpBaseSpeed
            shouldBoost = false
        }

        safeListener<TickEvent.ClientTickEvent> {
            lastDistance = hypot(player.posX - player.prevPosX, player.posZ - player.prevPosZ)

            if (!shouldWork()) return@safeListener
            if (!listOf(Mode.MatrixStrafe2, Mode.MatrixBoost, Mode.MatrixElytra).contains(mode)) return@safeListener

            player.isSprinting = isInputting() // avoid fov shaking
        }

        safeListener<JumpEvent> {
            if (!shouldWork()) return@safeListener

            val dir = calcMoveRad()
            val dir1 = Math.toRadians(player.rotationYaw.toDouble())

            when(mode) {
                Mode.MatrixStrafe1, Mode.MatrixStrafe2, Mode.MatrixBoost, Mode.MatrixElytra -> {
                    if (!isInputting()) return@safeListener

                    if (player.isSprinting) {
                        player.motionX += sin(dir1) * 0.2
                        player.motionZ -= cos(dir1) * 0.2
                    }

                    player.motionX -= sin(dir) * 0.2
                    player.motionZ += cos(dir) * 0.2
                }

                else -> {}
            }

            if (mode == Mode.MatrixBoost && isInputting()) player.jumpTicks = 0
        }

        safeListener<PlayerPacketEvent.Misc> {
            if (!shouldWork()) return@safeListener
            if (!listOf(Mode.MatrixStrafe2, Mode.MatrixBoost, Mode.MatrixElytra).contains(mode)) return@safeListener

            it.isSprinting = packetSprinting
            packetSprinting = !packetSprinting
        }

        safeListener<PlayerPacketEvent.Data> {
            when(mode) {
                Mode.MatrixBoost -> {
                    val bb = player.entityBoundingBox
                        .offset(0.0, -1.0, 0.0)
                        .shrink(0.0625)

                    if (world.getCollisionBoxes(player, bb).isNotEmpty() && shouldBoost) {
                        player.jumpTicks = 0
                        player.fallDistance = 0f
                        it.onGround = true
                        player.onGround = true
                    }
                }

                else -> {}
            }
        }
    }

    private fun SafeClientEvent.shouldWork(): Boolean {
        return !player.capabilities.isFlying &&
            !player.isElytraFlying &&
            !mc.gameSettings.keyBindSneak.isKeyDown &&
            !player.calcIsInWater() &&
            !player.isInLava
    }

    override fun onEnable() {
        ncpPhase = NCPPhase.SLOWDOWN
        ncpSpeed = ncpBaseSpeed
    }

    override fun onDisable() {
        airTicks = 0
        disablerTick = 0
    }
}