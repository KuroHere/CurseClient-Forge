package com.curseclient.client.manager.managers

import com.curseclient.client.event.EventBus
import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.ConnectionEvent
import com.curseclient.client.event.events.PlayerHotbarSlotEvent
import com.curseclient.client.event.listener.runSafe
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.Manager
import net.minecraft.network.play.client.CPacketHeldItemChange

object HotbarManager : Manager("HotbarManager") {
    var lastReportedSlot = 1; private set
    var lastSwapTime = 0L; private set
    fun handleSlotUpdate() = runSafe { updateSlot() }

    fun SafeClientEvent.updateSlot() {
        val event = PlayerHotbarSlotEvent(player.inventory.currentItem)
        EventBus.post(event)
        val slot = event.slot

        sendSlotPacket(slot)
    }

    fun SafeClientEvent.sendSlotPacket(slot: Int) {
        if (slot != lastReportedSlot) {
            lastReportedSlot = slot
            lastSwapTime = System.currentTimeMillis()
            val packet = CPacketHeldItemChange(lastReportedSlot)
            connection.sendPacket(packet)
        }
    }

    init {
        safeListener<ConnectionEvent.Connect> { lastReportedSlot = 1 }
        safeListener<ConnectionEvent.Disconnect> { lastReportedSlot = 1 }
    }
}