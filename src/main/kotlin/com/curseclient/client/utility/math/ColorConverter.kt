package com.curseclient.client.utility.math

import com.curseclient.client.utility.render.ColorUtils

class ColorConverter(var color: Int) {

    fun setRed(value: Int): Int {
        var value = value
        if (value >= 255) value = 255
        if (value < 0) value = 0
        return setColor(ColorUtils.toRGBA(value, green, blue, alpha))
    }

    fun setGreen(value: Int): Int {
        var value = value
        if (value >= 255) value = 255
        if (value < 0) value = 0
        return setColor(ColorUtils.toRGBA(red, value, blue, alpha))
    }

    fun setBlue(value: Int): Int {
        var value = value
        if (value >= 255) value = 255
        if (value < 0) value = 0
        return setColor(ColorUtils.toRGBA(red, green, value, alpha))
    }

    fun setAlpha(value: Int): Int {
        var value = value
        if (value >= 255) value = 255
        if (value < 0) value = 0
        return setColor(ColorUtils.toRGBA(red, green, blue, value))
    }

    val red: Int
        get() = color shr 16 and 255
    val green: Int
        get() = color shr 8 and 255
    val blue: Int
        get() = color and 255
    val alpha: Int
        get() = color shr 24 and 255

    fun setColor(color: Int): Int {
        return color.also { this.color = it }
    }
}