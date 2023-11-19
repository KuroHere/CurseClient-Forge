package com.curseclient.client.gui.impl.hudeditor.elements.settings.impl

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.gui.impl.clickgui.elements.ModuleButton
import com.curseclient.client.gui.impl.clickgui.elements.settings.SettingButton
import com.curseclient.client.gui.impl.hudeditor.elements.HudButton
import com.curseclient.client.gui.impl.hudeditor.elements.settings.SettingHudButton
import com.curseclient.client.module.modules.client.ClickGui
import com.curseclient.client.setting.type.UnitSetting
import com.curseclient.client.utility.render.font.FontUtils.drawString

class HudUnitButton(val setting: UnitSetting, gui: AbstractGui, baseButton: HudButton) : SettingHudButton(gui, baseButton) {
    override fun onRegister() {}
    override fun onGuiOpen() {}
    override fun onGuiClose() {}
    override fun onGuiCloseAttempt() {}
    override fun onTick() {}
    override fun onKey(typedChar: Char, key: Int) {}

    override fun isVisible() = setting.isVisible

    override fun onRender() {
        super.onRender()
        fr.drawString(setting.name, pos.plus(ClickGui.space, height / 2.0), scale = ClickGui.settingFontSize)
    }

    override fun onMouseAction(action: MouseAction, button: Int) {
        if (action != MouseAction.CLICK || button != 0 || !hovered) return
        setting.invokeBlock()
    }
}