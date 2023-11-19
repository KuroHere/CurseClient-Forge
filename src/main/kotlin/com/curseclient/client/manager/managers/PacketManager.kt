package com.curseclient.client.manager.managers

import com.curseclient.client.event.EventBus
import com.curseclient.client.event.events.ConnectionEvent
import com.curseclient.client.event.events.PacketEvent
import com.curseclient.client.event.events.PlayerPacketEvent
import com.curseclient.client.event.listener.listener
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.Manager
import com.curseclient.client.module.modules.player.FreeCam
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayer.*
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.math.Vec3d

object PacketManager: Manager("PacketManager") {
    private val mc = Minecraft.getMinecraft()

    // for some modules
    var prevReportedPos: Vec3d = Vec3d.ZERO; private set

    // for VisualRotations module
    var prevTickYaw = 0f; private set
    var prevTickPitch = 0f; private set

    var lastReportedPosX = 0.0
    var lastReportedPosY = 0.0
    var lastReportedPosZ = 0.0

    var lastReportedYaw = 0f
    var lastReportedPitch = 0f

    var lastReportedSprinting = false
    var lastReportedSneaking = false
    var lastReportedOnGround = false

    var positionUpdateTicks = 0

    var lastTeleportId = -1

    @JvmStatic
    fun handlePacketUpdate(){
        val player: EntityPlayerSP = mc.player

        // region MISC
        val miscEvent = PlayerPacketEvent.Misc(player.isSprinting, player.isSneaking)
        EventBus.post(miscEvent)

        val sprinting = miscEvent.isSprinting
        val sneaking = miscEvent.isSneaking

        if (sprinting != lastReportedSprinting) {
            if (sprinting) {
                send(CPacketEntityAction(player, CPacketEntityAction.Action.START_SPRINTING))
            } else {
                send(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SPRINTING))
            }
            lastReportedSprinting = sprinting
        }

        if (sneaking != lastReportedSneaking) {
            if (sneaking) {
                send(CPacketEntityAction(player, CPacketEntityAction.Action.START_SNEAKING))
            } else {
                send(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))
            }
            lastReportedSneaking = sneaking
        }
        // endregion

        if (mc.renderViewEntity != player && !FreeCam.isEnabled()) return
        positionUpdateTicks++
        val bb = player.entityBoundingBox

        // region DATA EVENT
        val dataEvent = PlayerPacketEvent.Data(player.posX, bb.minY, player.posZ, player.rotationYaw, player.rotationPitch, player.onGround)
        EventBus.post(dataEvent)

        val x = dataEvent.x
        val y = dataEvent.y
        val z = dataEvent.z
        val yaw = dataEvent.yaw
        val pitch = dataEvent.pitch
        val onGround = dataEvent.onGround
        // endregion

        // region STATE EVENT
        val xDiff = x - lastReportedPosX
        val yDiff = y - lastReportedPosY
        val zDiff = z - lastReportedPosZ

        val yawDiff = yaw - lastReportedYaw
        val pitchDiff = pitch - lastReportedPitch

        var isMoving = (xDiff * xDiff + yDiff * yDiff + zDiff * zDiff > 9.0E-4) || positionUpdateTicks >= 20
        var isRotating = yawDiff != 0.0f || pitchDiff != 0.0f

        val stateEvent = PlayerPacketEvent.State(isMoving, isRotating)
        EventBus.post(stateEvent)

        isMoving = stateEvent.isMoving
        isRotating = stateEvent.isRotating

        // endregion

        // region SENDING
        if (player.isRiding) {
            send(PositionRotation(player.motionX, -999.0, player.motionZ, yaw, pitch, onGround))
            isMoving = false
        }

        else if (isMoving && isRotating) { send(PositionRotation(x, y, z, yaw, pitch, onGround)) }

        else if (isMoving) { send(Position(x, y, z, onGround)) }

        else if (isRotating) { send(Rotation(yaw, pitch, onGround)) }

        else if (lastReportedOnGround != onGround) { send(CPacketPlayer(onGround)) }
        // endregion

        // region UPDATE REPORTED
        prevTickYaw = lastReportedYaw
        prevTickPitch = lastReportedPitch

        if (isMoving) {
            prevReportedPos = Vec3d(lastReportedPosX, lastReportedPosY, lastReportedPosZ)

            lastReportedPosX = x
            lastReportedPosY = y
            lastReportedPosZ = z
            positionUpdateTicks = 0
        }

        if (isRotating) {
            lastReportedYaw = yaw
            lastReportedPitch = pitch
        }

        lastReportedOnGround = onGround
        // endregion

        val postEvent = PlayerPacketEvent.Post()
        EventBus.post(postEvent)
    }

    private fun send(packet: Packet<*>) {
        Minecraft.getMinecraft().player.connection.sendPacket(packet)
    }

    init {
        listener<PacketEvent.Receive> {
            when (it.packet) {
                is SPacketPlayerPosLook -> {
                    lastTeleportId = it.packet.teleportId
                }
            }
        }

        safeListener<ConnectionEvent.Connect> {
            lastTeleportId = -1
        }
    }
}