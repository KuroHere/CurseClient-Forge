package com.curseclient.client.gui.impl.clickgui.elements.settings.misc

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.elements.InteractiveElement
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.module.modules.client.ClickGui
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.shader.BlurUtil
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import java.awt.Color

// TODO: ResetAnimation not actually work well, need fix
class DescriptionDisplay(description: String, pos: Vec2d, gui: AbstractGui) : InteractiveElement(pos, 0.0, 0.0, gui) {
    private var description: String = description
    private var draw: Boolean = false

    private var rectAlpha: Int = 0

    private var charsToShow: Int = 0
    private val charsPerFrame: Int = 1

    override fun onRender() {
        val mousePos = gui.mouse

        val pos1 = Vec2d(mousePos.x + 7, mousePos.y - 13)
        val pos2 = Vec2d(mousePos.x + 10 + fr.getStringWidth(this.description), mousePos.y + fr.getHeight() - 15)

        if (charsToShow < this.description.length) {
            charsToShow += charsPerFrame
            if (charsToShow > this.description.length) {
                charsToShow = this.description.length
            }
        }

        if (rectAlpha < 180) {
            rectAlpha += 5
        }
        val c1 = if (ClickGui.colorMode == ClickGui.ColorMode.Client)
            HUD.getColor(0)
        else if (ClickGui.pulse) ColorUtils.pulseColor(ClickGui.buttonColor1, 0, 1) else ClickGui.buttonColor1

        BlurUtil.drawBlurryRect(
            (pos1.x - 2.0).toFloat(),
            (pos1.y - 4.0).toFloat(),
            pos2.x.toFloat(),
            pos2.y.toFloat(),
            15f,
            Color(c1.setAlpha(10).rgb)
        )

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