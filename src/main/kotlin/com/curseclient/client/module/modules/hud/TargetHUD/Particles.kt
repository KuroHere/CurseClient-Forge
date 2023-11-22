package com.curseclient.client.module.modules.hud.TargetHUD

import com.curseclient.client.gui.impl.particles.simple.util.RenderUtils
import java.awt.Color

class Particles {
    var x = 0.0
    var y = 0.0
    var deltaX = 0.0
    var deltaY = 0.0
    var size = 0.0
    var opacity = 0.0
    var color: Color? = null

    fun render2D() {
        RenderUtils.circle(x, y, size, Color(color!!.red, color!!.green, color!!.blue, opacity.toInt()))
    }

    fun updatePosition() {
        x += deltaX * 2
        y += deltaY * 2
        deltaY *= 0.95
        deltaX *= 0.95
        opacity -= 2.0
        if (opacity < 1) opacity = 1.0
    }

    fun init(x: Double, y: Double, deltaX: Double, deltaY: Double, size: Double, color: Color) {
        this.x = x
        this.y = y
        this.deltaX = deltaX
        this.deltaY = deltaY
        this.size = size
        this.opacity = 254.0
        this.color = color
    }
}