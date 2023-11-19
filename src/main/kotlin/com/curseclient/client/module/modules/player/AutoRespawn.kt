package com.curseclient.client.module.modules.player

import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import net.minecraft.network.play.client.CPacketClientStatus
import net.minecraftforge.fml.common.gameevent.TickEvent

object AutoRespawn : Module(
    "AutoRespawn",
    "Automatically respawns you",
    Category.PLAYER
) {
    init {
        safeListener<TickEvent.ClientTickEvent>{
            if (!player.isDead) return@safeListener
            connection.sendPacket(CPacketClientStatus(CPacketClientStatus.State.PERFORM_RESPAWN))
        }
    }
}