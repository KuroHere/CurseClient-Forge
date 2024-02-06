package com.curseclient.client.module.impls.movement

import com.curseclient.client.event.events.MoveEvent
import com.curseclient.client.event.events.PlayerPacketEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.managers.PacketManager
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.entity.isInsideBlock
import com.curseclient.client.utility.extension.mixins.jumpTicks
import com.curseclient.client.utility.extension.settingName
import com.curseclient.client.utility.math.MathUtils.toInt
import com.curseclient.client.utility.player.MovementUtils.calcMoveRad
import com.curseclient.client.utility.player.MovementUtils.isInputting
import com.curseclient.client.utility.player.MovementUtils.verticalMovement
import com.curseclient.client.utility.player.PacketUtils.send
import net.minecraft.network.play.client.CPacketConfirmTeleport
import net.minecraft.network.play.client.CPacketPlayer
import java.util.*
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

object Flight : Module(
    "Flight",
    "Allows to fly in survival",
    Category.MOVEMENT
) {
    private val mode by setting("Mode", Mode.Vanilla)

    // vanilla
    private val vanillaSpeed by setting("Vanilla Speed", 1.0, 0.1, 10.0, 0.1, { mode == Mode.Vanilla })
    private val vanillaGlideSpeed by setting("Vanilla Glide Speed", 0.0, 0.0, 1.0, 0.02, { mode == Mode.Vanilla })

    // packet
    private val baseSpeed by setting("Speed", 0.2873, 0.01, 0.2873, 0.001, { mode == Mode.Packet })
    private val setbackMode by setting("Setback", SetbackMode.Up, { mode == Mode.Packet })
    private val setbackDistance by setting("Setback Distance", 555.0, 20.0, 1000.0, 1.0, { mode == Mode.Packet })
    private val setbackRandomizeFactor by setting("Randomize Factor", 0.0, 0.0, 20.0, 0.1, { mode == Mode.Packet })
    private val applyMotion by setting("Apply Motion", false, { mode == Mode.Packet })
    private val confirmTeleport by setting("Confirm Teleport", false, { mode == Mode.Packet })
    private val antikick by setting("Anti Kick", AntiKickMode.Fall, { mode == Mode.Packet })
    private val phase by setting("Phase", false, { mode == Mode.Packet })

    private const val ANTIKICK_SHIFT = 0.03125
    private const val PHASE_SPEED = 0.0624

    private enum class Mode {
        Vanilla,
        Packet,
        FallSpoof
    }

    @Suppress("UNUSED")
    private enum class SetbackMode(val direction: () -> Double) {
        Up({1.0}),
        Down({-1.0}),
        Strict({ (Random().nextDouble() - 0.5) * 2.0 })
    }

    @Suppress("UNUSED")
    private enum class AntiKickMode {
        None,
        Fall,
        Spoof
    }

    override fun getHudInfo() = mode.settingName

    init {
        safeListener<MoveEvent> {
            when(mode){
                Mode.Vanilla -> {
                    val vDir = verticalMovement
                    val dir = calcMoveRad()

                    player.motionY = if (vDir == 0) vanillaGlideSpeed * -0.1 else vDir.toDouble() * vanillaSpeed

                    if (isInputting()) {
                        player.motionX = -sin(dir) * vanillaSpeed
                        player.motionZ = cos(dir) * vanillaSpeed
                    } else {
                        player.motionX = 0.0
                        player.motionZ = 0.0
                    }
                }

                Mode.Packet -> {
                    player.setVelocity(0.0, 0.0, 0.0)

                    if (PacketManager.lastTeleportId < 0) {
                        if (player.ticksExisted % 10 == 0) CPacketPlayer.Position(0.0, 0.0, 0.0, true).send()
                        return@safeListener
                    }
                    val dir = calcMoveRad()

                    val inputting = isInputting().toInt().toDouble()
                    val speed = if (phase && player.isInsideBlock) PHASE_SPEED else baseSpeed
                    val setbackDist = max((setbackDistance + setbackRandomizeFactor * Random().nextDouble()), 20.0) * setbackMode.direction()

                    val v = verticalMovement.toDouble() * PHASE_SPEED

                    val x = -sin(dir) * speed * inputting
                    val packetY = -ANTIKICK_SHIFT * (antikick == AntiKickMode.Fall && player.ticksExisted % 14 == 0 && verticalMovement == 0).toInt().toDouble()
                    val posY = -ANTIKICK_SHIFT * (antikick == AntiKickMode.Spoof && player.ticksExisted % 14 == 0 && verticalMovement == 0).toInt().toDouble()
                    val z = cos(dir) * speed * inputting

                    val pos = player.positionVector.add(x, posY + v, z)
                    val setbackPos = pos.add(0.0, setbackDist, 0.0)

                    CPacketPlayer.Position(
                        pos.x,
                        pos.y + packetY,
                        pos.z,
                        true
                    ).send()

                    CPacketPlayer.Position(
                        setbackPos.x,
                        setbackPos.y,
                        setbackPos.z,
                        true
                    ).send()

                    if (applyMotion)
                        player.setPosition(pos.x, pos.y, pos.z)

                    if (confirmTeleport)
                        CPacketConfirmTeleport(PacketManager.lastTeleportId++).send()
                }

                else -> {}
            }
        }

        safeListener<PlayerPacketEvent.State> {
            if (mode != Mode.Packet) return@safeListener
            it.isMoving = false
            it.isRotating = false
            PacketManager.positionUpdateTicks = 0
        }

        safeListener<PlayerPacketEvent.Data> {
            if (mode == Mode.Packet) it.onGround = false

            if (mode != Mode.FallSpoof) return@safeListener

            if (player.ticksExisted % 2 == 0 && !player.onGround && !player.movementInput.sneak) {
                player.jumpTicks = 0
                player.fallDistance = 0f
                it.onGround = true
                player.motionY = 0.0
            }
        }
    }
}