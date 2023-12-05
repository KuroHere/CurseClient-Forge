package com.curseclient.client.module.modules.misc

import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.RenderUtils3D
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import java.awt.Color
import kotlin.math.floor


object MapBounds: Module(
    "MapBounds",
    "Shows the boundaries of the map you are currently standing in.",
    Category.MISC
) {
    //If rendering should go through walls
    private val throughWalls by setting("ThroughWalls", false)

    //The color the outline should be
    private val outlineColor by setting("OutlineColor", Color(75, 25, 255, 255))

    //The color the grid should be
    private val gridColor by setting("GridColor", Color(5, 155, 0, 255))

    init {
        safeListener<Render3DEvent> {event ->
            val mc = Minecraft.getMinecraft()
            val minX = (floor((mc.player.posX + 64) / 128).toInt() * 128 - 64).toDouble()
            val minZ = (floor((mc.player.posZ + 64) / 128).toInt() * 128 - 64).toDouble()
            val bb = AxisAlignedBB(minX, 0.0, minZ, minX + 127, 255.0, minZ + 127)
            RenderUtils3D.begin3D()

            // begin3D() disables depth
            if (!throughWalls) {
                GlStateManager.enableDepth()
            }
            for (face in EnumFacing.HORIZONTALS) {
                RenderUtils3D.renderFaceMesh(interpolateBB(bb, event.partialTicks), face, 8.0, 1f, gridColor.rgb)
            }
            RenderUtils3D.drawBoundingBox(interpolateBB(bb, event.partialTicks), 2f, outlineColor.rgb)
            RenderUtils3D.end3D()
        }

    }

    private fun interpolateBB(bb: AxisAlignedBB, partialTicks: Float): AxisAlignedBB {
        val entityplayer: EntityPlayer = Minecraft.getMinecraft().player
        val ix = entityplayer.lastTickPosX + (entityplayer.posX - entityplayer.lastTickPosX) * partialTicks.toDouble()
        val iy = entityplayer.lastTickPosY + (entityplayer.posY - entityplayer.lastTickPosY) * partialTicks.toDouble()
        val iz = entityplayer.lastTickPosZ + (entityplayer.posZ - entityplayer.lastTickPosZ) * partialTicks.toDouble()
        return AxisAlignedBB(
            bb.minX - ix,
            bb.minY - iy,
            bb.minZ - iz,
            bb.maxX - ix + 1,
            bb.maxY - iy + 1,
            bb.maxZ - iz + 1
        )
    }
}