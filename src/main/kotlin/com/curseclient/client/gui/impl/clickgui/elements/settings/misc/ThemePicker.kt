package com.curseclient.client.gui.impl.clickgui.elements.settings.misc

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.elements.InteractiveElement
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.gui.impl.styles.StyleManager
import com.curseclient.client.module.impls.client.ClickGui
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.ColorUtils.toColor
import com.curseclient.client.utility.render.animation.animaions.AstolfoAnimation
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.shader.gradient.GradientUtil
import com.curseclient.client.utility.render.vector.Vec2d

// Lazy to fix (～￣▽￣)～
class ThemePicker(pos: Vec2d,
                  width: Double,
                  height: Double,
                  gui: AbstractGui
) : InteractiveElement(pos, width, height, gui) {

    override fun onRegister() {}

    override fun onGuiOpen() {}

    override fun onGuiClose() {}

    override fun onRender() {
        val panelWidth = width
        val spacing = 3

        StyleManager.Styles.values().forEachIndexed { index, style ->
            val startX = pos.x
            val startY = pos.y + (index * (ClickGui.height + spacing)) + 16

            val (color1, color2) = if (style == StyleManager.Styles.Astolfo) {
                val astro1 = AstolfoAnimation.getColor(10, index, 0.5F, 1.0F, 1.0F).toColor()
                val astro2 = AstolfoAnimation.getColor(15, index, 0.5F, 1.0F, 1.0F).toColor()
                astro1 to astro2
            } else {
                style.color1.toColor() to style.color2.toColor()
            }

            val c1 = if (ClickGui.colorMode == ClickGui.ColorMode.Client)
                HUD.getColor(index)
            else if (ClickGui.pulse) ColorUtils.pulseColor(ClickGui.buttonColor1, 0, 1) else ClickGui.buttonColor1

            val c2 = when (ClickGui.colorMode) {
                ClickGui.ColorMode.Client -> HUD.getColor(index + 1)
                ClickGui.ColorMode.Static -> if (ClickGui.pulse) ColorUtils.pulseColor(ClickGui.buttonColor1, 0, 1) else ClickGui.buttonColor1
                else -> ClickGui.buttonColor2
            }

            RectBuilder(Vec2d(startX, startY), Vec2d(startX + panelWidth, startY + ClickGui.height)).apply {
                if (ClickGui.colorMode == ClickGui.ColorMode.Horizontal)
                    colorH(c1.setAlpha(50), c2.setAlpha(50))
                else
                    colorV(c1.setAlpha(50), c2.setAlpha(50))
                radius(ClickGui.buttonRound)
                draw()
            }

            RectBuilder(Vec2d(startX + width / 1.5, startY + 5), Vec2d(startX + panelWidth - 5, startY + ClickGui.height - 5)).apply {
                color(color1, color2, color1, color2)
                radius(3.0)
                draw()
            }

            GradientUtil.applyGradientCornerRL(
                (pos.x + 2).toFloat(),
                (pos.y + (index * (ClickGui.height + spacing)) + 25).toFloat(),
                fr.getStringWidth(style.name).toFloat(),
                fr.getHeight().toFloat(),
                1F,
                if (style == StyleManager.Styles.Astolfo) color1 else style.color1.toColor(),
                if (style == StyleManager.Styles.Astolfo) color2 else style.color2.toColor()
            ) {
                fr.drawString(style.name, Vec2d(pos.x + 2, pos.y + (index * (ClickGui.height + spacing)) + 25), false)
            }
        }
    }

    fun getButtonHeight() = height + 5

    override fun onGuiCloseAttempt() {}

    override fun onTick() {}

    override fun onMouseAction(action: MouseAction, button: Int) {
        val panelWidth = width
        val spacing = 3

        val mouseX = gui.mouse.x
        val mouseY = gui.mouse.y

        StyleManager.Styles.values().forEachIndexed { index, _ ->
            val startX = pos.x
            val startY = pos.y + (index * (ClickGui.height + spacing)) + 16

            val endX = startX + panelWidth - 5
            val endY = startY + ClickGui.height

            if (mouseX in startX..endX && mouseY >= startY && mouseY <= endY) {
                val clickedStyles = StyleManager.Styles.values()[index]

                val (color1, color2) = if (clickedStyles == StyleManager.Styles.Astolfo) {
                    AstolfoAnimation.getColor(15, index, 0.5F, 1.0F, 1.0F) to AstolfoAnimation.getColor(10, index, 0.5F, 1.0F, 1.0F)
                } else {
                    clickedStyles.color1 to clickedStyles.color2
                }

                HUD.setThemeColor(color1, color2)
                HUD.updateEnumColor(clickedStyles)

                ClickGui.updateColors(color1, color2)
                return@forEachIndexed
            }
        }
    }
    override fun onKey(typedChar: Char, key: Int) {}
}