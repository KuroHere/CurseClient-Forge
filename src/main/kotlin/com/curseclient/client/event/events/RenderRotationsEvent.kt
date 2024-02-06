package com.curseclient.client.event.events

import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

@Cancelable
class RenderRotationsEvent : Event() {
    var yaw = 0.0f
    var pitch = 0.0f

    fun setYawValue(inYaw: Float) {
        yaw = inYaw
    }

    fun setPitchValue(inPitch: Float) {
        pitch = inPitch
    }
}