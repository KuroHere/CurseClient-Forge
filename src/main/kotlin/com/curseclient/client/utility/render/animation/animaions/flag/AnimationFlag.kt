package com.curseclient.client.utility.render.animation.animaions.flag

import com.curseclient.client.utility.render.animation.ease.Delta
import com.curseclient.client.utility.render.animation.ease.EaseUtils

class AnimationFlag(private val interpolation: InterpolateFunction) {

    constructor(easing: EaseUtils, length: Float) : this({ time, prev, current ->
        easing.incOrDec(Delta.toDelta(time, length), prev, current)
    })

    var prev = 0.0f; private set
    var current = 0.0f; private set
    var time = System.currentTimeMillis(); private set

    fun forceUpdate(value: Float) {
        forceUpdate(value, value)
    }

    fun forceUpdate(prev: Float, current: Float) {
        if (prev.isNaN() || current.isNaN()) return

        this.prev = prev
        this.current = current
        time = System.currentTimeMillis()
    }

    fun update(current: Float) {
        if (!current.isNaN() && this.current != current) {
            prev = this.current
            this.current = current
            time = System.currentTimeMillis()
        }
    }

    fun getAndUpdate(current: Float): Float {
        val render = interpolation.invoke(time, prev, this.current)

        if (!current.isNaN() && current != this.current) {
            prev = render
            this.current = current
            time = System.currentTimeMillis()
        }

        return render
    }

    fun get(): Float {
        return interpolation.invoke(time, prev, current)
    }

    fun forceCurrent() {
        prev = current
        time = System.currentTimeMillis()
    }
}
