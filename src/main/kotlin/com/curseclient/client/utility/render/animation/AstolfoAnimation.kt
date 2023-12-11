package com.curseclient.client.utility.render.animation

import net.minecraft.client.Minecraft
import kotlin.math.floor


class AstolfoAnimation {
    private var value = 0.0
    private var prevValue = 0.0
    fun update() {
        prevValue = value
        value += 0.01
    }

    fun getColor(offset: Double): Int {
        var hue = (prevValue + (value - prevValue) * Minecraft.getMinecraft().renderPartialTicks + offset) % 1.0
        if (hue > 0.5f) {
            hue = 0.5f - (hue - 0.5f)
        }
        hue += 0.5
        return HSBtoRGB(hue.toFloat(), 0.5f, 1f)
    }

    companion object {
        fun HSBtoRGB(hue: Float, saturation: Float, brightness: Float): Int {
            var r = 0
            var g = 0
            var b = 0
            if (saturation == 0f) {
                b = (brightness * 255.0f + 0.5f).toInt()
                g = b
                r = g
            } else {
                val h = (hue - floor(hue.toDouble()).toFloat()) * 6.0f
                val f = h - floor(h.toDouble()).toFloat()
                val p = brightness * (1.0f - saturation)
                val q = brightness * (1.0f - saturation * f)
                val t = brightness * (1.0f - saturation * (1.0f - f))
                when (h.toInt()) {
                    0 -> {
                        r = (brightness * 255.0f + 0.5f).toInt()
                        g = (t * 255.0f + 0.5f).toInt()
                        b = (p * 255.0f + 0.5f).toInt()
                    }

                    1 -> {
                        r = (q * 255.0f + 0.5f).toInt()
                        g = (brightness * 255.0f + 0.5f).toInt()
                        b = (p * 255.0f + 0.5f).toInt()
                    }

                    2 -> {
                        r = (p * 255.0f + 0.5f).toInt()
                        g = (brightness * 255.0f + 0.5f).toInt()
                        b = (t * 255.0f + 0.5f).toInt()
                    }

                    3 -> {
                        r = (p * 255.0f + 0.5f).toInt()
                        g = (q * 255.0f + 0.5f).toInt()
                        b = (brightness * 255.0f + 0.5f).toInt()
                    }

                    4 -> {
                        r = (t * 255.0f + 0.5f).toInt()
                        g = (p * 255.0f + 0.5f).toInt()
                        b = (brightness * 255.0f + 0.5f).toInt()
                    }

                    5 -> {
                        r = (brightness * 255.0f + 0.5f).toInt()
                        g = (p * 255.0f + 0.5f).toInt()
                        b = (q * 255.0f + 0.5f).toInt()
                    }
                }
            }
            return -0x1000000 or (r shl 16) or (g shl 8) or b
        }
    }
}