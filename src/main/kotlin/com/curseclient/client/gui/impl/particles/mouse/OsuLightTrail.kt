package com.curseclient.client.gui.impl.particles.mouse

import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.vector.Vec2d
import org.lwjgl.opengl.GL11
import java.awt.Color

class OsuLightTrail {
    private val trail: MutableList<Pair<Double, Double>> = ArrayList()
    private val maxTrailLength = 50

    fun addToTrail(x: Double, y: Double) {
        trail.add(Pair(x, y))

        if (trail.size > maxTrailLength) {
            trail.removeAt(0)
        }
    }

    fun renderTrail() {
        for (i in 0 until trail.size - 1) {
            val start = Vec2d(trail[i].first, trail[i].second)
            val end = Vec2d(trail[i + 1].first, trail[i + 1].second)

            val alpha = calculateTrailAlpha(i)
            val c1 = HUD.getColor(0)

            val blurRadius = 10
            val blurColor = c1.setAlpha(100)

            drawGradientOutlineWithBlur(start, end, 2.5F, c1, c1, alpha, blurRadius, blurColor)
        }
    }

    private fun drawGradientOutlineWithBlur(start: Vec2d, end: Vec2d, width: Float, startColor: Color, endColor: Color, alpha: Int, blurRadius: Int, blurColor: Color) {
        val distance = MathUtils.calculateDistance(start, end)
        val numSegments = distance.toInt()
        val step = 1f / numSegments.toFloat()

        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glLineWidth(width)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)

        var t = 0f

        for (i in 0 until numSegments) {
            val currentColor = ColorUtils.interpolateColor(startColor, endColor, t)
            val currentAlpha = (alpha * (2 - t)).coerceIn(0F, 255F)
            GL11.glColor4f(currentColor.red / 255f, currentColor.green / 255f, currentColor.blue / 255f, currentAlpha / 255f)
            GL11.glBegin(GL11.GL_LINES)
            GL11.glVertex2d(start.x + (end.x - start.x) * t, start.y + (end.y - start.y) * t)
            GL11.glVertex2d(start.x + (end.x - start.x) * (t + step), start.y + (end.y - start.y) * (t + step))
            GL11.glEnd()
            t += step
        }

        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
    }

    private fun calculateTrailAlpha(index: Int): Int {
        val progress = index.toDouble() / trail.size.toDouble()
        return (255 * (2 - progress)).toInt()
    }
}