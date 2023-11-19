package com.curseclient.client.event.events

import com.curseclient.client.event.Cancellable
import com.curseclient.client.event.Event
import com.curseclient.client.event.ICancellable
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity

abstract class AttackEvent(val entity: Entity) : Event, Cancellable() {
    class Pre(entity: Entity) : AttackEvent(entity)
    class Post(entity: Entity) : AttackEvent(entity)
}

class MoveEvent(private val player: EntityPlayerSP) : Event, ICancellable by Cancellable() {
    private val prevX = player.motionX
    private val prevY = player.motionY
    private val prevZ = player.motionZ

    val isModified: Boolean
        get() = player.motionX != prevX
            || player.motionY != prevY
            || player.motionZ != prevZ

    var x: Double
        get() = if (cancelled) 0.0 else player.motionX
        set(value) {
            if (!cancelled) player.motionX = value
        }

    var y: Double
        get() = if (cancelled) 0.0 else player.motionY
        set(value) {
            if (!cancelled) player.motionY = value
        }

    var z: Double
        get() = if (cancelled) 0.0 else player.motionZ
        set(value) {
            if (!cancelled) player.motionZ = value
        }
}

class JumpMotionEvent(var motion: Float) : Event, ICancellable by Cancellable()
class JumpEvent : Event, ICancellable by Cancellable()

class PushByEntityEvent : Event, ICancellable by Cancellable()

class PushOutOfBlocksEvent : Event, ICancellable by Cancellable()

abstract class PlayerPacketEvent : Event {
    class Data(var x: Double, var y: Double, var z: Double, var yaw: Float, var pitch: Float, var onGround: Boolean) : PlayerPacketEvent()
    class State(var isMoving: Boolean, var isRotating: Boolean) : PlayerPacketEvent()
    class Misc(var isSprinting: Boolean, var isSneaking: Boolean) : PlayerPacketEvent()
    class Post : PlayerPacketEvent()
}

class TravelEvent : Event, ICancellable by Cancellable()

class PlayerHotbarSlotEvent(var slot: Int) : Event

abstract class StepEvent : Event {
    class Pre(var height: Double) : StepEvent(), ICancellable by Cancellable()
    class Post : StepEvent()
}