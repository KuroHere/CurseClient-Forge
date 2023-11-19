package com.curseclient.client.gui.impl.hudeditor.elements.settings.impl

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.elements.InteractiveElement
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.gui.impl.clickgui.elements.ModuleButton
import com.curseclient.client.gui.impl.clickgui.elements.settings.SettingButton
import com.curseclient.client.gui.impl.hudeditor.elements.HudButton
import com.curseclient.client.gui.impl.hudeditor.elements.settings.SettingHudButton
import com.curseclient.client.module.modules.client.ClickGui
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.setting.type.EnumSetting
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.math.MathUtils.toInt
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.graphic.GLUtils
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import java.awt.Color

class HudEnumButton(val setting: EnumSetting<*>, gui: AbstractGui, baseButton: HudButton) : SettingHudButton(gui, baseButton) {
    override fun onRegister() {
        modes.addAll(setting.names.map { ModeButton(it, this, gui) })
    }
    override fun onGuiOpen() {}
    override fun onGuiClose() {}
    override fun onGuiCloseAttempt() {}
    override fun onTick() {}
    override fun onKey(typedChar: Char, key: Int) {}
    override fun onSettingsOpen() { extended = false; progress = 0.0 }
    override fun onInvisible() { extended = false; progress = 0.0 }

    private val modes = ArrayList<ModeButton>()
    private var extended = false
    private var progress = 0.0

    override fun isVisible() = setting.isVisible

    override fun onRender() {
        super.onRender()
        progress = lerp(progress, extended.toInt().toDouble(), GLUtils.deltaTimeDouble() * 5.0 * ClickGui.settingsSpeed)

        if (progress > 0.05)
            RenderUtils2D.drawRect(pos, pos.plus(width, height * progress), Color.BLACK.setAlpha((50 * progress).toInt()))

        val h = ClickGui.height
        val x1 = pos.x + ClickGui.space
        val x2 = pos.x + width / 2.0 - fr.getStringWidth(setting.name, ClickGui.settingFontSize) / 2.0
        fr.drawString(setting.name, Vec2d(lerp(x1, x2, progress), pos.y + h / 2.0), scale = ClickGui.settingFontSize)

        if (!extended) {
            val textPos = pos.plus(width - ClickGui.space - fr.getStringWidth(setting.valueName, ClickGui.settingFontSize), h / 2.0)
            fr.drawString(setting.valueName, textPos, scale = ClickGui.settingFontSize)
            return
        }

        val x = pos.x
        var y = pos.y + h
        modes.forEach {
            it.pos = Vec2d(x, y)
            it.width = width
            y += it.height
            it.onRender()
        }
    }

    override fun onMouseAction(action: MouseAction, button: Int) {
        if (!hovered || action == MouseAction.RELEASE) return
        when(button) {
            0 -> {
                if (extended)
                    modes.forEach { it.onMouseAction(action, button) }
                else
                    setting.next()
            }
            1 -> { extended = !extended }
        }
    }

    override fun getSettingHeight() =
        ClickGui.height + modes.sumOf { it.height } * extended.toInt().toDouble()

    private class ModeButton(val name: String, val baseButton: HudEnumButton, gui: AbstractGui) : InteractiveElement(Vec2d.ZERO, 0.0, 10.0, gui) {
        override fun onRegister() {}
        override fun onGuiOpen() {}
        override fun onGuiClose() {}
        override fun onGuiCloseAttempt() {}
        override fun onKey(typedChar: Char, key: Int) {}
        override fun onTick() {}

        override fun onRender() {
            val displayName = name
            val textPos = pos.plus(width / 2.0 - fr.getStringWidth(displayName, ClickGui.settingFontSize) / 2.0, height / 2.0)
            val c = HUD.getColor(baseButton.index, ClickGui.settingsBrightness)
            val color = if (baseButton.setting.valueName == name) c else Color.WHITE
            fr.drawString(displayName, textPos, color = color, scale = ClickGui.settingFontSize)
        }

        override fun onMouseAction(action: MouseAction, button: Int) {
            if (!hovered) return
            baseButton.setting.setByName(name)
            baseButton.setting.listeners.forEach { it() }
        }
    }
}