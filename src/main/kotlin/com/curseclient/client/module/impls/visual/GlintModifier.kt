package com.curseclient.client.module.impls.visual

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import java.awt.Color

object GlintModifier : Module(
    "GlintModifier",
    "Modifier minecraft glint effect",
    Category.VISUAL
) {
    @JvmStatic
    val color by setting("Color", Color(-1))
}