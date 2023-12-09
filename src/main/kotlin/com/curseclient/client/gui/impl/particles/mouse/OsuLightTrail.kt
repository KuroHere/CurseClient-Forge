package com.curseclient.client.gui.impl.particles.mouse

import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.utility.render.shader.GradientUtil
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.abs


// TODO: it work only when gradle runclient
class OsuLightTrail {
    private val trail: MutableList<Pair<Double, Double>> = ArrayList()
    private val maxTrailLength = 100
    private val trailFadeDuration = 2000 // (miligiây)
    private val trailBlurRadius = 2

    fun addToTrail(x: Double, y: Double) {
        trail.add(Pair(x, y))

        // Đảm bảo rằng độ dài của đuôi ánh sáng không vượt quá giới hạn
        if (trail.size > maxTrailLength) {
            trail.removeAt(0)
        }
    }

    fun renderTrail() {
        System.currentTimeMillis()
        for (i in 0 until trail.size - 1) {
            val start = trail[i]
            val end = trail[i + 1]

            val alpha = calculateTrailAlpha(i)
            val c1 = HUD.getColor(0)
            val c2 = HUD.getColor(10)

            GradientUtil.applyGradientCornerLR(start.first.toFloat(), start.second.toFloat(), end.first.toFloat(), end.second.toFloat(), alpha.toFloat(), c1, c2) {
                //drawSmoothLine(start.first, start.second, end.first, end.second, Color.WHITE, alpha)
                drawLine(start.first, start.second, end.first, end.second, Color.WHITE, alpha)
            }
        }
    }

    private fun calculateTrailAlpha(index: Int): Int {
        // Tính toán giá trị alpha dựa trên vị trí của phần tử trong đuôi ánh sáng
        val progress = index.toDouble() / trail.size.toDouble()
        return (255 * (1 - progress)).toInt()
    }

    private fun drawSmoothLine(x1: Double, y1: Double, x2: Double, y2: Double, color: Color, alpha: Int) {
        val minecraft = Minecraft.getMinecraft()
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        minecraft.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
        RenderHelper.disableStandardItemLighting()
        buffer.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR)

        val dx = abs(x2 - x1)
        val dy = abs(y2 - y1)
        val sx = if (x1 < x2) 1 else -1
        val sy = if (y1 < y2) 1 else -1
        var err = dx - dy

        var x = x1
        var y = y1

        while (true) {
            buffer.pos(x, y, 0.0).color(color.red, color.green, color.blue, alpha).endVertex()

            if (x == x2 && y == y2) break
            val e2 = 2 * err
            if (e2 > -dy) {
                err -= dy
                x += sx
            }
            if (e2 < dx) {
                err += dx
                y += sy
            }
        }

        tessellator.draw()
        RenderHelper.enableStandardItemLighting()
    }

    private fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double, color: Color, alpha: Int) {
        val minecraft = Minecraft.getMinecraft()
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        minecraft.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
        RenderHelper.disableStandardItemLighting()
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR)
        buffer.pos(x1, y1, 0.0).color(color.red, color.green, color.blue, alpha).endVertex()
        buffer.pos(x2, y2, 0.0).color(color.red, color.green, color.blue, alpha).endVertex()
        tessellator.draw()
        RenderHelper.enableStandardItemLighting()
    }
}