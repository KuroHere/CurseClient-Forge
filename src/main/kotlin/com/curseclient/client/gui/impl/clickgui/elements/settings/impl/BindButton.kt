package com.curseclient.client.gui.impl.clickgui.elements.settings.impl

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.gui.impl.clickgui.elements.ModuleButton
import com.curseclient.client.gui.impl.clickgui.elements.settings.SettingButton
import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.client.ClickGui
import com.curseclient.client.utility.player.ChatUtils
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.Font
import java.util.*

class BindButton(val module: Module, gui: AbstractGui, baseButton: ModuleButton) : SettingButton(gui, baseButton) {
    override fun onRegister() {}
    override fun onGuiOpen() { binding = false }
    override fun onGuiClose() {}
    override fun onGuiCloseAttempt() {}
    override fun onTick() {}

    override fun onInvisible() { binding = false }

    private var binding = false

    override fun onRender() {
        super.onRender()
        fr.drawString("Bind", pos.plus(ClickGui.space, height / 2.0), scale = ClickGui.settingFontSize)

        val text = if (binding) "..." else Keyboard.getKeyName(module.key)
        val color = Color.WHITE
        val textWidth = Fonts.DEFAULT_BOLD.getStringWidth(text, ClickGui.settingFontSize)
        val textHeight = Fonts.DEFAULT_BOLD.getHeight(ClickGui.settingFontSize)
        val buttonHeight = ClickGui.height

        val rectStart = Vec2d(pos.x + width - textWidth - ClickGui.space - 3, pos.y - 6 + buttonHeight - textHeight)
        val rectEnd = Vec2d(pos.x + width - ClickGui.space, pos.y + buttonHeight - 4)

        RectBuilder(rectStart, rectEnd).apply {
            color(ClickGui.disabledColor)
            radius(2.3)
            draw()
        }

        Fonts.DEFAULT_BOLD.drawString(
            text.uppercase(Locale.getDefault()),
            pos.plus(width - textWidth - ClickGui.space - 2, buttonHeight / 2.0),
            color = color,
            scale = ClickGui.settingFontSize
        )
    }

    override fun onMouseAction(action: MouseAction, button: Int) {
        if (action != MouseAction.CLICK || button != 0 || !hovered) return
        binding = !binding
    }

    override fun onKey(typedChar: Char, key: Int) {
        if (!binding) return

        if (key == module.key || key == Keyboard.KEY_ESCAPE) {
            binding = false
            return
        }

        if (key == Keyboard.KEY_BACK || key == Keyboard.KEY_DELETE) {
            module.key = Keyboard.KEY_NONE
            ChatUtils.sendMessage("New " + module.name + " bind: §a[§f NONE §a]§f")

            binding = false
            return
        }

        module.key = key
        ChatUtils.sendMessage("New " + module.name + " bind: §a[§f " + Keyboard.getKeyName(module.key) + " §a]§f")
        binding = false
    }
}