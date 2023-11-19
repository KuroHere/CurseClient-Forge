package com.curseclient.client.setting.type

import com.curseclient.client.setting.Setting
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.render.ColorUtils.setAlphaD
import java.awt.Color
import kotlin.reflect.KProperty

class ColorSetting(
    name: String,
    value: Color,
    visibility: () -> Boolean = { true },
    description: String = ""

): Setting<Any?>(name, visibility, description) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = getColor()
    operator fun setValue(thisRef: Any?, property: KProperty<*>, v: Color) {
        setColor(v)
    }

    var hue = 0f
    var alpha = 1f
    var saturation = 1f
    var brightness = 1f

    init { setColor(value) }

    fun getColor(): Color {
        val color = Color(Color.HSBtoRGB(hue, saturation, brightness))
        return color.setAlphaD(alpha.toDouble())
    }

    fun setColor(color: Color){
        val hsb = Color.RGBtoHSB(color.red, color.green, color.blue, null)
        setColor(hsb[0], color.alpha.toFloat() / 255f, hsb[1], hsb[2])
    }

    fun setColor(hueIn: Float, alphaIn: Float, saturationIn: Float, brightnessIn: Float){
        hue = clamp(hueIn, 0.0f, 1.0f)
        alpha = clamp(alphaIn, 0.0f, 1.0f)
        saturation = clamp(saturationIn, 0.0f, 1.0f)
        brightness = clamp(brightnessIn, 0.0f, 1.0f)

        listeners.forEach { it() }
    }
}