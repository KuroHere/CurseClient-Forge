package com.curseclient.client.module.modules.player

import com.curseclient.client.event.events.PacketEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.utility.extension.mixins.playerPosLookPitch
import com.curseclient.client.utility.extension.mixins.playerPosLookYaw
import net.minecraft.network.play.server.SPacketPlayerPosLook

object AntiRotate : Module(
    "AntiRotate",
    "Cancel server's rotation packets",
    Category.PLAYER
) {
    init {
        safeListener<PacketEvent.Receive> {
            if (it.packet !is SPacketPlayerPosLook) return@safeListener
            it.packet.playerPosLookYaw = player.rotationYaw
            it.packet.playerPosLookPitch = player.rotationPitch
        }
    }
}