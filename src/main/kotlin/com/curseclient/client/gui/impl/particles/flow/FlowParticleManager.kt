package com.curseclient.client.gui.impl.particles.flow

import baritone.api.utils.Helper.mc
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec2f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import java.awt.*
import java.nio.FloatBuffer
import java.util.*

/*
 I get it from Atomic client (1.17+)
 and adjust somethin to using in curse
 */

class FlowParticleManager(amount: Int) {
    val particles = mutableListOf<FlowParticle>()

    init {
        val sr = ScaledResolution(mc)
        val w = sr.scaledWidth
        val h = sr.scaledHeight
        val r = Random()
        repeat(amount) {
            val initialPos = Vec2f((r.nextInt(w - 2) + 1).toFloat(), (r.nextInt(h - 2) + 1).toFloat())
            val randomVec = Vec2f((Math.random() - 0.5).toFloat(), (Math.random() - 0.5).toFloat())
            val vel = Vec2f(randomVec.x * 100f, randomVec.y * 100f)
            particles.add(FlowParticle(initialPos, vel, Color.WHITE))
        }
    }

    fun tick() {
        particles.forEach { it.move() }
    }

    fun render() {
        val pl = particles.toTypedArray()
        for (particle in pl) {
            fill(
                modify(
                    particle.color,
                    -1,
                    -1,
                    -1,
                    MathHelper.clamp((particle.brightness * 255).toInt(), 0, 255)
                ),
                particle.x - 0.5,
                particle.y - 0.5,
                particle.x + .5,
                particle.y + .5
            )
            var last: PosEntry? = null
            particle.previousPos.forEachIndexed { i, previousPos ->
                val v = i.toDouble() / particle.previousPos.size
                if (last == null) last = previousPos
                val dist = Math.sqrt(
                    Math.pow((last!!.x - previousPos.x), 2.0) + Math.pow((last!!.y - previousPos.y), 2.0)
                )
                if (dist < 10)
                    lineScreenD(
                        modify(
                            Color.getHSBColor(v.toFloat(), 0.6f, 1f),
                            -1,
                            -1,
                            -1,
                            (v * 255f).toInt()
                        ),
                        last!!.x,
                        last!!.y,
                        previousPos.x,
                        previousPos.y
                    )
                last = previousPos
            }
        }
    }

    fun fill(color: Color, x1: Double, y1: Double, x2: Double, y2: Double) {
        val buffer: FloatBuffer = BufferUtils.createFloatBuffer(16)

        var j: Double
        var x1New = x1
        var x2New = x2

        if (x1New < x2New) {
            j = x1New
            x1New = x2New
            x2New = j
        }

        var y1New = y1
        var y2New = y2
        if (y1New < y2New) {
            j = y1New
            y1New = y2New
            y2New = j
        }

        val f = (color.rgb ushr 24 and 255) / 255.0f
        val g = (color.rgb ushr 16 and 255) / 255.0f
        val h = (color.rgb ushr 8 and 255) / 255.0f
        val k = (color.rgb and 255) / 255.0f

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glBegin(GL_QUADS)
        glColor4f(g, h, k, f)
        glVertex2d(x1New, y2New)
        glVertex2d(x2New, y2New)
        glVertex2d(x2New, y1New)
        glVertex2d(x1New, y1New)
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
    }

    fun lineScreenD(c: java.awt.Color, x: Double, y: Double, x1: Double, y1: Double) {
        val g = c.red / 255f
        val h = c.green / 255f
        val k = c.blue / 255f
        val f = c.alpha / 255f

        val tessellator = Tessellator.getInstance()
        val bufferBuilder: BufferBuilder = tessellator.buffer

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        bufferBuilder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR)
        bufferBuilder.pos(x, y, 0.0).color(g, h, k, f).endVertex()
        bufferBuilder.pos(x1, y1, 0.0).color(g, h, k, f).endVertex()
        tessellator.draw()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
    }

    /**
     * @param original       the original color
     * @param redOverwrite   the new red (or -1 for original)
     * @param greenOverwrite the new green (or -1 for original)
     * @param blueOverwrite  the new blue (or -1 for original)
     * @param alphaOverwrite the new alpha (or -1 for original)
     * @return the modified color
     */
    private fun modify(original: Color, redOverwrite: Int, greenOverwrite: Int, blueOverwrite: Int, alphaOverwrite: Int): Color {
        return Color(if (redOverwrite == -1) original.red else redOverwrite, if (greenOverwrite == -1) original.green else greenOverwrite, if (blueOverwrite == -1) original.blue else blueOverwrite, if (alphaOverwrite == -1) original.alpha else alphaOverwrite)
    }

    fun remake() {
        val sr = ScaledResolution(mc)
        val w = sr.scaledWidth
        val h = sr.scaledHeight
        val r = Random()
        particles.forEach {
            it.x = r.nextInt(w).toDouble()
            it.y = r.nextInt(h).toDouble()
            it.previousPos.clear()
        }
    }
}
