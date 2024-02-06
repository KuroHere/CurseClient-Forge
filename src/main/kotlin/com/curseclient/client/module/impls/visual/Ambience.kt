package com.curseclient.client.module.impls.visual

import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.ColorUtils.b
import com.curseclient.client.utility.render.ColorUtils.g
import com.curseclient.client.utility.render.ColorUtils.r
import net.minecraftforge.client.event.EntityViewRenderEvent
import java.awt.Color


object Ambience: Module(
    "Ambience",
    "Change game environment",
    Category.VISUAL
) {
    val customLight by setting("CustomLightMap", false)
    val lightMap by setting("LightMap", Color(125, 255, 125))

    private val fogMod by setting("FogColor", FogMode.None)
    private val fogColor by setting("FogColor", Color(130, 130, 230))

    val timeOfDay by setting("Time of Day", Time.None)
    val time by setting("Time", 0.0, 0.0, 24000.0, 600.0,)
    val customTimeSpeed by setting("CustomTimeSpeed", false)
    val timeSpeed by setting("TimeSpeed", 0.0, 0.0, 100.0, 1.0)

    enum class Time {
        None,
        Day,
        Sunset,
        Dawn,
        Night,
        Midnight,
        Noon,
        Custom
    }

    private enum class FogMode {
        Custom,
        Client,
        None
    }

    init {
        safeListener<EntityViewRenderEvent.FogColors> {
            when (fogMod) {
                FogMode.Custom -> {
                    it.red = fogColor.r
                    it.green = fogColor.g
                    it.blue = fogColor.b
                }
                FogMode.Client -> {
                    val color = HUD.getColor(0, 0.7)
                    it.red = color.red.toFloat() / 255f
                    it.green = color.green.toFloat() / 255f
                    it.blue = color.blue.toFloat() / 255f
                }

                FogMode.None -> null
            }
        }
    }
}