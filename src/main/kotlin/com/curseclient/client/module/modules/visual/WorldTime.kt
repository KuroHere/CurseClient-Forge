package com.curseclient.client.module.modules.visual

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting

object WorldTime : Module(
    "WorldTime",
    "Allows to change world time",
    Category.VISUAL
) {
    val time by setting("Time", 0.0, 0.0, 24000.0, 600.0)
}