package com.curseclient.client.utility.render.animation.animaions.simple

import kotlin.math.abs

class SimpleAnimation(var value: Float) {
    private var lastMS: Long

    init {
        lastMS = System.currentTimeMillis()
    }

    fun setAnimation(value: Float, speed: Double) {
        var spd = speed
        val currentMS = System.currentTimeMillis()
        val delta = currentMS - lastMS
        lastMS = currentMS
        var deltaValue = 0.0
        if (spd > 28) {
            spd = 28.0
        }
        if (spd != 0.0) {
            deltaValue = abs((value - this.value).toDouble()) * 0.35f / (10.0 / spd)
        }
        this.value = SimpleUtil.animate(value, this.value, deltaValue, delta)
    }
}