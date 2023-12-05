package com.curseclient.client.module.modules.visual

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting

// TODO: Will change to ItemModify have physic and 2D(older minecraft ver) renderer
object ItemPhysics : Module(
    "ItemPhysics",
    "Makes item renderer better",
    Category.VISUAL
){
    val size by setting("Size", 1.0, 0.3, 1.5, 0.1)
    val itemWeight by setting("Weight", 0.5, 0.0, 1.0, 0.1)
}