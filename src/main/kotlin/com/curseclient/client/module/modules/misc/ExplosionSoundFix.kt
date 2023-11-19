package com.curseclient.client.module.modules.misc

import com.curseclient.client.event.events.PacketEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.mixin.accessor.network.AccessorSPacketSoundEffect
import net.minecraft.init.SoundEvents
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.util.SoundCategory

object ExplosionSoundFix : Module(
    "ExplosionSoundFix",
    "Fixes crystal explosion sound pitch",
    Category.MISC
) {
    private val shift by setting("Shift", -0.2, -0.5, 0.5, 0.05)

    init {
        safeListener<PacketEvent.Receive> {
            val packet = (it.packet as? SPacketSoundEffect) ?: return@safeListener
            if (packet.category != SoundCategory.BLOCKS) return@safeListener
            if (packet.sound != SoundEvents.ENTITY_GENERIC_EXPLODE) return@safeListener

            (packet as AccessorSPacketSoundEffect).setPitch(packet.pitch + shift.toFloat())
        }
    }
}