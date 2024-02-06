package com.curseclient.client.module.impls.visual

import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.esp.AnimatedESPRenderer
import com.curseclient.client.utility.render.esp.ESPRenderInfo
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.RayTraceResult
import java.awt.Color

object BlockHighlight : Module(
    "BlockHighlight",
    "highlight the block you look at",
    Category.VISUAL
) {
    private val filledColor by setting("Filled Color", Color(40, 200, 250, 60))
    private val outlineColor by setting("Outline Color", Color(40, 200, 250, 150))
    private val outlineWidth by setting("Outline Width", 1.0, 1.0, 5.0, 0.25)
    private val allSides by setting("All Sides", true)
    private val fullOutline by setting("Full Outline", false, { !allSides })

    private val glide by setting("Glide", false)
    private val moveSpeed by setting("GlideSpeed", 1.0, 0.5, 3.0, 0.1, { glide })

    private var info: ESPRenderInfo? = null
    private val renderer = AnimatedESPRenderer { Triple(filledColor.setAlpha(60), filledColor.setAlpha(120), outlineWidth.toFloat()) }

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

        if (!isEnabled() || !glide) {
            renderer.reset()
            return
        }

        if (glide) {
            renderer.setPosition(pos)
            renderer.animationSpeed = moveSpeed
            renderer.maxSize = 1.0
            renderer.draw()
        } else
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