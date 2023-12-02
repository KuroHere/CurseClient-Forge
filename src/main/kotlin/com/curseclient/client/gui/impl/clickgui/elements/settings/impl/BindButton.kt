package com.curseclient.client.gui.impl.clickgui.elements.settings.impl

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.gui.impl.clickgui.elements.ModuleButton
import com.curseclient.client.gui.impl.clickgui.elements.settings.SettingButton
import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.client.ClickGui
import com.curseclient.client.utility.player.ChatUtils
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import org.lwjgl.input.Keyboard
import java.awt.Color

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
        fr.drawString("Binding:", pos.plus(ClickGui.space, height / 2.0), scale = ClickGui.settingFontSize)

        val text = if (binding) "..." else Keyboard.getKeyName(module.key)
        val color = if (binding) Color.GREEN else Color.WHITE
        fr.drawString(text, pos.plus(width - fr.getStringWidth(text, ClickGui.settingFontSize) - ClickGui.space, height / 2.0), color = color, scale = ClickGui.settingFontSize)
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