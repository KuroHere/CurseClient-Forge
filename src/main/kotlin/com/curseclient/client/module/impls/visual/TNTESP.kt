package com.curseclient.client.module.impls.visual

import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.RenderUtils3D
import net.minecraft.entity.item.EntityTNTPrimed
import java.awt.Color

object TNTESP : Module(
    "TNT ESP",
    "Allows you to see ignited TNT blocks through walls and timer.",
    Category.VISUAL
) {
    private val color by setting("Color", Color(173, 0, 0, 250))
    val timeCount by setting("Count", true)

    init {
        safeListener<Render3DEvent> {
            mc.world.loadedEntityList.filterIsInstance<EntityTNTPrimed>().forEach {
                RenderUtils3D.drawEntityBox(it, Color(color.red, color.green, color.blue), false)
            }
        }
    }
}
