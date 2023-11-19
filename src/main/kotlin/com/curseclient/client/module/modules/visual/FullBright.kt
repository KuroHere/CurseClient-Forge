package com.curseclient.client.module.modules.visual

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module

object FullBright : Module(
    "FullBright",
    "Makes everything brighter",
    Category.VISUAL
) {
    override fun onEnable() {
        mc.gameSettings.gammaSetting = 1000f
        setEnabled(false)
    }
}