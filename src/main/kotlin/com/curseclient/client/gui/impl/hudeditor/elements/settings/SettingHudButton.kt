package com.curseclient.client.gui.impl.hudeditor.elements.settings

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.elements.InteractiveElement
import com.curseclient.client.gui.impl.hudeditor.elements.HudButton
import com.curseclient.client.module.impls.client.ClickGui
import com.curseclient.client.utility.render.vector.Vec2d

abstract class SettingHudButton(gui: AbstractGui, val baseButton: HudButton) : InteractiveElement(Vec2d.ZERO, 0.0, 0.0, gui) {
    var index = 0
    open fun onSettingsOpen() {}
    open fun onSettingsClose() {}
    override fun onRender() {}

    open fun onInvisible() {}

    open fun isVisible() = true
    open fun getSettingHeight() = ClickGui.height
}