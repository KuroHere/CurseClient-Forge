package com.curseclient.client.gui.impl.clickgui.elements.settings.impl

import baritone.api.utils.Helper
import baritone.api.utils.Helper.mc
import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.elements.InteractiveElement
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.gui.impl.clickgui.elements.ModuleButton
import com.curseclient.client.gui.impl.clickgui.elements.settings.SettingButton
import com.curseclient.client.module.modules.client.ClickGui
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.setting.type.EnumSetting
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.math.MathUtils.toInt
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.graphic.GLUtils
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import com.sun.imageio.plugins.common.ImageUtil
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color


class EnumButton(val setting: EnumSetting<*>, gui: AbstractGui, baseButton: ModuleButton) : SettingButton(gui, baseButton) {
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

    private val arrow = ResourceLocation("textures/icons/enum.png")
    private val modes = ArrayList<ModeButton>()
    private var extended = false
    private var progress = 0.0

    override fun isVisible() = setting.isVisible

    override fun onRender() {
        super.onRender()
        progress = lerp(progress, extended.toInt().toDouble(), GLUtils.deltaTimeDouble() * 5.0 * ClickGui.settingsSpeed)

        val c1 = if (ClickGui.colorMode == ClickGui.ColorMode.Client) HUD.getColor(index)
        else if (ClickGui.pulse) ColorUtils.pulseColor(ClickGui.buttonColor1, 0, 1) else ClickGui.buttonColor1

        val c2 = when (ClickGui.colorMode) {
            ClickGui.ColorMode.Client -> HUD.getColor(index + 1)
            ClickGui.ColorMode.Static -> if (ClickGui.pulse) ColorUtils.pulseColor(ClickGui.buttonColor1, 0, 1) else ClickGui.buttonColor1
            else -> ClickGui.buttonColor2
        }

        val rectBuilder = RectBuilder(pos.plus(5.0, fr.getHeight(ClickGui.fontSize)), pos.plus(width, (height - fr.getHeight(ClickGui.fontSize)) * progress).minus(5.0, 0.0))
        with(rectBuilder) {
            outlineColor(c1.setAlpha(0), c2.setAlpha(0), c2.setAlpha((255 * progress).toInt()), c1.setAlpha((255 * progress).toInt()))
            width(1.0 * progress)
            color(ClickGui.disabledColor.setAlpha((230 * progress).toInt()))
            radius(ClickGui.buttonRound * progress)
            draw()
        }

        val h = ClickGui.height
        val h2 = if (!extended) ClickGui.height + fr.getHeight(ClickGui.fontSize) else ClickGui.height / progress

        val x1 = pos.x + 5 + ClickGui.space
        val x2 = pos.x + width / 2.0 - fr.getStringWidth(setting.name, ClickGui.settingFontSize) / 2.0

        val secondRectBuilder = RectBuilder(pos.plus(5.0, 0.0), pos.plus(width, h2).minus(5.0, 0.0))
        with(secondRectBuilder) {
            outlineColor(c1.setAlpha(0), c2.setAlpha((255 * progress).toInt()), c2.setAlpha(0), c1.setAlpha((255 * progress).toInt()))
            width(1.0 * progress)
            color(ClickGui.disabledColor.setAlpha(250))
            radius(if (extended) 0.8 / progress else ClickGui.buttonRound)
            draw()
        }

        fr.drawString(setting.name, Vec2d(lerp(x1, x2, progress), pos.y + h / 2.0), scale = ClickGui.settingFontSize)
        arrow(pos, 6.0f, 6.0f, lerp(90.0, 180.0, progress).toFloat())

        if (!extended) {
            val textPos = pos.plus(5 + ClickGui.space, h * 1.175)
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

    private fun arrow(pos: Vec2d, width: Float, height: Float, rotateAngle: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        mc.textureManager.bindTexture(arrow)
        GL11.glTranslatef((pos.x + this.width / 1.15).toFloat(), (pos.y + ClickGui.height / 2.2).toFloat(), 0f)
        GL11.glRotatef(rotateAngle, 0f, 0f, 1f)
        Gui.drawModalRectWithCustomSizedTexture(-5, -5, 0f, 0f, width.toInt(), height.toInt(), width, height)
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
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
        ClickGui.height + 3 + modes.sumOf { it.height } * extended.toInt().toDouble() + fr.getHeight(ClickGui.fontSize)

    private class ModeButton(val name: String, val baseButton: EnumButton, gui: AbstractGui) : InteractiveElement(Vec2d.ZERO, 0.0, 10.0, gui) {
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