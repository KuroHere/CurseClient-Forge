package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import net.minecraftforge.fml.common.gameevent.TickEvent

object CustomFov : Module(
    "CustomFov",
    "Allows to break the fov limit",
    Category.VISUAL
){
    private val fov by setting("FOV", 90.0, 40.0, 180.0, 1.0)
    val static by setting("Static", true)
    val allowSprint by setting("Allow Sprint", true, visible = { static })

    init {
        safeListener<TickEvent.ClientTickEvent> {
            mc.gameSettings.fovSetting = fov.toFloat()
        }
    }
}