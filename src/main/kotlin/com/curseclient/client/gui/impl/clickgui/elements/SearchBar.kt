package com.curseclient.client.gui.impl.clickgui.elements

import baritone.api.utils.Helper.mc
import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.elements.InteractiveElement
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.vector.Vec2d
import org.lwjgl.input.Keyboard
import java.awt.Color

class SearchBar(moduleName: String, pos: Vec2d, gui: AbstractGui) : InteractiveElement(pos, 0.0, 0.0, gui) {

    private var moduleName: String = moduleName
    private var listening: Boolean = false

    override fun onRender() {
        val fr = Fonts.DEFAULT
        val textToAdd = if (mc.player == null || System.currentTimeMillis() / 10000 % 2 == 0L) " " else "_"
        val finalText = moduleName + textToAdd

        RenderUtils2D.drawRoundedRect((pos.x ).toFloat(), pos.y.toFloat(), (width / 2).toFloat(), 20f, 5f, Color(45, 45, 45 ,200).rgb)
        if (!listening)
            fr.drawString("Search...", Vec2d(pos.x, height - fr.getHeight(2.0)), scale = 2.0)
        else
        fr.drawString(finalText, Vec2d(pos.x, height - fr.getHeight(2.0)), false, scale = 2.0)
    }

    override fun onRegister() {}

    override fun onGuiOpen() {}

    override fun onGuiClose() {}

    override fun onGuiCloseAttempt() {}

    override fun onTick() {}

    fun shouldListening(): Boolean {
        return this.listening
    }

    fun setListening(listening: Boolean) {
        this.listening = listening
    }

    override fun onMouseAction(action: MouseAction, button: Int) {
        if (action != MouseAction.CLICK || button != 0 || !hovered) return
        if (isHovered(Vec2d(pos.x, pos.y)))
            listening = true
        else {
            moduleName = "";
            listening = false;
        }
    }

    fun setModuleName(moduleName: String) {
        this.moduleName = moduleName
    }

    override fun onKey(typedChar: Char, key: Int) {
        if (listening) {
            when (key) {
                Keyboard.KEY_ESCAPE -> {
                    listening = false
                    moduleName = ""
                    return
                }

                Keyboard.KEY_RETURN -> {
                    return
                }

                Keyboard.KEY_BACK -> {
                    moduleName = removeLastChar(moduleName)
                    return
                }

                Keyboard.KEY_SPACE -> {
                    moduleName = "$moduleName "
                    return
                }
            }
            if (Keyboard.getKeyName(0) == null) return
            moduleName += Keyboard.getKeyName(0)
        }
    }

    private fun removeLastChar(str: String?): String {
        var output = ""
        if (!str.isNullOrEmpty()) {
            output = str.substring(0, str.length - 1)
        }
        return output
    }
}