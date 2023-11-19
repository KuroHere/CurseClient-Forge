package com.curseclient.client.utility.math

import net.minecraft.client.Minecraft
import kotlin.math.roundToInt

object GCDFix {
    fun getFixedRotation(rot: Float): Float {
        return getDeltaMouse(rot) * getGCDValue()
    }

    private fun getGCDValue(): Float {
        return (getGCD().toDouble() * 0.15).toFloat()
    }

    private fun getGCD(): Float {
        val f1 = (Minecraft.getMinecraft().gameSettings.mouseSensitivity * 0.6 + 0.2).toFloat()
        return f1 * f1 * f1 * 8.0f
    }

    private fun getDeltaMouse(delta: Float): Float {
        return (delta / getGCDValue()).roundToInt().toFloat()
    }
}