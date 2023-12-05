package com.curseclient.client.gui.impl.particles.moving

import baritone.api.utils.Helper
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec2f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.nio.FloatBuffer
import java.util.*
import kotlin.math.abs

/*
 I get it from Atomic client (1.17+)
 and adjust somethin to using in curse
 */
class MovingParticleManager(amount: Int) {
    val particles: MutableList<MovingParticle> = ArrayList()

    init {
        val sr = ScaledResolution(Helper.mc)
        val w = sr.scaledWidth
        val h = sr.scaledHeight
        val r = Random()
        repeat(amount) {
            particles.add(
                MovingParticle(
                    Vec2f((r.nextInt(w - 2) + 1).toFloat(), (r.nextInt(h - 2) + 1).toFloat()),
                    Vec2f((Math.random() - 0.5f).toFloat() * 3f, (Math.random() - 0.5f).toFloat() * 3f),
                    Color.WHITE
                )
            )
        }
    }

    fun tick() {
        particles.toList().forEach { it.move(particles.toTypedArray()) }
    }

    fun render() {
        render(255)
    }

    fun render(cursorAlpha: Int) {
        val sr = ScaledResolution(Helper.mc)
        val w = sr.scaledWidth
        val h = sr.scaledHeight

        val renderX = Minecraft.getMinecraft().mouseHelper.deltaX
        val renderY = Minecraft.getMinecraft().mouseHelper.deltaY

        val mouse = Vec2f(renderX.toFloat(), renderY.toFloat())
        val md = 3 * (w + h)

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
            val v1 = Vec2f(particle.x.toFloat(), particle.y.toFloat())
            for (particle1 in pl) {
                val v = Vec2f(particle1.x.toFloat(), particle1.y.toFloat())
                val dist = distanceSquared(v1, v)
                if (dist < md) {
                    val dCalc = dist / md
                    val dCalcR = abs(1 - dCalc)
                    gradientLineScreen(
                        modify(
                            particle.color,
                            -1,
                            -1,
                            -1,
                            MathHelper.clamp((particle.brightness * 255 * dCalcR).toInt(), 0, 255)
                        ),
                        modify(
                            particle1.color,
                            -1,
                            -1,
                            -1,
                            MathHelper.clamp((particle1.brightness * 255 * dCalcR).toInt(), 0, 255)
                        ),
                        particle.x,
                        particle.y,
                        particle1.x,
                        particle1.y
                    )
                }
            }
            val mdist = distanceSquared(mouse, v1)
            if (mdist < md * 5) {
                val dCalc = mdist / (md * 5)
                val dCalcR = abs(1 - dCalc)
                gradientLineScreen(
                    modify(
                        particle.color,
                        -1,
                        -1,
                        -1,
                        MathHelper.clamp((particle.brightness * 255 * dCalcR).toInt(), 0, 255)
                    ),
                    modify(
                        getCurrentRGB(),
                        -1,
                        -1,
                        -1,
                        (255 * dCalcR * (cursorAlpha / 255.0)).toInt()
                    ),
                    particle.x,
                    particle.y,
                    renderX.toDouble(),
                    renderY.toDouble()
                )
            }
        }
    }

    private fun distanceSquared(v1: Vec2f, v2: Vec2f): Float {
        val dx = v2.x - v1.x
        val dy = v2.y - v1.y
        return (dx * dx + dy * dy)
    }

    private fun getCurrentRGB(): Color {
        return Color(Color.HSBtoRGB(System.currentTimeMillis() % 4750 / 4750f, 0.7f, 1f))
    }

    fun fill(color: Color, x1: Double, y1: Double, x2: Double, y2: Double) {
        BufferUtils.createFloatBuffer(16)

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

        GL11.glPushMatrix()
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glBegin(GL11.GL_QUADS)
        GL11.glColor4f(g, h, k, f)
        GL11.glVertex2d(x1New, y2New)
        GL11.glVertex2d(x2New, y2New)
        GL11.glVertex2d(x2New, y1New)
        GL11.glVertex2d(x1New, y1New)
        GL11.glEnd()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glPopMatrix()
    }

    private fun gradientLineScreen(start: Color, end: Color, x: Double, y: Double, x1: Double, y1: Double) {
        val g = start.red / 255f
        val h = start.green / 255f
        val k = start.blue / 255f
        val f = start.alpha / 255f
        val g1 = end.red / 255f
        val h1 = end.green / 255f
        val k1 = end.blue / 255f
        val f1 = end.alpha / 255f

        GL11.glPushMatrix()
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glBegin(GL11.GL_LINES)
        GL11.glColor4f(g, h, k, f)
        GL11.glVertex3d(x, y, 0.0)
        GL11.glColor4f(g1, h1, k1, f1)
        GL11.glVertex3d(x1, y1, 0.0)
        GL11.glEnd()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glPopMatrix()
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
}
