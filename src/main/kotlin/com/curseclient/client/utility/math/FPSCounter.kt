package com.curseclient.client.utility.math

object FPSCounter {
    private var lastRenderTime = 0L
    var deltaTime = 0.0; private set

    fun tick() {
        val time = System.nanoTime()

        val prevRenderTime = lastRenderTime
        lastRenderTime = time

        if (prevRenderTime < 1) return
        val delta = time - prevRenderTime
        deltaTime = delta.toDouble() * 0.000001 * 0.001
    }
}