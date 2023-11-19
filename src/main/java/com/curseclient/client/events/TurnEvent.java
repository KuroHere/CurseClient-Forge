package com.curseclient.client.events;

import com.curseclient.client.event.Cancellable;
import com.curseclient.client.event.ICancellable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class TurnEvent extends Cancellable implements ICancellable {
    private final float yaw;
    private final float pitch;

    public TurnEvent(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

}