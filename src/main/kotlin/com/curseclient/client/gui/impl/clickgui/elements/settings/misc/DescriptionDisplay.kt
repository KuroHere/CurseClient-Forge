package com.curseclient.client.gui.impl.clickgui.elements.settings.misc

import baritone.api.utils.Helper
import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.elements.InteractiveElement
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.module.Category
import com.curseclient.client.module.modules.client.ClickGui
import com.curseclient.client.module.modules.hud.NewOverlay
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.font.BonIcon
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.graphic.GLUtils
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import com.jhlabs.image.ImageMath
import com.jhlabs.image.ImageMath.lerp
import java.awt.Color

// TODO: ResetAnimation not actually work well, need fix
// I'm not too good about making animation, but I love it
class DescriptionDisplay(description: String, pos: Vec2d, gui: AbstractGui) : InteractiveElement(pos, 0.0, 0.0, gui) {
    private var description: String = description
    private var draw: Boolean = false

    private var rectAlpha: Int = 0

    private var charsToShow: Int = 0
    private val charsPerFrame: Int = 1

    override fun onRender() {
        val mousePos = gui.mouse

        val pos1 = Vec2d(mousePos.x + 7, mousePos.y - 13)
        val pos2 = Vec2d(mousePos.x + 7 + fr.getStringWidth(this.description), mousePos.y + fr.getHeight() - 13)

        if (charsToShow < this.description.length) {
            charsToShow += charsPerFrame
            if (charsToShow > this.description.length) {
                charsToShow = this.description.length
            }
        }

        if (rectAlpha < 180) {
            rectAlpha += 5
        }

        RectBuilder(pos1.minus(2.0, 0.0).minus(0.0, 4.0), pos2.plus(2.0, 0.0).minus(0.0, 2.0)).apply {
            outlineColor(Color.WHITE.setAlpha(rectAlpha))
            width(0.5)
            color(Color.BLACK.setAlpha(rectAlpha))
            radius(1.5)
            draw()
        }

        val animatedDescription = if (charsToShow > this.description.length) {
            this.description
        } else {
            this.description.substring(0, charsToShow)
        }
        fr.drawString(animatedDescription, pos1, true, Color(-1))
    }

    fun resetAnimation() {
        charsToShow = 0
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