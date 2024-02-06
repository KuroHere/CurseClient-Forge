package com.curseclient.client.event.events


import com.curseclient.client.event.Cancellable
import com.curseclient.client.event.ICancellable
import net.minecraft.inventory.EntityEquipmentSlot

class ArmorRenderEvent private constructor() : Cancellable(), ICancellable {

    private lateinit var slot: EntityEquipmentSlot

    companion object {
        private val INSTANCE = ArmorRenderEvent()

        fun get(slot: EntityEquipmentSlot): ArmorRenderEvent {
            INSTANCE.cancelled
            INSTANCE.slot = slot
            return INSTANCE
        }
    }

    fun getSlot(): EntityEquipmentSlot {
        return slot
    }
}