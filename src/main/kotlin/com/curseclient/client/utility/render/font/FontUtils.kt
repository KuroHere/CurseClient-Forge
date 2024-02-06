package com.curseclient.client.utility.render.font

import com.curseclient.client.module.impls.client.FontSettings
import com.curseclient.client.utility.render.vector.Vec2d
import java.awt.Color

object FontUtils {

    fun Fonts.drawCentreString(
        text: String,
        pos: Vec2d,
        shadow: Boolean = true,
        color: Color = Color.WHITE,
        scale: Double = 1.0,
    ) {
        val x = pos.x - this.getStringWidth(text, scale) / 2
        val y = pos.y - this.getHeight(scale) / 2.0

        FontRenderer.drawString(text, x.toFloat(), y.toFloat(), shadow && FontSettings.shadow, color, scale.toFloat(), this)
    }
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
}