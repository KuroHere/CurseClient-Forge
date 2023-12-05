package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.events.render.EventFovUpdate
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.RenderUtils2D
import net.minecraft.client.Minecraft
import net.minecraft.init.Items
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.Display


object FovModifier : Module(
    "FovModifier",
    "Allows to break the fov limit and some feature",
    Category.VISUAL
){
    private var baseFov by setting("FOV", 90.0, 40.0, 180.0, 1.0)

    val static by setting("Static", true)
    val allowSprint by setting("Allow Sprint", true, visible = { static })

    val allowBow by setting("Bow Zoom", false)
    val zoomFactor by setting("Zoom Factor", 1.5, 1.0, 5.0, 0.1, visible = { allowBow })
    private val smoothCamera by setting("SmoothCamera", true, visible = { allowBow })

    private var lastSensitivity = mc.gameSettings.mouseSensitivity
    private var wasCinematic = false

    init {
        safeListener<TickEvent.ClientTickEvent> {
            val player = Minecraft.getMinecraft().player
            val item = player.heldItemMainhand

            if (player.isUser) {
                if (allowBow && player.isHandActive && item.item === Items.BOW) {
                    lastSensitivity = mc.gameSettings.mouseSensitivity
                    wasCinematic = mc.gameSettings.smoothCamera
                    mc.gameSettings.smoothCamera = smoothCamera
                    mc.renderGlobal.setDisplayListEntitiesDirty()
                } else if (!player.isHandActive || item.item !== Items.BOW) {
                    mc.gameSettings.mouseSensitivity = lastSensitivity
                    mc.gameSettings.smoothCamera = wasCinematic
                }
            }
        }
        safeListener<EventFovUpdate> { event ->
            val base = baseFov
            event.setFov(base.toFloat())
        }

    }

    // just Put It Here And Will Touch It Later
    private fun bowOverlay(progress: Float, alpha: Float) {
        mc.textureManager.bindTexture(ResourceLocation("textures/bowoverlays.png"))
        RenderUtils2D.drawTexture(0f, 0f, 0f, 0f, Display.getWidth().toFloat(), Display.getHeight().toFloat())
    }
}