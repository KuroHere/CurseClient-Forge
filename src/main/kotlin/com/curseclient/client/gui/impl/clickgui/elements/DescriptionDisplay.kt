package com.curseclient.client.gui.impl.clickgui.elements

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.elements.InteractiveElement
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

class DescriptionDisplay(description: String, pos: Vec2d, gui: AbstractGui) : InteractiveElement(pos, 0.0, 0.0, gui) {
    private var description: String = description
    private var draw: Boolean = false

    init {
        this.width = fr.getStringWidth(this.description) + 4
        this.height = fr.getHeight() + 4
    }

    override fun onRender() {
        this.width = fr.getStringWidth(this.description) + 4
        this.height = fr.getHeight() + 4
        val mousePos = gui.mouse
        val pos1 = Vec2d(mousePos.x + 7, mousePos.y - 13)
        val pos2 = Vec2d(mousePos.x + 7 + fr.getStringWidth(this.description), mousePos.y + fr.getHeight() - 13)

        RectBuilder(pos1.minus(2.0, 0.0).minus(0.0, 4.0), pos2.plus(2.0, 0.0).minus(0.0, 2.0)).apply {
            outlineColor(Color.WHITE)
            width(0.5)
            color(Color.BLACK.setAlpha(180))
            draw()
        }

        fr.drawString(this.description, pos1, true, Color(-1) )
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

    override fun onMouseAction(action: MouseAction, button: Int) {
    }

    override fun onKey(typedChar: Char, key: Int) {
    }
}