package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.events.render.EventFovUpdate
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.world.CrystalUtils
import org.lwjgl.opengl.Display
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation


object FovModifier : Module(
    "FovModifier",
    "Allows to break the fov limit and some feature",
    Category.VISUAL
){
    private var baseFov by setting("FOV", 90.0, 40.0, 180.0, 1.0)

    val static by setting("Static", true)
    val allowSprint by setting("Allow Sprint", true, visible = { static })

    val allowBow by setting("BowZoom", false)
    val zoomFactor by setting("Zoom Factor", 1.5, 1.0, 5.0, 0.1, visible = { allowBow })

    // TODO: fix this T_T
    // Oh its got fixed fr, why I'm not realize it soon
    init {
        safeListener<EventFovUpdate> { event ->
            val base = baseFov
            event.setFov(base.toFloat())
        }
    }

    // just Put It Here And Will Touch It Later
    private fun bowOverlay(alpha: Float) {
        mc.textureManager.bindTexture(ResourceLocation("textures/bowoverlays.png"))
        RenderUtils2D.drawTexture(0f, 0f, 0f, 0f, Display.getWidth().toFloat(), Display.getHeight().toFloat())
    }
}