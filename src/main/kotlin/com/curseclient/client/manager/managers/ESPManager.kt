package com.curseclient.client.manager.managers

import com.curseclient.client.module.impls.client.PerformancePlus
import com.curseclient.client.utility.render.graphic.GLUtils.renderGL
import com.curseclient.client.utility.render.RenderTessellator
import com.curseclient.client.utility.render.RenderUtils3D
import com.curseclient.client.utility.render.esp.ESPRenderInfo
import com.curseclient.client.utility.render.esp.move
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import kotlin.math.abs

object ESPManager {
    private val facings by lazy { EnumFacing.values().toList() }
    private val toRender: MutableList<ESPRenderInfo> = ArrayList()

    fun render() {
        if (toRender.isEmpty()) return

        renderGL {
            GlStateManager.disableDepth()

            val viewerPos = Vec3d.ZERO.subtract(RenderUtils3D.viewerPos)

            drawFilled(viewerPos)
            drawOutline(viewerPos)
        }

        toRender.clear()
    }

    private fun drawFilled(viewerPos: Vec3d) {
        RenderTessellator.render(GL11.GL_QUADS) {
            toRender.forEach { node ->
                val box = node.box.move(viewerPos)
                RenderTessellator.drawBox(box, node.filledColor, node.sides)
            }
        }
    }

    private fun drawOutline(viewerPos: Vec3d) {
        if (PerformancePlus.isEnabled() && PerformancePlus.fastOutline) {
            drawOutlineFast(viewerPos)
            return
        }

        RenderTessellator.render(GL11.GL_LINES) {
            var lastWidth = -1f
            var isRendering = false

            toRender.sortedBy { it.thickness }.forEach { node ->
                if (abs(lastWidth - node.thickness) > 0.3) {
                    if (isRendering) {
                        RenderTessellator.render()
                        RenderTessellator.begin(GL11.GL_LINES)
                    }

                    GlStateManager.glLineWidth(node.thickness)
                    lastWidth = node.thickness
                }

                val box = node.box.move(viewerPos)
                RenderTessellator.drawOutline(box, node.outlineColor, if (node.fullOutline) facings else node.sides)
                isRendering = true
            }
        }
    }

    private fun drawOutlineFast(viewerPos: Vec3d) {
        RenderTessellator.render(GL11.GL_LINES) {
            toRender.forEach { node ->
                val box = node.box.move(viewerPos)
                RenderTessellator.drawOutline(box, node.outlineColor, if (node.fullOutline) facings else node.sides)
            }
        }
    }

    fun put(info: ESPRenderInfo) {
        toRender.add(info)
    }
}