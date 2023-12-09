package com.curseclient.client.module.modules.visual

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.model.wing.DragonWing
import com.curseclient.client.utility.render.model.wing.LayerWings
import java.awt.Color


object CustomModel: Module(
    "CustomModel",
    "Custom your player model.",
    Category.VISUAL
) {
    val wingType by setting("WingType", Type.Dragon)

    val dragon by setting("Wing", DragonWing.textures.Wing, { wingType == Type.Dragon })
    val wingLayer by setting("WingLayer", LayerWings.textures.Feather, { wingType == Type.Layer })

    val oxygen by setting("OxygenMask", false)
    val scale by setting("Scale", 1.0, 0.75, 1.25, 0.25)
    val color by setting("Color", Color(255, 255, 255))

    enum class Type {
        Dragon,
        Layer,
        Crystal
    }
}