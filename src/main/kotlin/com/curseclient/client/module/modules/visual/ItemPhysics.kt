package com.curseclient.client.module.modules.visual

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting

object ItemPhysics : Module(
    "ItemPhysics",
    "Makes item renderer better",
    Category.VISUAL
){
    val size by setting("Size", 1.0, 0.3, 1.5, 0.1)
}