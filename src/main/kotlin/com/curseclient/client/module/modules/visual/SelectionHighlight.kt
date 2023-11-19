package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.events.render.Render2DEvent
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.utility.render.RenderUtils2D.drawBorderedRect
import com.curseclient.client.utility.render.esp.ESPRenderInfo
import com.curseclient.client.utility.render.font.FontRenderer
import com.curseclient.client.utility.render.font.Fonts
import net.minecraft.block.Block
import net.minecraft.block.BlockAir
import net.minecraft.block.state.IBlockState
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import java.awt.Color

object SelectionHighlight : Module(
    "SelectionHighlight",
    "BOX!",
    Category.VISUAL
) {
    private val filledColor by setting("Filled Color", Color(40, 200, 250, 60))
    private val outlineColor by setting("Outline Color", Color(40, 200, 250, 150))
    private val outlineWidth by setting("Outline Width", 1.0, 1.0, 5.0, 0.25)
    private val allSides by setting("All Sides", true)
    private val fullOutline by setting("Full Outline", false, { !allSides })

    private var info: ESPRenderInfo? = null

    init {
        safeListener<Render3DEvent> {
            info?.draw()
        }
    }

    @JvmStatic
    fun draw(result: RayTraceResult) {
        if (result.typeOfHit != RayTraceResult.Type.BLOCK) {
            info = null
            return
        }

        val pos = result.blockPos
        val sides = if (allSides) EnumFacing.values().toList() else listOf(result.sideHit)

        info = ESPRenderInfo(
            pos,
            filledColor,
            outlineColor,
            sides,
            fullOutline,
            outlineWidth.toFloat()
        )
    }

}