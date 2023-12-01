package com.curseclient.client.event.events.render

import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraftforge.fml.common.eventhandler.Event

class EventFovUpdate(val entity: AbstractClientPlayer, var fov: Float) : Event() {


    @JvmName("getFovValue")
    fun getFov(): Float {
        return fov
    }

    @JvmName("setFovValue")
    fun setFov(fov: Float) {
        this.fov = fov
    }

    @JvmName("getEntityValue")
    fun getEntity(): AbstractClientPlayer {
        return entity
    }
}
