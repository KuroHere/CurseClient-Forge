package com.curseclient.client.gui.impl.clickgui.elements

import com.curseclient.client.gui.api.elements.InteractiveElement
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.gui.impl.clickgui.ClickGuiHud
import com.curseclient.client.gui.impl.clickgui.elements.settings.SettingButton
import com.curseclient.client.gui.impl.clickgui.elements.settings.impl.*
import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.client.ClickGui
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.setting.Setting
import com.curseclient.client.setting.type.*
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.math.MathUtils.toInt
import com.curseclient.client.utility.math.MathUtils.toIntSign
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.multAlpha
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.ScissorUtils.scissor
import com.curseclient.client.utility.render.animation.EaseUtils.ease
import com.curseclient.client.utility.render.animation.NewEaseType
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.graphic.GLUtils
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.*


class ModuleButton(val module: Module, var index: Int, var subOpen: Boolean, gui: ClickGuiHud, private val basePanel: CategoryPanel) : InteractiveElement(
    Vec2d.ZERO, 0.0, 0.0, gui) {

    override fun onRegister() {
        settings.add(BindButton(module, gui, this))
        settings.addAll(module.settings.mapNotNull { it.toGuiButton() })
        settings.forEach { it.onRegister() }
    }
    override fun onGuiOpen() = settings.forEach { it.onGuiOpen() }.also {
        subOpen = true
        extended = false
        renderHeight = 0.0
        hoverProgress = 0.0
        enabledProgress = module.isEnabled().toInt().toDouble()
    }

    override fun onGuiClose() = settings.forEach { it.onGuiClose() }
    override fun onGuiCloseAttempt() {}

    val settings = ArrayList<SettingButton>()
    private var prevHovered = false

    var extended = false
    var renderHeight = 0.0
    private var hoverProgress = 0.0
    private var enabledProgress = 0.0

    override fun onTick() {
        if (extended) settings.filter { it.isVisible() }.forEach { it.onTick() }
    }

    fun update() {
        val heightTo = settings.filter { it.isVisible() && extended }.sumOf { it.getSettingHeight() }
        renderHeight = lerp(renderHeight, heightTo, GLUtils.deltaTimeDouble() * 6.0 * ClickGui.settingsSpeed)
        if (abs(heightTo - renderHeight) < 0.5 && extended) renderHeight = heightTo
        if (renderHeight < 0.5 && !extended) renderHeight = 0.0

        hoverProgress += (hovered && basePanel.panelFocused).toIntSign().toDouble() * GLUtils.deltaTimeDouble() * 3.0 * ClickGui.hoverSpeed
        hoverProgress = clamp(hoverProgress, 0.0, 1.0)

        enabledProgress += module.isEnabled().toIntSign().toDouble() * GLUtils.deltaTimeDouble() * 8.0 * ClickGui.toggleSpeed
        enabledProgress = clamp(enabledProgress, 0.0, 1.0)
    }

    override fun onRender() {
        val p1 = pos.plus(1.0)
        val p2 = pos.plus(width, height + renderHeight).minus(1.0)

        val disabled = ClickGui.disabledColor

        val c1 = if (ClickGui.colorMode == ClickGui.ColorMode.Client)
            HUD.getColor(index)
        else if (ClickGui.pulse) ColorUtils.pulseColor(ClickGui.buttonColor1, 0, 1) else ClickGui.buttonColor1

        val c2 = when (ClickGui.colorMode) {
            ClickGui.ColorMode.Client -> HUD.getColor(index + 1)
            ClickGui.ColorMode.Static -> if (ClickGui.pulse) ColorUtils.pulseColor(ClickGui.buttonColor1, 0, 1) else ClickGui.buttonColor1
            else -> ClickGui.buttonColor2
        }

        val p = enabledProgress
        val a = ClickGui.buttonAlpha

        val buttonColor1 = ColorUtils.lerp(disabled, c1, p).multAlpha(a)
        val buttonColor2 = ColorUtils.lerp(disabled, c2, p).multAlpha(a)

        if (ClickGui.colorMode == ClickGui.ColorMode.Shader) {
            if (module.isEnabled())
            RenderUtils2D.rectGuiTexSmooth(
                pos.x.toFloat() + 0.5f,
                pos.y.toFloat() + 1,
                width.toFloat() - 0.5f,
                (height + renderHeight - 1).toFloat(),
                ClickGui.buttonRound.toFloat(), // too ugly
                Color.WHITE)
            else
                RectBuilder(p1, p2).apply {
                color(disabled)
                radius(ClickGui.buttonRound)
                draw()

            }
        } else
            RectBuilder(p1, p2).apply {
                if (ClickGui.colorMode == ClickGui.ColorMode.Horizontal)
                    colorH(buttonColor1, buttonColor2)
                else
                    colorV(buttonColor1, buttonColor2)
                radius(ClickGui.buttonRound)
                draw()

            }

        val textPos = pos.plus(ClickGui.space + hoverProgress.ease(NewEaseType.OutBack) * 2.0, height / 2.0)
        fr.drawString(module.name, textPos, scale = ClickGui.fontSize)

        detail()

        // 😼👌
        //if (hovered) {
        //    if (!prevHovered)
        //        SoundUtils.playSound { "scroll.wav" }
        //}
        //prevHovered = hovered;

        val hoveredModule = (gui as ClickGuiHud).panels.flatMap { it.modules }.firstOrNull { it.isHovered(gui.mouse) }
        val descriptionDisplay = gui.descriptionDisplay

        if (hoveredModule != null && descriptionDisplay != null) {
            descriptionDisplay.setDescription(hoveredModule.module.description)
            descriptionDisplay.setLocation(Vec2d(gui.mouse.x, gui.mouse.y))
            descriptionDisplay.setDraw(true)
        } else {
            descriptionDisplay?.resetAnimation()
            descriptionDisplay?.setDraw(false)
        }

        if (renderHeight < 1.0) return

        val sp1 = pos.minus(1.0)
        val sp2 = pos.plus(width, height + renderHeight).plus(1.0)
        val ss = gui.currentScale * 2.0

        scissor(Vec2d(sp1.x, max(sp1.y, basePanel.yRange.first)), Vec2d(sp2.x, min(sp2.y, basePanel.yRange.second)), ss) {
            drawSettings()
        }
    }

    private fun detail() {

        val bindPos = pos.plus(
            ClickGui.space.plus(hoverProgress.ease(NewEaseType.OutBack).times(2.0)) + fr.getStringWidth(module.name, ClickGui.settingFontSize) + fr.getStringWidth(Keyboard.getKeyName(module.key).toString(), 0.3) + 5,
            (height / 2) - 2)
        val charPos = pos.plus(width - 10, height / 2)

        val show_bind = ClickGui.detailPage == ClickGui.Details.Bind
        val show_characters = ClickGui.detailPage == ClickGui.Details.Characters

        var char: String? = null

        if (subOpen)
            char = ClickGui.open
        if (extended)
            char = ClickGui.close

        if (show_characters)
            if (module.settings.size > 1)
                fr.drawString(char.toString(), charPos, color = Color.WHITE, scale = 0.8)

        if (show_bind)
            if (module.key != Keyboard.KEY_NONE)
                fr.drawString("[" + Keyboard.getKeyName(module.key).toString() + "]", bindPos, shadow = true, color = Color.WHITE, scale = 0.6)
    }


    private fun drawSettings() {
        val x = pos.x
        var y = pos.y + height

        val p = pos.plus(0.0, height)

        // background
        val c = Color.BLACK.setAlpha(((1.0 - ClickGui.settingsBrightness) * 250.0).toInt())
        RectBuilder(p.plus(1.0, 0.0), p.plus(width - 1.0, renderHeight)).color(c).draw()

        // shadow
        RectBuilder(p.plus(1.0, 0.0), p.plus(width - 1.0, 5.0)).colorV(Color(0, 0, 0, 90), Color(0, 0, 0, 0)).draw()

        settings.forEachIndexed { index, it ->
            if (!it.isVisible()) it.onInvisible().also { return@forEachIndexed }
            it.pos = Vec2d(x, y)
            it.width = width
            it.height = it.getSettingHeight()
            it.index = index
            y += it.height
            it.onRender()
        }
    }

    override fun onMouseAction(action: MouseAction, button: Int) {
        if (extended) settings.filter { it.isVisible() }.forEach { it.onMouseAction(action, button) }

        if ((action == MouseAction.CLICK && !hovered) || action == MouseAction.RELEASE) return

        when (button) {
            0 -> module.toggle()
            1 -> {
                if (extended) {
                    extended = false
                } else {
                    (gui as ClickGuiHud).panels.forEach {
                        it.modules.forEach { module ->
                            if (module.extended) {
                                module.extended = false
                                module.settings.forEach { it.onSettingsClose() }
                            }
                        }
                    }
                    extended = true
                }
                settings.forEach { if (extended) it.onSettingsOpen() else it.onSettingsClose() }
            }
        }


    }

    override fun onKey(typedChar: Char, key: Int) {
        if (extended) settings.filter { it.isVisible() }.forEach { it.onKey(typedChar, key) }
    }

    fun getButtonHeight() = height + renderHeight

    private fun Setting<*>.toGuiButton(): SettingButton? {
        return when(this) {
            is BooleanSetting -> BooleanButton(this, gui, this@ModuleButton)
            is UnitSetting -> UnitButton(this, gui, this@ModuleButton)
            is DoubleSetting -> DoubleSlider(this, gui, this@ModuleButton)
            is StringSetting -> StringButton(this, gui, this@ModuleButton)
            is EnumSetting<*> -> EnumButton(this, gui, this@ModuleButton)
            is ColorSetting -> ColorPicker(this, gui, this@ModuleButton)
            else -> null
        }
    }
}