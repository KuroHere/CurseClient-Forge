package com.curseclient.client.event.events.render

import com.curseclient.client.event.Cancellable

class RenderPutColorMultiplierEvent : Cancellable() {
    internal var opacity = 0f

    fun setOpacity(opacity: Float) {
        this.opacity = opacity
    }

    fun getOpacity(): Float {
        return opacity
    }

}