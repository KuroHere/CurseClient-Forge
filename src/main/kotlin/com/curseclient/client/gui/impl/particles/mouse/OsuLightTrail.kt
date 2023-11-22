package com.curseclient.client.gui.impl.particles.mouse

import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.utility.render.shader.GradientUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11
import java.awt.Color

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
        val currentTime = System.currentTimeMillis()
        for (i in 0 until trail.size - 1) {
            val start = trail[i]
            val end = trail[i + 1]

            val alpha = calculateTrailAlpha(i)
            val c1 = HUD.getColor(0)
            val c2 = HUD.getColor(10)
            //val color = Color(255, 255, 255, alpha)

            GradientUtil.applyGradientHorizontal(start.first.toFloat(), start.second.toFloat(), end.first.toFloat(), end.second.toFloat(), alpha.toFloat(), c1, c2) {
                drawLine(start.first, start.second, end.first, end.second, Color.WHITE, alpha)
            }
        }
    }

    private fun calculateTrailAlpha(index: Int): Int {
        // Tính toán giá trị alpha dựa trên vị trí của phần tử trong đuôi ánh sáng
        val progress = index.toDouble() / trail.size.toDouble()
        return (255 * (1 - progress)).toInt()
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