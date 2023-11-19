package com.curseclient.client.module.modules.visual

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting

object MotionBlur : Module(
    "MotionBlur",
    "Add motion to your visual",
    Category.VISUAL) {

    val amount by setting("Amount", 1.0, 1.0, 8.0, 1.0)


    fun onTick() {
        setTags(amount.toString())
    }

}