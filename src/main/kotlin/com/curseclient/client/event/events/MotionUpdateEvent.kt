package com.curseclient.client.event.events

import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

@Cancelable
class MotionUpdateEvent : Event() {
    var x: Double = 0.0
    var y: Double = 0.0
    var z: Double = 0.0
    var yaw: Float = 0.0f
    var pitch: Float = 0.0f
    var onGround: Boolean = false
}