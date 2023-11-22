package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.events.MoveEvent
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.listener
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.entity.interpolatedPosition
import com.curseclient.client.utility.math.MathUtils.normalize
import com.curseclient.client.utility.render.ColorUtils.b
import com.curseclient.client.utility.render.ColorUtils.g
import com.curseclient.client.utility.render.ColorUtils.glColor
import com.curseclient.client.utility.render.ColorUtils.r
import com.curseclient.client.utility.render.ColorUtils.setAlphaD
import com.curseclient.client.utility.render.RenderTessellator
import com.curseclient.client.utility.render.RenderUtils3D
import com.curseclient.client.utility.render.graphic.GLUtils.draw
import com.curseclient.client.utility.render.graphic.GLUtils.glVertex
import com.curseclient.client.utility.render.graphic.GLUtils.renderGL
import com.curseclient.client.utility.render.shader.RoundedUtil.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin


object Trails : Module(
    "Breadcrumbs",
    "Draws a walking path",
    Category.VISUAL
) {
    private val mode by setting("Mode", Mode.Normal)
    private val seeThroughWalls by setting("Walls", true)
    private val length by setting("Length", 10.0, 5.0, 50.0, 1.0)
    private val lineWidth by setting("Width", 1.0, 1.0, 8.0, 1.0)
    private val onlyThirdPerson by setting("Only Third Person", true)

    private val positions = ArrayList<TrailPoint>()

    private var dimension = -100

    private enum class Mode {
        Normal,
        Line
    }

    enum class ModeC {
        Custom,
        Client,
    }

    override fun onEnable() {
        dimension = -100
    }

    init {
        safeListener<MoveEvent> {
            positions.add(TrailPoint(player.positionVector.add(0.0, 0.1, 0.0)))
        }

        listener<TickEvent.ClientTickEvent> {
            positions.removeIf { it.shouldRemove() }
        }

        safeListener<Render3DEvent> {
            if (dimension != player.dimension) {
                dimension = player.dimension
                positions.clear()
                return@safeListener
            }

            if (mc.gameSettings.thirdPersonView == 0 && onlyThirdPerson) return@safeListener

            renderGL {
                glDisable(GL_ALPHA_TEST)
                glLineWidth(lineWidth.toFloat())

                val posList = ArrayList(positions)
                posList.add(TrailPoint(player.interpolatedPosition.add(0.0, 0.1, 0.0)))

                when (mode) {
                    Mode.Normal -> drawNormal(posList)
                    Mode.Line -> drawLine(posList)
                }

                glEnable(GL_ALPHA_TEST)
            }
        }
    }

    private fun drawNormal(trailList: ArrayList<TrailPoint>) {
        if (seeThroughWalls) {
            GlStateManager.disableDepth()
        }
        // main
        draw(GL_QUAD_STRIP) {
            trailList.forEach { point ->
                point.setColor(0.7)
                vertex(point.pos)
                vertex(point.pos.add(0.0, point.height, 0.0))
            }
        }

        val width = lineWidth * 0.015

        // top line
        draw(GL_QUAD_STRIP) {
            trailList.forEach { point ->
                point.setColor(1.0)
                vertex(point.pos.add(0.0, point.height, 0.0))

                point.setColor(0.0)
                vertex(point.pos.add(0.0, point.height + width, 0.0))
            }
        }

        draw(GL_QUAD_STRIP) {
            trailList.forEach { point ->
                point.setColor(1.0)
                vertex(point.pos.add(0.0, point.height, 0.0))

                point.setColor(0.0)
                vertex(point.pos.add(0.0, point.height - width, 0.0))
            }
        }

        // bottom line
        draw(GL_QUAD_STRIP) {
            trailList.forEach { point ->
                point.setColor(1.0)
                vertex(point.pos)

                point.setColor(0.0)
                vertex(point.pos.add(0.0, width, 0.0))
            }
        }

        draw(GL_QUAD_STRIP) {
            trailList.forEach { point ->
                point.setColor(1.0)
                vertex(point.pos)

                point.setColor(0.0)
                vertex(point.pos.add(0.0, -width, 0.0))
            }
        }
        if (seeThroughWalls) {
            GlStateManager.enableDepth()
        }
    }

    private fun drawLine(posList: ArrayList<TrailPoint>) {
        if (seeThroughWalls) {
            GlStateManager.disableDepth()
        }
        draw(GL_LINE_STRIP) {
            posList.forEach { point ->
                point.setColor(1.0)
                vertex(point.pos)
            }
        }
        if (seeThroughWalls) {
            GlStateManager.enableDepth()
        }
    }

    //From rise, alan gave me this
    private fun drawFilledCircleNoGL(x: Int, y: Int, r: Double, c: Int, quality: Int) {
        resetColor()
        setAlphaLimit(0f)
        startBlend()
        GlStateManager.disableTexture2D();
        color(c)
        glBegin(GL_TRIANGLE_FAN)
        for (i in 0..360 / quality) {
            val x2 = sin(i * quality * Math.PI / 180) * r
            val y2 = cos(i * quality * Math.PI / 180) * r
            glVertex2d(x + x2, y + y2)
        }
        glEnd()
        GlStateManager.enableTexture2D();
        endBlend();
    }

    private fun vertex(vec3d: Vec3d) {
        vec3d.subtract(RenderUtils3D.viewerPos).glVertex()
    }

    private class TrailPoint(val pos: Vec3d) {
        val height = mc.player.getEyeHeight().toDouble() + 0.15
        val initTick = mc.player.ticksExisted

        fun setColor(alpha: Double) {
            HUD.getColor(initTick + mc.player.ticksExisted).setAlphaD(getAlpha() * alpha).glColor()
        }

        fun color(color: Color, alpha: Double) {
            glColor4f(color.r, color.g, color.b, (getAlpha() * alpha).toFloat())
        }

        fun shouldRemove(): Boolean {
            if (mc.player == null) return true
            if (mc.player.isDead) return true
            return (mc.player.ticksExisted - initTick).toDouble() > length + 10.0
        }

        private fun getAlpha(): Double {
            val i = mc.player.ticksExisted.toDouble() - initTick.toDouble() + RenderTessellator.partialTicks
            return normalize(i, 0.0, length, 1.0, 0.0)
        }
    }
}