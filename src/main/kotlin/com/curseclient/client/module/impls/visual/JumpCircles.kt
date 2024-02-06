package com.curseclient.client.module.impls.visual

import com.curseclient.client.event.events.JumpEvent
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.module.impls.client.HUD.getColorByProgress
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.mixins.renderPosX
import com.curseclient.client.utility.extension.mixins.renderPosY
import com.curseclient.client.utility.extension.mixins.renderPosZ
import com.curseclient.client.utility.extension.mixins.timer
import com.curseclient.client.utility.math.FPSCounter
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.math.MathUtils.normalize
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.glColor
import com.curseclient.client.utility.render.ColorUtils.setAlphaD
import com.curseclient.client.utility.render.RenderUtils3D
import com.curseclient.client.utility.render.graphic.GLUtils
import com.curseclient.client.utility.render.graphic.GLUtils.draw
import com.curseclient.client.utility.render.graphic.GLUtils.glVertex
import com.curseclient.client.utility.render.graphic.GLUtils.matrix
import com.curseclient.client.utility.render.graphic.GLUtils.translateGL
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object JumpCircles : Module(
    "JumpCircles",
    "Amazing circles",
    Category.VISUAL
) {
    private val mode by setting("Mode", Mode.Normal)
    private val speed by setting("Speed", 1.0, 1.0, 5.0, 0.01, { mode == Mode.Text })

    private val radius by setting("Radius", 1.0, 0.5, 2.0, 0.05)
    private val duration by setting("Duration", 1.5, 0.5, 3.0, 0.1, { mode == Mode.Normal })
    private val outlineWidth by setting("Outline Width", 2.0, 1.0, 5.0, 0.1, { mode == Mode.Normal })
    private val rotateSpeed by setting("Rotate Speed", 2.0, 0.0, 10.0, 0.1, { mode == Mode.Normal })
    private val glowSize by setting("Glow Size", 1.0, 0.5, 5.0, 0.05, { mode == Mode.Normal })
    private val glowAlpha by setting("Glow Alpha", 1.0, 0.0, 1.0, 0.05, { mode == Mode.Normal })

    private var textCircles = ArrayList<Circle>()
    private val circles = ArrayList<JumpCircle>()
    private var lastJumpTick = 0 // :nerd:
    private var ticks = 0

    private enum class Mode {
        Normal,
        Text // Don't use that
    }

    init {
        safeListener<TickEvent.ClientTickEvent> {
            ticks++
        }

        safeListener<JumpEvent> {
            if (ticks - lastJumpTick <= 2) return@safeListener

            textCircles.add(Circle(player.positionVector))
            circles.add(JumpCircle(player.positionVector.add(0.0, 0.1, 0.0), System.currentTimeMillis()))
            lastJumpTick = ticks
        }

        safeListener<Render3DEvent> {
            when (mode) {
                Mode.Text -> {
                    update()
                    textCircles.forEach {
                        matrix { it.draw() }
                    }
                }

                Mode.Normal -> {
                    GLUtils.renderGL {
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
        }
    }

    /**
     * @param circle Jump Circle.
     * @param radius Circle Radius.
     * @param alpha  Circle Transparency.
     */
    private fun drawJumpCircle(
        circle: Circle,
        radius: Float,
        alpha: Float
    ) {
        val x: Double = circle.pos.x
        val y: Double = circle.pos.y + 0.1
        val z: Double = circle.pos.z
        GlStateManager.translate(x, y, z)
        GlStateManager.rotate(circle.factor * 70, 0f, -1f, 0f)
        mc.textureManager.bindTexture(ResourceLocation("textures/circle.png"))
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        buffer.begin(GL_QUAD_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR)
        for (i in 0 .. 360 step 5) {
            val colors: FloatArray = ColorUtils.rgb(ColorUtils.getColorStyle(((i * 2).toFloat()), HUD.themeColor))

            val sin = (sin(Math.toRadians(i + 0.1).toFloat()) * radius).toDouble()
            val cos = (cos(Math.toRadians(i + 0.1).toFloat()) * radius).toDouble()

            buffer.pos(0.0, 0.0, 0.0).color(colors[0], colors[1], colors[2], MathHelper.clamp(alpha, 0f, 1f)).tex(0.5, 0.5).endVertex()
            buffer.pos(sin, 0.0, cos).color(colors[0], colors[1], colors[2], MathHelper.clamp(alpha, 0f, 1f)).tex(((sin / (2 * radius)) + 0.5f), ((cos / (2 * radius)) + 0.5f)).endVertex()
        }
        tessellator.draw()
        GlStateManager.rotate(-circle.factor * 70, 0f, -1f, 0f)
        GlStateManager.translate(-x, -y, -z)
    }

    fun update() {
        for (circle in textCircles) {
            circle.factor = circle.fast(circle.factor, radius.toFloat() + 0.1f, speed.toFloat())
            //circle.shadow = circle.fast(circle.shadow, shadow.toFloat(), speed.toFloat())
            circle.alpha = circle.fast(circle.alpha, 0F, speed.toFloat())
        }
        if (textCircles.size >= 1) textCircles.removeIf { circle: Circle -> circle.alpha <= 0.005f }
    }

    class Circle(
        val pos: Vec3d,
    ) {
        var factor = 0F
        var alpha = 5F
        var shadow = 40F
        var ticks = 0F

        fun fast(end: Float, start: Float, multiple: Float) = ((1 - clamp((FPSCounter.deltaTime * multiple), 0.0, 1.0)) * end + clamp((FPSCounter.deltaTime * multiple), 0.0, 1.0) * start).toFloat()

        fun draw() {
            setupRenderSettings()
            for (circle in textCircles) {
                drawJumpCircle(circle, circle.factor, circle.alpha)
            }
            restoreRenderSettings()
        }

        private fun setupRenderSettings() {
            GlStateManager.disableLighting()
            GlStateManager.depthMask(false)
            GlStateManager.enableBlend()
            GlStateManager.disableCull()
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE)
            GlStateManager.disableAlpha()
            GlStateManager.disableDepth()
            GlStateManager.shadeModel(7425)
            GlStateManager.disableTexture2D()
            GlStateManager.translate(
                -mc.renderManager.renderPosX,
                -mc.renderManager.renderPosY,
                -mc.renderManager.renderPosZ
            )
        }

        private fun restoreRenderSettings() {
            GlStateManager.enableAlpha()
            GlStateManager.enableDepth()
            GlStateManager.enableCull()
            glEnable(GL_TEXTURE_2D)
            glDisable(GL_BLEND)
            GlStateManager.depthMask(true)
            GlStateManager.enableTexture2D()
            GlStateManager.shadeModel(7424)
            GlStateManager.disableBlend()
        }
    }

    private class JumpCircle(
        val pos: Vec3d,
        val spawnTime: Long
    ) {
        val progress get() =
            clamp((System.currentTimeMillis() - spawnTime) / (duration * 1000.0), 0.0, 1.0)

        fun draw() {
            drawCircle(progress, radius * 0.75)
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