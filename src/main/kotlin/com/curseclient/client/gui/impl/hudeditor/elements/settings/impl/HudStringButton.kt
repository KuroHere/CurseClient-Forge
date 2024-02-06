package com.curseclient.client.gui.impl.hudeditor.elements.settings.impl

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.gui.impl.hudeditor.elements.HudButton
import com.curseclient.client.gui.impl.hudeditor.elements.settings.SettingHudButton
import com.curseclient.client.module.impls.client.ClickGui
import com.curseclient.client.setting.type.StringSetting
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import org.lwjgl.input.Keyboard

class HudStringButton(val setting: StringSetting, gui: AbstractGui, baseButton: HudButton) : SettingHudButton(gui, baseButton) {
    override fun onRegister() {}
    override fun onGuiCloseAttempt() {}
    override fun onTick() {}

    override fun onInvisible() = cancelTyping()
    override fun onGuiOpen() = cancelTyping()
    override fun onGuiClose() = cancelTyping()
    override fun onSettingsOpen() = cancelTyping()
    override fun onSettingsClose() = cancelTyping()

    override fun isVisible() = setting.isVisible

    private var typing = false
    private var typed = ""

    override fun onRender() {
        super.onRender()
        val text = if (typing) "${setting.name}: $typed" else setting.name
        fr.drawString(text, pos.plus(ClickGui.space, height / 2.0), scale = ClickGui.settingFontSize)
        if (!typing) fr.drawString(setting.value, pos.plus(width - fr.getStringWidth(setting.value, ClickGui.settingFontSize) - ClickGui.space, height / 2.0), scale = ClickGui.settingFontSize)
    }

    override fun onMouseAction(action: MouseAction, button: Int) {
        if (action != MouseAction.CLICK || !hovered) return
        baseButton.settings.filterIsInstance<HudStringButton>().forEach { it.applyTyped() }
        baseButton.settings.filterIsInstance<HudDoubleSlider>().forEach { it.applyTyped() }
        typing = true
        typed = setting.value
    }

    override fun onKey(typedChar: Char, key: Int) {
        when (key) {
            Keyboard.KEY_ESCAPE -> {
                cancelTyping()
            }

            Keyboard.KEY_RETURN -> {
                applyTyped()
            }

            Keyboard.KEY_BACK -> { typed = typed.dropLast(1) }

            else -> {
                if (!typedChar.isLetter()) return
                typed += typedChar
            }
        }
    }

    fun applyTyped() {
        if (!typing) return
        setting.value = typed
        setting.listeners.forEach { it() }
        cancelTyping()
    }

    private fun cancelTyping() {
        typing = false
    }
}