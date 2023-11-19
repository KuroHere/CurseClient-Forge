package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.events.PlayerPacketEvent
import com.curseclient.client.event.events.render.RenderEntityEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.managers.PacketManager
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.Vec2f
import net.minecraftforge.fml.common.gameevent.TickEvent

object VisualRotations : Module(
    "VisualRotations",
    "Shows your serverside rotation",
    Category.VISUAL
) {
    private var onlyHead by setting("Only Head", false)

    private var pitch = 0f
    private var prevPitch = 0f
    private var p = Vec2f(0.0f, 0.0f)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (!onlyHead)
                player.renderYawOffset = PacketManager.lastReportedYaw

            player.rotationYawHead = PacketManager.lastReportedYaw
        }

        safeListener<PlayerPacketEvent.Post> {
            prevPitch = pitch
            pitch = PacketManager.lastReportedPitch
        }

        safeListener<RenderEntityEvent.Pre> {
            if (it.entity !is EntityPlayer) return@safeListener
            if (it.entity != player) return@safeListener

            with(it.entity) {
                p = Vec2f(rotationPitch, prevRotationPitch)

                prevRotationPitch = prevPitch
                rotationPitch = pitch
            }
        }

        safeListener<RenderEntityEvent.Post> {
            if (it.entity !is EntityPlayer) return@safeListener
            if (it.entity != player) return@safeListener

            with(it.entity) {
                prevRotationPitch = p.y
                rotationPitch = p.x
            }
        }
    }

}