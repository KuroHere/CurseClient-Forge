package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.events.JumpEvent
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.client.HUD.getColorByProgress
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.mixins.timer
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.math.MathUtils.normalize
import com.curseclient.client.utility.render.ColorUtils.glColor
import com.curseclient.client.utility.render.ColorUtils.setAlphaD
import com.curseclient.client.utility.render.graphic.GLUtils
import com.curseclient.client.utility.render.graphic.GLUtils.draw
import com.curseclient.client.utility.render.graphic.GLUtils.glVertex
import com.curseclient.client.utility.render.graphic.GLUtils.matrix
import com.curseclient.client.utility.render.graphic.GLUtils.translateGL
import com.curseclient.client.utility.render.RenderUtils3D
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.GL11.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object JumpCircles : Module(
    "JumpCircles",
    "Amazing circles",
    Category.VISUAL
) {
    private val radiusValue by setting("Radius", 1.0, 0.5, 2.0, 0.05)
    private val duration by setting("Duration", 1.5, 0.5, 3.0, 0.1)
    private val outlineWidth by setting("Outline Width", 2.0, 1.0, 5.0, 0.1)
    private val rotateSpeed by setting("Rotate Speed", 2.0, 0.0, 10.0, 0.1)
    private val throughWalls by setting("Through Walls", false)
    private val glowSize by setting("Glow Size", 1.0, 0.5, 5.0, 0.05)
    private val glowAlpha by setting("Glow Alpha", 1.0, 0.0, 1.0, 0.05)

    private val circles = ArrayList<JumpCircle>()
    private var lastJumpTick = 0 // :nerd:
    private var ticks = 0

    init {
        safeListener<TickEvent.ClientTickEvent> {
            ticks++
        }

        safeListener<JumpEvent> {
            if (ticks - lastJumpTick <= 2) return@safeListener

            circles.add(JumpCircle(player.positionVector.add(0.0, 0.1, 0.0), System.currentTimeMillis()))
            lastJumpTick = ticks
        }

        safeListener<Render3DEvent> {
            GLUtils.renderGL {
                if (throughWalls) GlStateManager.disableDepth()
                circles.removeIf { it.progress > 0.99 }
                circles.forEach {
                    matrix {
                        glLineWidth(outlineWidth.toFloat())

                        it.pos.subtract(RenderUtils3D.viewerPos).translateGL()
                        glRotated((player.ticksExisted + mc.timer.renderPartialTicks) * rotateSpeed * -5.0, 0.0, 1.0, 0.0)

                        it.draw()
                    }
                }
            }
        }
    }

    private class JumpCircle(val pos: Vec3d, val spawnTime: Long) {
        val progress get() =
            clamp((System.currentTimeMillis() - spawnTime) / (duration * 1000.0), 0.0, 1.0)

        fun draw() {
            drawCircle(progress, radiusValue * 0.75)
        }
    }

    private fun drawCircle(p: Double, radius: Double) {
        var alpha = 1.0

        if (p < 0.1) alpha = normalize(p, 0.0, 0.1, 0.0, 1.0)
        if (p > 0.75) alpha = normalize(p, 0.75, 1.0, 1.0, 0.0)

        var size = radius
        size *= sin(sqrt(p * 3.14)) * 1.02

        drawGlow(size, size * glowSize * 0.2, 0.2 * alpha * glowAlpha)
        drawGlow(size, size * glowSize * -0.2, 0.2 * alpha * glowAlpha)

        drawGlow(size, size * glowSize * 0.1, 0.2 * alpha * glowAlpha)
        drawGlow(size, size * glowSize * -0.1, 0.2 * alpha * glowAlpha)

        drawGlow(size, size * glowSize * 0.04, 0.15 * alpha * glowAlpha)
        drawGlow(size, size * glowSize * -0.04, 0.15 * alpha * glowAlpha)

        drawGlow(size, size * glowSize * 0.03, 0.2 * alpha * glowAlpha)
        drawGlow(size, size * glowSize * -0.03, 0.2 * alpha * glowAlpha)

        drawOutline(size, 1.0 * alpha)
    }

    private fun drawOutline(radius: Double, alpha: Double) {
        draw(GL_LINE_STRIP) {
            for (i in 0..360 step 5) {
                val p = i.toDouble() / 360.0
                val colorProgress = (if (p > 0.5) 1.0 - p else p) * 2.0
                getColorByProgress(colorProgress).setAlphaD(alpha).glColor()

                val dir = Math.toRadians(i - 180.0)
                val x = -sin(dir) * radius
                val z = cos(dir) * radius
                Vec3d(x, 0.0, z).glVertex()
            }
        }
    }

    private fun drawGlow(radius: Double, glowSize: Double, alpha: Double) {
        draw(GL_QUAD_STRIP) {
            for (i in 0..360 step 5) {
                val p = i.toDouble() / 360.0
                val colorProgress = (if (p > 0.5) 1.0 - p else p) * 2.0
                val color = getColorByProgress(colorProgress)

                val dir = Math.toRadians(i - 180.0)

                val x = -sin(dir) * radius
                val z = cos(dir) * radius

                val glowX = -sin(dir) * (radius + glowSize)
                val glowZ = cos(dir) * (radius + glowSize)

                color.setAlphaD(0.0).glColor()
                Vec3d(glowX, 0.0, glowZ).glVertex()

                color.setAlphaD(alpha).glColor()
                Vec3d(x, 0.0, z).glVertex()
            }
        }
    }
}