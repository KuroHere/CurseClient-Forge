package com.curseclient.client.gui.impl.hudeditor.elements.settings.impl

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.gui.impl.clickgui.elements.ModuleButton
import com.curseclient.client.gui.impl.clickgui.elements.settings.SettingButton
import com.curseclient.client.gui.impl.hudeditor.elements.HudButton
import com.curseclient.client.gui.impl.hudeditor.elements.settings.SettingHudButton
import com.curseclient.client.module.modules.client.ClickGui
import com.curseclient.client.setting.type.ColorSetting
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.math.MathUtils.normalize
import com.curseclient.client.utility.math.MathUtils.toInt
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.glColor
import com.curseclient.client.utility.render.ColorUtils.setAlphaD
import com.curseclient.client.utility.render.graphic.GLUtils
import com.curseclient.client.utility.render.graphic.GLUtils.prepareGL2D
import com.curseclient.client.utility.render.graphic.GLUtils.releaseGL2D
import com.curseclient.client.utility.render.HoverUtils
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.shader.RectBuilder
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.max

class HudColorPicker(val setting: ColorSetting, gui: AbstractGui, baseButton: HudButton): SettingHudButton(gui, baseButton) {
    override fun onRegister() {}
    override fun onGuiOpen() {
        extended = false
        progress = 0.0
        resetDragging()
    }
    override fun onGuiClose() {
        extended = false
        progress = 0.0
        resetDragging()
    }
    override fun onGuiCloseAttempt() {}
    override fun onSettingsOpen() {
        extended = false
        progress = 0.0
        resetDragging()
    }
    override fun onSettingsClose() {
        extended = false
        resetDragging()
    }
    override fun onTick() {}
    override fun isVisible() =
        setting.isVisible

    private var extended = false
    private var progress = 0.0

    private val space = 3.0
    private val sliderWidth = 2.0

    private var pickerPosition = Vec2d.ZERO to Vec2d.ZERO
    private var pickerSliderPosition = Vec2d.ZERO
    private var pickerHovered = false
    private var pickerDragging = false

    private var huePosition = Vec2d.ZERO to Vec2d.ZERO
    private var hueSliderPosition = Vec2d.ZERO
    private var hueHovered = false
    private var hueDragging = false

    private var alphaPosition = Vec2d.ZERO to Vec2d.ZERO
    private var alphaSliderPosition = Vec2d.ZERO
    private var alphaHovered = false
    private var alphaDragging = false

