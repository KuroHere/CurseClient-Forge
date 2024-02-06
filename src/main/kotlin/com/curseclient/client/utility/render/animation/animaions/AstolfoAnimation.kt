package com.curseclient.client.utility.render.animation.animaions

import net.minecraft.client.Minecraft
import net.minecraft.util.math.MathHelper
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

object AstolfoAnimation {
    private var value = 0.0
    private var prevValue = 0.0

    fun update() {
        prevValue = value
        value += 0.01
    }

    fun getColor(speed: Int,
                 offset: Int,
                 saturation: Float,
                 brightness: Float,
                 alpha: Float): Int {
        var hue: Float = calculateHueDegrees(speed, offset).toFloat()
        hue = (hue.toDouble() % 360.0).toFloat()
        var hueNormalized: Float
        return reAlphaInt(
            Color.HSBtoRGB(if ((((hue % 360.0f).also { hueNormalized = it }) / 360.0f).toDouble() < 0.5) -(hueNormalized / 360.0f) else hueNormalized / 360.0f, saturation, brightness),
            max(0.0, min(255.0, (alpha * 255.0f).toInt().toDouble())).toInt()
        )
    }

    private fun reAlphaInt(color: Int,
                           alpha: Int): Int {
        return (MathHelper.clamp(alpha, 0, 255) shl 24) or (color and 16777215)
    }

    private fun calculateHueDegrees(divisor: Int,
                                    offset: Int): Int {
        val currentTime = System.currentTimeMillis()
        val calculatedValue = (currentTime / divisor + offset) % 360L
        return calculatedValue.toInt()
    }

    fun getColor(offset: Double): Int {
        var hue = (prevValue + (value - prevValue) * Minecraft.getMinecraft().renderPartialTicks + offset) % 1.0
        if (hue > 0.5f) {
            hue = 0.5f - (hue - 0.5f)
        }
        hue += 0.5
        return Color.HSBtoRGB(hue.toFloat(), 0.5f, 1f)
    }

}