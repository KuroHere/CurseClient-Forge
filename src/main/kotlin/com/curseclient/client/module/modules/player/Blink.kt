package com.curseclient.client.module.modules.player

import com.curseclient.client.event.events.ConnectionEvent
import com.curseclient.client.event.events.PacketEvent
import com.curseclient.client.event.listener.listener
import com.curseclient.client.event.listener.runSafe
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import net.minecraft.network.play.client.CPacketPlayer

object Blink : Module(
    "Blink",
    "Holds player packets",
    Category.PLAYER
) {
    private val packets = ArrayList<CPacketPlayer>()

    init {
        safeListener<PacketEvent.Send> {
            if (it.packet !is CPacketPlayer) return@safeListener
            it.cancel()
            packets.add(it.packet)
        }

        listener<ConnectionEvent.Connect> { packets.clear() }
        listener<ConnectionEvent.Disconnect> { packets.clear() }
    }

    override fun onEnable() { packets.clear() }

    override fun onDisable() {
        runSafe { packets.forEach { connection.sendPacket(it) } }
        packets.clear()
    }
}