    override fun onRender() {
        progress = lerp(progress, extended.toInt().toDouble(), GLUtils.deltaTimeDouble() * 5.0 * ClickGui.settingsSpeed)
        val sliderProgress = normalize(max(progress - 0.6, 0.0), 0.0, 0.6, 0.0, 1.0)

        if (!extended) resetDragging()

        when {
            pickerDragging -> {
                val saturationProgress = normalize(gui.mouse.x, pickerPosition.first.x, pickerPosition.second.x, 0.0, 1.0)
                val brightnessProgress = normalize(gui.mouse.y, pickerPosition.first.y, pickerPosition.second.y, 1.0, 0.0)
                setting.saturation = saturationProgress.toFloat()
                setting.brightness = brightnessProgress.toFloat()
            }

            hueDragging -> {
                val hueProgress = normalize(gui.mouse.y, huePosition.first.y, huePosition.second.y, 0.0, 1.0)
                setting.hue = hueProgress.toFloat()
            }

            alphaDragging -> {
                val alphaProgress = normalize(gui.mouse.y, alphaPosition.first.y, alphaPosition.second.y, 1.0, 0.0)
                setting.alpha = alphaProgress.toFloat()
            }
        }

        val rawColor = Color.getHSBColor(setting.hue, 1f, 1f).setAlphaD(progress)
        // Background
        RectBuilder(pos.plus(2.0), pos.plus(width, height).minus(2.0)).apply {
            color(Color.BLACK.setAlphaD(progress * 0.2))
            radius(ClickGui.buttonRound * progress)
            draw()
        }

        // ColorPicker
        val pickerFrom = pos.plus(width - ClickGui.height, 0.0).plus(space) to pos.plus(width, ClickGui.height).minus(space)
        val pickerTo = pos.plus(0.0, ClickGui.height).plus(space * 2.0) to pos.plus(0.0, ClickGui.height).plus(pickerWidth).minus(space * 2.0)
        pickerPosition = lerp(pickerFrom.first, pickerTo.first, progress) to lerp(pickerFrom.second, pickerTo.second, progress)
        val pickerSliderPositionX = lerp(pickerPosition.first.x, pickerPosition.second.x, setting.saturation.toDouble())
        val pickerSliderPositionY = lerp(pickerPosition.second.y, pickerPosition.first.y, setting.brightness.toDouble())
        pickerSliderPosition = lerp(pickerSliderPosition, Vec2d(pickerSliderPositionX, pickerSliderPositionY), GLUtils.deltaTimeDouble() * 10.0 * ClickGui.settingsSpeed * sliderProgress)
        pickerSliderPosition = Vec2d(clamp(pickerSliderPosition.x, pickerPosition.first.x, pickerPosition.second.x), clamp(pickerSliderPosition.y, pickerPosition.first.y, pickerPosition.second.y))
        pickerHovered = HoverUtils.isHovered(gui.mouse, pickerPosition.first, pickerPosition.second)

        RectBuilder(pickerPosition.first, pickerPosition.second).apply {
            val c1 = ColorUtils.lerp(setting.getColor(), Color.WHITE, progress)
            val c2 = ColorUtils.lerp(setting.getColor(), Color.BLACK, progress)
            val c3 = ColorUtils.lerp(setting.getColor(), rawColor.setAlphaD(1.0), progress)

            color(c1, c3, c2, c2)
            radius(ClickGui.buttonRound + (1.0 - progress) * 10.0)
            outlineColor(ClickGui.backgroundColor.setAlphaD(1.0 - progress))
            width(1.0 - progress)
            draw()
        }

        RectBuilder(pickerSliderPosition.minus(2.0), pickerSliderPosition.plus(2.0)).apply {
            color(Color.getHSBColor(setting.hue, setting.saturation, setting.brightness).setAlphaD(progress))
            radius(100.0)
            outlineColor(ClickGui.backgroundColor.setAlphaD(sliderProgress))
            width(sliderProgress)
            draw()
        }

        val pickerEnd = pos.x + pickerWidth
        val rightCenter = lerp(pickerEnd, pos.x + width, 0.5)

        // Hue
        val hueX = lerp(pickerEnd, rightCenter, 0.5)
        huePosition = Vec2d(hueX - sliderWidth, pickerPosition.first.y) to Vec2d(hueX + sliderWidth, pickerPosition.second.y)
        val hueMin = huePosition.first.y
        val hueMax = huePosition.second.y
        val hueSliderY = lerp(hueMin, hueMax, setting.hue.toDouble())
        hueSliderPosition = Vec2d(hueX, clamp(lerp(hueSliderPosition.y, hueSliderY, GLUtils.deltaTimeDouble() * 5.0 * ClickGui.settingsSpeed * sliderProgress), hueMin, hueMax))
        hueHovered = HoverUtils.isHovered(gui.mouse, huePosition.first.minus(1.0), huePosition.second.plus(1.0))

        drawHueLine(huePosition.first, huePosition.second, sliderProgress)
        RectBuilder(hueSliderPosition.minus(4.0, 2.0), hueSliderPosition.plus(4.0, 2.0)).apply {
            outlineColor(ClickGui.backgroundColor.setAlphaD(sliderProgress))
            width(sliderProgress)

            color(Color.BLACK.setAlphaD(0.0))
            draw()
        }

        // Alpha
        val alphaX = lerp(rightCenter, pos.x + width, 0.5)
        alphaPosition = Vec2d(alphaX - sliderWidth, pickerPosition.first.y) to Vec2d(alphaX + sliderWidth, pickerPosition.second.y)
        val alphaMin = alphaPosition.second.y
        val alphaMax = alphaPosition.first.y
        val alphaSliderY = lerp(alphaMax, alphaMin, (1f - setting.alpha).toDouble())
        alphaSliderPosition = Vec2d(alphaX, clamp(lerp(alphaSliderPosition.y, alphaSliderY, GLUtils.deltaTimeDouble() * 5.0 * ClickGui.settingsSpeed * sliderProgress), alphaMax, alphaMin))
        alphaHovered = HoverUtils.isHovered(gui.mouse, alphaPosition.first.minus(1.0), alphaPosition.second.plus(1.0))

        RectBuilder(alphaPosition.first, alphaPosition.second).apply {
            colorV(Color.WHITE.setAlphaD(sliderProgress), Color.BLACK.setAlphaD(sliderProgress))
            draw()
        }

        RectBuilder(alphaSliderPosition.minus(4.0, 2.0), alphaSliderPosition.plus(4.0, 2.0)).apply {
            outlineColor(ClickGui.backgroundColor.setAlphaD(sliderProgress))
            width(sliderProgress)

            color(Color.BLACK.setAlphaD(0.0))
            draw()
        }

        // Setting name
        fr.drawString(setting.name, pos.plus(ClickGui.space, ClickGui.height * 0.5), scale = ClickGui.settingFontSize)
    }

    private val hueColors by lazy {
        arrayListOf<Color>().apply {
            for (i in 0..36) {
                val hue = i.toFloat() / 36f
                add(Color.getHSBColor(hue, 1f, 1f))
            }
        }
    }

    private fun drawHueLine(pos1: Vec2d, pos2: Vec2d, alpha: Double) {
        prepareGL2D()

        for (i in 0 until hueColors.size) {
            val width = pos2.x - pos1.x
            val height = (pos2.y - pos1.y) / hueColors.size

            val x = pos1.x
            val y = pos1.y + height * i

            val c1 = hueColors[i].setAlphaD(alpha)
            val c2 = hueColors.getOrNull(i + 1)?.setAlphaD(alpha) ?: c1

            glBegin(GL_QUADS)

            c1.glColor()
            glVertex2d(x, y)
            glVertex2d(x + width, y)

            c2.glColor()
            glVertex2d(x + width, y + height)
            glVertex2d(x, y + height)

            glEnd()
        }

        GlStateManager.resetColor()
        releaseGL2D()
    }

    private val pickerWidth get() = ClickGui.width * 0.75

    override fun getSettingHeight() =
        ClickGui.height + progress * pickerWidth

    override fun onMouseAction(action: MouseAction, button: Int) {
        if (action == MouseAction.CLICK) {
            if (!hovered) return
            when(button) {
                1 -> {
                    extended = !extended
                }

                0 -> {
                    pickerDragging = pickerHovered
                    hueDragging = hueHovered
                    alphaDragging = alphaHovered
                }
            }
        } else resetDragging()
    }

    private fun resetDragging() {
        pickerDragging = false
        hueDragging = false
        alphaDragging = false
    }

    override fun onKey(typedChar: Char, key: Int) {}
}