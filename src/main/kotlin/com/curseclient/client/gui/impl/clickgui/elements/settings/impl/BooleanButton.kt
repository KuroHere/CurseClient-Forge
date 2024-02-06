package com.curseclient.client.gui.impl.clickgui.elements.settings.impl

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.gui.impl.clickgui.elements.ModuleButton
import com.curseclient.client.gui.impl.clickgui.elements.settings.SettingButton
import com.curseclient.client.module.impls.client.ClickGui
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.setting.type.BooleanSetting
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.math.MathUtils.toInt
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.graphic.GLUtils
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.shader.RectBuilder

class BooleanButton(val setting: BooleanSetting, gui: AbstractGui, baseButton: ModuleButton): SettingButton(gui, baseButton) {
    override fun onRegister() {}
    override fun onGuiOpen() {
        slideProgress = setting.value.toInt().toDouble()
    }
    override fun onGuiClose() {}
    override fun onGuiCloseAttempt() {}
    override fun onTick() {}

    private var slideProgress = 0.0

    override fun isVisible() = setting.isVisible

    override fun onRender() {
        super.onRender()
        fr.drawString(setting.name, pos.plus(ClickGui.space, height / 2.0), scale = ClickGui.settingFontSize)

        drawCheckBox(pos.plus(width - 7.5 - ClickGui.space, height / 2.0))
    }

    private fun drawCheckBox(p: Vec2d) {
        slideProgress = lerp(slideProgress, setting.value.toInt().toDouble(), GLUtils.deltaTimeDouble() * 5.0)

        val radius = 2.5

        val p1 = p.minus(radius * 2.0, radius).minus(2.0, 1.0)
        val p2 = p.plus(radius * 2.0, radius).plus(2.0, 1.0)

        // shadow
        RenderUtils2D.drawBlurredRect(p1, p2, 8, ClickGui.backgroundColor.setAlpha(150))

        // background
        RectBuilder(p1, p2).radius(15.0).draw()

        // circle
        val cp = p.minus(radius * 1.5, 0.0).plus(slideProgress * radius * 3.0, 0.0)

        val c = if (baseButton.module.isEnabled()) HUD.getColor(baseButton.index, ClickGui.settingsBrightness) else ClickGui.backgroundColor
        RectBuilder(cp.minus(radius), cp.plus(radius)).color(c).radius(radius).draw()
    }

    override fun onMouseAction(action: MouseAction, button: Int) {
        if (action != MouseAction.CLICK || button != 0 || !hovered) return
        setting.toggle()
    }
    override fun onKey(typedChar: Char, key: Int) {}
}