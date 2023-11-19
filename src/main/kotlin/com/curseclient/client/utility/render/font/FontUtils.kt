package com.curseclient.client.utility.render.font

import com.curseclient.client.module.modules.client.FontSettings
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.vector.Vec2d
import java.awt.Color

object FontUtils {

    fun Fonts.drawString(
        text: String,
        pos: Vec2d,
        shadow: Boolean = true,
        color: Color = Color.WHITE,
        scale: Double = 1.0,
    ) {
        val x = pos.x
        val y = pos.y - this.getHeight(scale) / 2.0

        FontRenderer.drawString(text, x.toFloat(), y.toFloat(), shadow && FontSettings.shadow, color, scale.toFloat(), this)
    }

    fun Fonts.getHeight(scale: Double = 1.0) =
        FontRenderer.getFontHeight(this, scale.toFloat()).toDouble()

    fun Fonts.
        getStringWidth(text: String, scale: Double = 1.0) =
        FontRenderer.getStringWidth(text, this, scale.toFloat()).toDouble()

    fun getColorIndex(type: Char): Int {
        return when (type) {
            in '0'..'9' -> type - '0'
            in 'a'..'f' -> type - 'a' + 10
            in 'k'..'o' -> type - 'k' + 16
            'r' -> 21
            else -> -1
        }
    }
}