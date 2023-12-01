package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.events.render.EventFovUpdate
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.hud.Watermark
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.shader.GradientUtil
import com.curseclient.client.utility.world.CrystalUtils
import org.lwjgl.opengl.Display
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import java.awt.Color


object FovModifier : Module(
    "FovModifier",
    "Allows to break the fov limit and some feature",
    Category.VISUAL
){
    private var baseFov by setting("FOV", 90.0, 40.0, 180.0, 1.0)

    val static by setting("Static", true)
    val allowSprint by setting("Allow Sprint", true, visible = { static })

    private val bow by setting("BowZoom", false)
    private val zoomFactor by setting("Zoom Factor", 1.5, 1.0, 5.0, 0.1, visible = { bow })

    // TODO: fix this T_T
    // No idea, how to return original fov
    init {
        safeListener<EventFovUpdate> { event ->
            var base = baseFov
            val entity: EntityPlayer = event.entity
            val item: ItemStack = entity.heldItemMainhand
            val useDuration: Int = entity.itemInUseCount

            val bowFov: Int = zoomFactor.toInt()

            val duration: Int = useDuration.toFloat().coerceAtMost(20.0F).toInt()
            val modifier: Float = CrystalUtils.MODIFIER_BY_TICK[duration] ?: 0.0F
            baseFov -= modifier * bowFov
            if (bow && entity == mc.player && item.item == Items.BOW) {
                event.fov = baseFov.toFloat()
            } else if (!bow) {
                event.setFov(baseFov.toFloat())
            }
        }
    }

    // just Put It Here And Will Touch It Later
    private fun bowOverlay(alpha: Float) {
        mc.textureManager.bindTexture(ResourceLocation("textures/bowoverlays.png"))
        RenderUtils2D.drawTexture(0f, 0f, 0f, 0f, Display.getWidth().toFloat(), Display.getHeight().toFloat())
    }
}