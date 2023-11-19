package com.curseclient.client.module.modules.visual

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.setting.type.ColorSetting
import java.awt.Color


object CustomModel: Module(
    "CustomModel",
    "put a wing on you",
    Category.VISUAL
) {
    val wing by setting("Wing", true)
    val scale by setting("Scale", 1.0, 0.75, 1.25, 0.25, visible = { wing })
    val colorM by setting("ColorMode", Mode.Client, visible = { wing })
    val color by setting("Color", Color(255, 255, 255), visible = { wing && colorM == Mode.Custom })
    enum class Mode {
        Client,
        Custom
    }
}