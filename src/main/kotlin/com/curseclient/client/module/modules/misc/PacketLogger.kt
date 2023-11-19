package com.curseclient.client.module.modules.misc

import com.curseclient.client.event.events.PacketEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.transformIf
import com.curseclient.client.utility.math.MathUtils.roundToPlaces
import com.curseclient.client.utility.player.ChatUtils
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayer

object PacketLogger : Module(
    "PacketLogger",
    "Shows you what packets is sending by client",
    Category.MISC
) {
    private val roundCPacketPlayer by setting("Round CPacketPlayer", true)
    private val positionRoundPlaces by setting("Position Round Places", 3.0, 1.0, 5.0, 1.0)

    init {
        safeListener<PacketEvent.PostSend> { event ->
            val message = event.packet.toMessage()
            message?.let { ChatUtils.sendMessage("[${event.packet.javaClass.simpleName.replace('$', '.')}] $it") }
        }
    }

    private fun Packet<*>.toMessage(): String? {
        return when(val packet = this) {
            is CPacketPlayer.Position -> "x:${packet.x()} y:${packet.y()} z:${packet.z()}"
            is CPacketPlayer.Rotation -> "yaw:${packet.yaw()} pitch:${packet.pitch()}"
            is CPacketPlayer.PositionRotation -> "x:${packet.x()} y:${packet.y()} z:${packet.z()} yaw:${packet.yaw()} pitch:${packet.pitch()}"
            is CPacketPlayer -> "ground: ${packet.isOnGround}"

            is CPacketAnimation -> "hand: ${packet.hand.name}"
            //is CPacketUseEntity -> "hand: ${packet.hand.name} action: ${packet.action.name} hitVec: ${packet.hitVec} entityID: ${packet.useEntityId}"

            else -> null
        }
    }

    private fun CPacketPlayer.x() =
        getX(0.0).transformIf(roundCPacketPlayer) { it.roundToPlaces(positionRoundPlaces.toInt()) }

    private fun CPacketPlayer.y() =
        getY(0.0).transformIf(roundCPacketPlayer) { it.roundToPlaces(positionRoundPlaces.toInt()) }

    private fun CPacketPlayer.z() =
        getZ(0.0).transformIf(roundCPacketPlayer) { it.roundToPlaces(positionRoundPlaces.toInt()) }

    private fun CPacketPlayer.yaw() =
        getYaw(0f).toDouble().transformIf(roundCPacketPlayer) { it.roundToPlaces(positionRoundPlaces.toInt()) }

    private fun CPacketPlayer.pitch() =
        getPitch(0f).toDouble().transformIf(roundCPacketPlayer) { it.roundToPlaces(positionRoundPlaces.toInt()) }
}