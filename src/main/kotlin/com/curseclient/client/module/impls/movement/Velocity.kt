package com.curseclient.client.module.impls.movement

import com.curseclient.client.event.events.PacketEvent
import com.curseclient.client.event.events.PushByEntityEvent
import com.curseclient.client.event.events.PushOutOfBlocksEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketExplosion

object Velocity : Module(
    "Velocity",
    "Controls your velocity",
    Category.MOVEMENT
) {
    private val noKnockBack by setting("No Knock Back", true)
    private val noEntityPush by setting("No Entity Push", true)
    private val noBlockPush by setting("No Block Push", true)
    private val explosion by setting("Explosion", true)

    init {
        safeListener<PacketEvent.Receive> {
            if (it.packet is SPacketEntityVelocity) {
                if (it.packet.entityID != player.entityId) return@safeListener
                if (noKnockBack) it.cancel()
            }

            if (it.packet is SPacketExplosion && explosion) it.cancel()
        }

        safeListener<PushOutOfBlocksEvent> {
            if(noBlockPush) it.cancel()
        }

        safeListener<PushByEntityEvent> {
            if(noEntityPush) it.cancel()
        }
    }
}