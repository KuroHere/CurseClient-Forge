package com.curseclient.client.gui.impl.clickgui.elements.settings.misc

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.elements.InteractiveElement
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.module.impls.client.ClickGui
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.math.MathUtils.toInt
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.graphic.GLUtils
import com.curseclient.client.utility.render.shader.gradient.GradientUtil
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.shader.RoundedUtil.endBlend
import com.curseclient.client.utility.render.shader.RoundedUtil.startBlend
import com.curseclient.client.utility.render.shader.blur.KawaseBloom
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

class DescriptionDisplay(
    description: String,
    pos: Vec2d,
    gui: AbstractGui
) : InteractiveElement(pos, 0.0, 0.0, gui) {

    private var description: String = description
    private var draw: Boolean = false
    private var rectAlpha: Int = 0
    private var charsToShow: Int = 0
    private val charsPerFrame: Int = 1
    private var progress = 0.0

    override fun onRender() {
        val mousePos = gui.mouse

        val pos1 = Vec2d(mousePos.x + 7, mousePos.y - 12.5)
        val pos2 = Vec2d(mousePos.x + 10 + fr.getStringWidth(this.description), mousePos.y + fr.getHeight() - 15)

        progress = MathUtils.lerp(progress, draw.toInt().toDouble(), GLUtils.deltaTimeDouble() * 5.0 * ClickGui.settingsSpeed)

        val c1 = if (ClickGui.colorMode == ClickGui.ColorMode.Client)
            HUD.getColor(0)
        else if (ClickGui.pulse) ColorUtils.pulseColor(ClickGui.buttonColor1, 0, 1) else ClickGui.buttonColor1

        val c2 = when (ClickGui.colorMode) {
            ClickGui.ColorMode.Client -> HUD.getColor(5)
            ClickGui.ColorMode.Static -> if (ClickGui.pulse) ColorUtils.pulseColor(ClickGui.buttonColor1, 0, 1) else ClickGui.buttonColor1
            else -> ClickGui.buttonColor2
        }

        if (charsToShow < this.description.length) {
            charsToShow += charsPerFrame
            if (charsToShow > this.description.length) {
                charsToShow = this.description.length
            }
        }

        if (rectAlpha < 200) {
            rectAlpha += 3
        }

        KawaseBloom.glBloom({
            GlStateManager.pushMatrix()
            RectBuilder(pos1.minus(2.0, 4.0), pos2.times(progress, 1.0)).apply {
                color(c1.setAlpha(rectAlpha), c1.setAlpha(rectAlpha), c2.setAlpha(rectAlpha), c2.setAlpha(rectAlpha))
                radius(3.0)
                draw()
            }
            GlStateManager.popMatrix()
        }, 2, 3)

        GlStateManager.pushMatrix()
        startBlend()
        RectBuilder(pos1.minus(2.0, 4.0), pos2.times(progress, 1.0)).apply {
            outlineColor(c1, c1, c2, c2)
            width(1.0)
            color(Color.BLACK.setAlpha(rectAlpha))
            radius(3.0)
            draw()
        }

        val animatedDescription = if (charsToShow > this.description.length) {
            this.description
        } else {
            this.description.substring(0, charsToShow)
        }
        GradientUtil.applyGradientCornerRL(pos1.x.toFloat(), pos1.y.toFloat(), fr.getStringWidth(animatedDescription).toFloat(), fr.getHeight().toFloat(), 1f, c1, c2) {
            fr.drawString(animatedDescription, pos1, false, Color(-1))
        }
        endBlend()
        GlStateManager.popMatrix()
    }

    fun resetAnimation() {
        charsToShow = 0
        progress = 0.0
        rectAlpha = 0
    }

    fun shouldDraw(): Boolean {
        return this.draw
    }

    fun getDescription(): String {
        return this.description
    }

    fun setDescription(description: String) {
        this.description = description
    }

    fun setDraw(draw: Boolean) {
        this.draw = draw
    }

    override fun onRegister() {
    }

    override fun onGuiOpen() {
    }

    override fun onGuiClose() {
    }

    override fun onGuiCloseAttempt() {
    }

    override fun onTick() {
    }

    override fun onMouseAction(action: MouseAction, button: Int) {}

    override fun onKey(typedChar: Char, key: Int) {}
}