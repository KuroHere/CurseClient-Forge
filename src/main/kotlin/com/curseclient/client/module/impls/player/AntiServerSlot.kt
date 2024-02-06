package com.curseclient.client.module.impls.player

import com.curseclient.client.event.events.PacketEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.managers.HotbarManager
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.utility.player.PacketUtils.send
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.server.SPacketHeldItemChange

object AntiServerSlot : Module(
    "AntiServerSlot",
    "Prevents server from changing hotbar slot (may cause desync)",
    Category.PLAYER
) {
    init {
        safeListener<PacketEvent.Receive> { event ->
            if (event.packet !is SPacketHeldItemChange) return@safeListener
            event.cancel()
            CPacketHeldItemChange(HotbarManager.lastReportedSlot).send()
        }
    }
}