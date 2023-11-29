package com.curseclient.client.module.modules.hud.TargetHUD

import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.shader.RoundedUtil
import sun.plugin2.util.ColorUtil
import java.awt.Color


class Particles {
    var x = 0.0
    var y = 0.0
    var adjustedX = 0.0
    var adjustedY = 0.0
    var deltaX = 0.0
    var deltaY = 0.0
    var size = 0.0
    var opacity = 0.0
    private var nullableColor: Color? = null
    var color: Color = nullableColor ?: Color.WHITE

    fun render2D() {
        RoundedUtil.drawRound((x + adjustedX).toFloat(), (y + adjustedY).toFloat(), size.toFloat(), size.toFloat(), (size / 2f - .5f).toFloat(), ColorUtils.applyOpacity(color, opacity.toFloat() / 255f))
    }

    fun updatePosition() {
        for (i in 1..2) {
            adjustedX += deltaX
            adjustedY += deltaY
            deltaY *= 0.97
            deltaX *= 0.97
            opacity -= 1.0
            if (opacity < 1) opacity = 1.0
        }
    }

    fun init(x: Float, y: Float, deltaX: Float, deltaY: Float, size: Float, color: Color) {
        this.x = x.toDouble()
        this.y = y.toDouble()
        this.deltaX = deltaX.toDouble()
        this.deltaY = deltaY.toDouble()
        this.size = size.toDouble()
        opacity = 254.0
        this.color = color

    }
}