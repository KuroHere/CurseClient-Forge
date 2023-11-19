package com.curseclient.client.module.modules.client

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import net.minecraft.util.ResourceLocation

object Cape : Module(
    "Cape"
    ,"Custom your cape",
    Category.CLIENT
) {
    val styleValue by setting("Style", CapeStyle.DARK)

    private val capeCache = hashMapOf<String, CapeStyle>()

    fun getCapeLocation(value: String): ResourceLocation {
        if (capeCache[value.uppercase()] == null) {
            try {
                capeCache[value.uppercase()] = CapeStyle.valueOf(value.uppercase())
            } catch (e: Exception) {
                capeCache[value.uppercase()] = CapeStyle.DARK
            }
        }
        return capeCache[value.uppercase()]!!.location
    }
    enum class CapeStyle(val location: ResourceLocation) {
        DARK(ResourceLocation("curseclient", "cape/dark.png")),
        DARKER(ResourceLocation("curseclient", "cape/darker.png")),
        LIGHT(ResourceLocation("curseclient", "cape/light.png")),
        SPECIAL1(ResourceLocation("curseclient", "cape/special1.png")),
        SPECIAL2(ResourceLocation("curseclient", "cape/special2.png"))
    }

}