package com.curseclient.client.utility.player

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.listener.runSafe
import net.minecraft.network.Packet

object PacketUtils {
    fun Packet<*>.send() = runSafe {
        connection.sendPacket(this@send)
    }

    fun Packet<*>.send(event: SafeClientEvent) =
        event.connection.sendPacket(this@send)

}