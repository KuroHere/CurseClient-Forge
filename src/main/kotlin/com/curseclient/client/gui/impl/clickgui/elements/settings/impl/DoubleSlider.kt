package com.curseclient.client.gui.impl.clickgui.elements.settings.impl

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.gui.impl.clickgui.elements.ModuleButton
import com.curseclient.client.gui.impl.clickgui.elements.settings.SettingButton
import com.curseclient.client.module.modules.client.ClickGui
import com.curseclient.client.setting.type.DoubleSetting
import com.curseclient.client.utility.extension.transformIf
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.math.MathUtils.decimalPlaces
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.math.MathUtils.roundToPlaces
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.render.graphic.GLUtils
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.shader.RectBuilder
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.abs
import kotlin.math.round

class DoubleSlider(val setting: DoubleSetting, gui: AbstractGui, baseButton: ModuleButton) : SettingButton(gui, baseButton) {
    override fun onRegister() {}
    override fun onGuiClose() {}
    override fun onGuiCloseAttempt() {}
    override fun onTick() {}

    override fun onGuiOpen() = reset()
    override fun onSettingsOpen() = reset()
    override fun onSettingsClose() = reset()
    override fun onInvisible() = reset()
    override fun isVisible() = setting.isVisible

    private var renderProgress = 0.0
    private var sliding = false
    var typing = false
    private var typed = ""

    private val formattedName get() =
        setting.value.toString().transformIf(setting.step % 1 == 0.0) { it.dropLast(2) }

    override fun onRender() {
        super.onRender()

        if (!isVisible()) reset().also { return }
        if (!hovered || typing) sliding = false

        val startX = pos.x + ClickGui.space
        val endX = pos.x + width - ClickGui.space

        val sliderStartX = pos.x + 4
        val sliderEndX = pos.x + width - 4

        val mouseProgress = (gui.mouse.x - sliderStartX) / (sliderEndX - sliderStartX)

        if (sliding) {
            val rawValue = lerp(setting.min, setting.max, mouseProgress)
            var valueRounded = round(rawValue / setting.step) * setting.step
            valueRounded = valueRounded.roundToPlaces(decimalPlaces(setting.step))
            if (abs(valueRounded) == 0.0) valueRounded = 0.0
            setting.value = clamp(valueRounded, setting.min, setting.max)
            setting.listeners.forEach { it() }
        }

        val centerY = pos.y + height / 2.0 - 4

        val renderProgressTo = clamp((setting.value - setting.min) / (setting.max - setting.min), 0.0, 1.0)
        renderProgress = lerp(renderProgress, renderProgressTo, GLUtils.deltaTimeDouble() * 3.0)

        val sliderBegin = Vec2d(sliderStartX, centerY + 7)
        val sliderEnd = Vec2d(lerp(sliderStartX, sliderEndX, renderProgress), centerY + 9)
        val sliderFull = Vec2d(sliderEndX, centerY + 9)

        if (typing) {
            val text = "${setting.name}: $typed"
            fr.drawString(text, Vec2d(startX, centerY), scale = ClickGui.settingFontSize)
            return
        } else {
            //if (sliding) {
            RectBuilder(sliderBegin, sliderFull).color(Color(150, 150, 150, 110)).radius(2.3).draw()
            RectBuilder(sliderBegin, sliderEnd).color(Color(210, 210, 210)).radius(2.3).draw()
            RectBuilder(sliderEnd.minus(3.0, 3.0), sliderEnd.plus(1.0, 1.0)).color(Color(210, 210, 210)).radius(1.8).draw()

            val text1 = setting.name
            val text2 = formattedName

            fr.drawString(text1, Vec2d(startX, centerY), scale = ClickGui.settingFontSize)
            fr.drawString(text2, Vec2d(endX - fr.getStringWidth(text2, ClickGui.settingFontSize), centerY), scale = ClickGui.settingFontSize)

        }
    }

    override fun onMouseAction(action: MouseAction, button: Int) {
        if (action == MouseAction.CLICK && button == 1 && hovered) {
            baseButton.settings.filterIsInstance<StringButton>().forEach { it.applyTyped() }
            baseButton.settings.filterIsInstance<DoubleSlider>().forEach { it.applyTyped() }
            typing = true
            typed = formattedName
        }

        sliding = action == MouseAction.CLICK && button == 0 && hovered && !typing

        if (!hovered && action == MouseAction.CLICK) cancelTyping()
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
                if (!"1234567890,.-".toCharArray().contains(typedChar)) return
                typed += typedChar
            }
        }
    }

    private fun reset() {
        sliding = false
        cancelTyping()
    }

    fun applyTyped() {
        if (!typing) return
        typed.toDoubleOrNull()?.let {
            var valueRounded = round(it / setting.step) * setting.step
            valueRounded = valueRounded.roundToPlaces(decimalPlaces(setting.step))
            if (abs(valueRounded) == 0.0) valueRounded = 0.0
            setting.value = clamp(valueRounded, setting.min, setting.max)

            setting.listeners.forEach { it() }
        }
        cancelTyping()
    }

    private fun cancelTyping() {
        typing = false
        typed = setting.value.toString()
    }
}