package com.curseclient.client.module.impls.visual

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting

// TODO: Will change to ItemModify have physic and 2D(from older minecraft version) renderer
object ItemPhysics : Module(
    "ItemPhysics",
    "Makes item renderer better",
    Category.VISUAL
){
    val size by setting("Size", 1.0, 0.3, 1.5, 0.1)
    val weight by setting("Weight", 0.5, 0.0, 1.0, 0.1)
}