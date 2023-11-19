package com.curseclient.client.module.modules.visual

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import java.awt.Color

object Ambience: Module(
    "Ambience",
    "Change environment color",
    Category.VISUAL
) {
    val lightMap by setting("LightMap", Color(125, 255, 125))
}