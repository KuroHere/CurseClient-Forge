package com.curseclient.client.events;

import com.curseclient.client.event.Cancellable;
import com.curseclient.client.event.ICancellable;
import net.minecraft.inventory.EntityEquipmentSlot;

public class ArmorRenderEvent extends Cancellable implements ICancellable {
    private static ArmorRenderEvent INSTANCE = new ArmorRenderEvent();

    private EntityEquipmentSlot slot;

    public static ArmorRenderEvent get(EntityEquipmentSlot slot) {
        INSTANCE.setCancelled(false);
        INSTANCE.slot = slot;
        return INSTANCE;
    }

    public EntityEquipmentSlot getSlot() {
        return slot;
    }
}