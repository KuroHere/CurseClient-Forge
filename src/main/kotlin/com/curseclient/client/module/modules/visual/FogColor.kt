package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.ColorUtils.b
import com.curseclient.client.utility.render.ColorUtils.g
import com.curseclient.client.utility.render.ColorUtils.r
import net.minecraftforge.client.event.EntityViewRenderEvent
import java.awt.Color

object FogColor : Module(
    "FogColor",
    "Changes fog color",
    Category.VISUAL
) {
    private val colorMode by setting("Color Mode", ColorMode.Custom)
    private val color by setting("Color", Color(130, 130, 230), { colorMode == ColorMode.Custom })

    private enum class ColorMode {
        Custom,
        Client
    }

    init {
        safeListener<EntityViewRenderEvent.FogColors> {
            when (colorMode) {
                ColorMode.Custom -> {
                    it.red = color.r
                    it.green = color.g
                    it.blue = color.b
                }
                ColorMode.Client -> {
                    val color = HUD.getColor(0, 0.7)
                    it.red = color.red.toFloat() / 255f
                    it.green = color.green.toFloat() / 255f
                    it.blue = color.blue.toFloat() / 255f
                }
            }
        }
    }
}