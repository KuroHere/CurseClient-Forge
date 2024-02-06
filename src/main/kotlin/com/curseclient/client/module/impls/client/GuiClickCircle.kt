package com.curseclient.client.module.impls.client

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.animation.ease.EaseUtils

import java.awt.Color

object GuiClickCircle: Module(
    "GuiClickCircle",
    "Custom your cursor click",
    Category.CLIENT
) {
    val mode by setting("Mode", Mode.Fill)
    val seconds by setting("Seconds", 2, 1, 5,1, visible = {mode == Mode.Fill || mode == Mode.Outline1})
    val radius by setting("Radius", 5, 3, 15, 1)
    val color by setting("Color", Color(-0x7f000001))
    val easing by setting("Easing", EaseUtils.EaseType.InQuad, visible = {mode == Mode.Fill || mode == Mode.Outline1})

    enum class Mode {
        Fill,
        Outline1,
        Outline2
    }
}