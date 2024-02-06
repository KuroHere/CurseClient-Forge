package com.curseclient.client.event.events.render

import com.curseclient.client.event.Cancellable
import com.curseclient.client.event.Event
import com.curseclient.client.event.ICancellable
import net.minecraft.entity.Entity

abstract class RenderEntityEvent(val entity: Entity) : Event, ICancellable by Cancellable() {
    class Pre(entityIn: Entity) : RenderEntityEvent(entityIn)
    class Peri(entityIn: Entity) : RenderEntityEvent(entityIn)
    class Post(entityIn: Entity) : RenderEntityEvent(entityIn)

    class ModelPre(entity: Entity) : RenderEntityEvent(entity)
    class ModelPost(entity: Entity) : RenderEntityEvent(entity)

    companion object {
        @JvmStatic
        var renderingEntities = false
    }
}

class Render2DEvent(val partialTicks: Float) : Event
class Render3DEvent(val partialTicks: Float) : Event

class ResolutionUpdateEvent(val width: Int, val height: Int) : Event