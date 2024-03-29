package com.curseclient.client.module.impls.visual

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
            val mainHandItem = player.heldItemMainhand
            val offHandItem = player.heldItemOffhand

            if (player.isUser) {
                if (allowBow
                    && player.isHandActive
                    && mainHandItem.item == Items.BOW
                    || offHandItem.item == Items.BOW
                    ) {
                    lastSensitivity = mc.gameSettings.mouseSensitivity
                    wasCinematic = mc.gameSettings.smoothCamera
                    mc.gameSettings.smoothCamera = smoothCamera
                    mc.renderGlobal.setDisplayListEntitiesDirty()
                } else if (!allowBow
                    || !player.isHandActive
                    || mainHandItem.item != Items.BOW
                    || offHandItem.item != Items.BOW
                    ) {
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
}