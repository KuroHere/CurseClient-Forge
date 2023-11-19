package com.curseclient.client.module.modules.hud.modulelist

import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.client.FontSettings
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.module.modules.hud.modulelist.ModuleList.animationMode
import com.curseclient.client.module.modules.hud.modulelist.ModuleList.font
import com.curseclient.client.utility.render.animation.EaseUtils
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.ColorUtils.setAlphaD
import com.curseclient.client.utility.render.graphic.GLUtils
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.Screen
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import net.minecraft.client.Minecraft
import java.awt.Color
import kotlin.math.max

class ModuleListElement(val module: Module, var y: Double, var pos: Int) {
    var shouldRemoved = false
    private var progress = 0.0f

    val mc: Minecraft = Minecraft.getMinecraft()

    private fun getSmoothedProgress(): Float {
        return EaseUtils.getEase(progress.toDouble(), EaseUtils.EaseType.OutCubic).toFloat()
    }

    fun update() {
        if(progress < 0f) {
            shouldRemoved = true
            return
        }

        if (module.isEnabled() && module.isVisible && ModuleList.isEnabled()) {
            progress += ModuleList.animationSpeed.toFloat() * GLUtils.deltaTimeFloat()
        } else {
            progress -= ModuleList.animationSpeed.toFloat() * GLUtils.deltaTimeFloat()
        }

        if(progress > 1.0f) progress = 1.0f
        if(progress < -1.0f) progress = -1.0f
    }

    fun render() {
        val sr = Screen

        val name = module.name
        val mode = " " + module.getHudInfo()
        val text = if(module.getHudInfo() == "") name else name + mode

        val p = if (animationMode == ModuleList.ModuleListAnimationMode.Slide) getSmoothedProgress() else 1.0f
        val offset: Double = (1.0 - p) * font.getStringWidth(module.name + if(module.getHudInfo() != "") " " + module.getHudInfo() else "", ModuleList.size)

        val bgColor = HUD.bgColor.setAlphaD(ModuleList.bgAlpha)
        val fontColor1 = HUD.getColor(pos).setAlpha(if (animationMode != ModuleList.ModuleListAnimationMode.Scale) 255 else (255.0 * p).toInt())
        val fontColor2 = HUD.getColor(pos + 1).setAlpha(if (animationMode != ModuleList.ModuleListAnimationMode.Scale) 255 else (255.0 * p).toInt())

        //background
        RenderUtils2D.drawRect(
            Vec2d(sr.scaledWidth + offset, y),
            Vec2d(sr.scaledWidth - 5.0 - font.getStringWidth(text, ModuleList.size) + offset, y + getHeight()),
            bgColor
        )

        //right line
        if (ModuleList.line) RenderUtils2D.drawGradientRect(
            Vec2d(sr.scaledWidth, y),
            Vec2d(sr.scaledWidth - p, y + getHeight()),
            fontColor1,
            fontColor1,
            fontColor2,
            fontColor2
        )

        //module name
        font.drawString(
            name,
            Vec2d((sr.scaledWidth - 3.0 - font.getStringWidth(text, ModuleList.size)) + offset, y + (realHeight / 2.0)),
            color = fontColor1,
            shadow = ModuleList.textShadow,
            scale = ModuleList.size
        )

        //module mode
        font.drawString(
            mode,
            Vec2d((sr.scaledWidth - 3.0 - font.getStringWidth(mode, ModuleList.size)) + offset, y + (realHeight / 2.0)),
            color = Color(220, 220, 220, 255),
            shadow = ModuleList.textShadow,
            scale = ModuleList.size
        )
    }

    private val realHeight: Double get() =
        font.getHeight(ModuleList.size) + 4.0 * ModuleList.size * FontSettings.size

    fun getHeight() =
        max(0.0, realHeight * progress)
}