package com.curseclient.client.manager.managers

import baritone.api.utils.Helper.mc
import com.curseclient.client.event.events.MotionUpdateEvent
import com.curseclient.client.event.events.PacketEvent
import com.curseclient.client.event.events.RenderRotationsEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.Manager
import com.curseclient.client.utility.player.Rotation
import com.curseclient.mixin.accessor.network.AccessorCPacketPlayer
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

// Keeps track of server rotations
object RotationManager : Manager("RotationManager") {

    private val serverRotation = Rotation(Float.NaN, Float.NaN)
    private var rotation = Rotation(Float.NaN, Float.NaN)
    private var stay: Long = 0

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (System.currentTimeMillis() - stay >= 250 && rotation.isValid()) {
                rotation = Rotation(Float.NaN, Float.NaN)
            }
        }
        safeListener<PacketEvent.Send> {
            val packet = it.packet

            if (packet is CPacketPlayer) {
                if ((packet as AccessorCPacketPlayer).isRotating) {
                    serverRotation.yaw = packet.getYaw(0F)
                    serverRotation.pitch = packet.getPitch(0F)
                }
            }
        }
    }

    @SubscribeEvent
    fun onMotionUpdate(event: MotionUpdateEvent) {
        if (rotation.isValid()) {
            event.onGround = mc.player.onGround
            event.x = mc.player.posX
            event.y = mc.player.entityBoundingBox.minY
            event.z = mc.player.posZ
            event.yaw = rotation.yaw
            event.pitch = rotation.pitch
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onRenderRotations(event: RenderRotationsEvent) {
        if (rotation.isValid()) {
            event.isCanceled = true
            event.yaw = serverRotation.yaw
            event.pitch = serverRotation.pitch
        }
    }

    fun setRotation(inRotation: Rotation) {
        rotation = inRotation
        stay = System.currentTimeMillis()
    }

    fun getRotation(): Rotation {
        return rotation
    }

    fun getServerRotation(): Rotation {
        return serverRotation
    }
}