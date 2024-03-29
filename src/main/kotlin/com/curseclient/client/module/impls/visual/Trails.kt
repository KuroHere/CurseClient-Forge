package com.curseclient.client.module.impls.visual

import com.curseclient.client.event.events.MoveEvent
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.listener
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.MathUtils.normalize
import com.curseclient.client.utility.render.ColorUtils.b
import com.curseclient.client.utility.render.ColorUtils.g
import com.curseclient.client.utility.render.ColorUtils.glColor
import com.curseclient.client.utility.render.ColorUtils.r
import com.curseclient.client.utility.render.ColorUtils.setAlphaD
import com.curseclient.client.utility.render.RenderTessellator
import com.curseclient.client.utility.render.RenderUtils2D.glColor
import com.curseclient.client.utility.render.RenderUtils3D
import com.curseclient.client.utility.render.graphic.GLUtils.draw
import com.curseclient.client.utility.render.graphic.GLUtils.glVertex
import com.curseclient.client.utility.render.graphic.GLUtils.matrix
import com.curseclient.client.utility.render.graphic.GLUtils.renderGL
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityEnderPearl
import net.minecraft.entity.item.EntityExpBottle
import net.minecraft.entity.projectile.*
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object Trails : Module(
    "Breadcrumbs",
    "Draws a walking path",
    Category.VISUAL
) {
    private val page by setting("Page", Page.Target)

    private val mode by setting("Mode", Mode.Normal, { page == Page.General})

    private val self by setting("DrawSelf", true, { page == Page.Target})
    private val other by setting("DrawOther", true, { page == Page.Target})
    private val length by setting("Length", 10.0, 5.0, 50.0, 1.0, { page == Page.General})
    private val lineWidth by setting("Width", 1.0, 1.0, 8.0, 1.0, { page == Page.General})
    private val seeThroughWalls by setting("Walls", true, { page == Page.General})
    private val onlyThirdPerson by setting("OnlyThirdPerson", true, { page == Page.General})
    private val positions = HashMap<String, ArrayList<TrailPoint>>()

    private var dimension = -100

    var x: Double = 0.0
    var y: Double = 0.0
    var z: Double = 0.0

    var e: Entity? = null

    val lastPoss: HashMap<Entity, Vec3d> = HashMap()
    val i1: HashMap<Entity, Int> = HashMap()
    var ix = 0.0
    var iy = 0.0
    var iz = 0.0

    private enum class Page {
        General,
        Target
    }

    private enum class Mode {
        Normal,
        Line,
    }

    override fun onEnable() {
        dimension = -100
    }

    init {
        safeListener<MoveEvent> {
            if (other) {
                val otherPlayers = mc.world.playerEntities ?: return@safeListener
                otherPlayers.forEach { player ->
                    if (player != mc.player) {
                        val playerPositions = positions.getOrPut(player.name) { ArrayList() }
                        playerPositions.add(TrailPoint(player.positionVector.add(0.0, 0.1, 0.0)))
                    }
                }
            }
            if (self) {
                positions.getOrPut(mc.player.name) { ArrayList() }
                    .add(TrailPoint(mc.player.positionVector.add(0.0, 0.1, 0.0)))
            }
        }

        listener<TickEvent.ClientTickEvent> {
            val iterator = positions.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val value = entry.value

                value.removeIf { it.shouldRemove() }

                if (value.isEmpty()) {
                    iterator.remove()
                }
            }
        }

        safeListener<Render3DEvent> {
            if (mc.world == null) positions.clear()
            if (other) {
                renderOtherPlayersTrails()
            }
            if (self) {
                renderPlayerTrails(mc.player.name)
            }
        }
    }

    private fun renderOtherPlayersTrails() {
        positions.entries.filter { it.key != mc.player.name }.forEach { entry ->
            val posList = entry.value
            if (posList.size > 1) {
                renderTrail(posList)
            }
        }
    }

    private fun renderPlayerTrails(playerName: String) {
        val posList = positions[playerName]
        if (posList != null && posList.size > 1) {
            renderTrail(posList)
        }
    }

    private fun renderTrail(posList: ArrayList<TrailPoint>) {
        if (seeThroughWalls) {
            GlStateManager.disableDepth()
        }
        if (dimension != mc.player.dimension) {
            dimension = mc.player.dimension
            positions.clear()
            return
        }

        if (mc.gameSettings.thirdPersonView == 0 && onlyThirdPerson) return

        renderGL {
            matrix {
                glDisable(GL_ALPHA_TEST)
                glLineWidth(lineWidth.toFloat())

                when (mode) {
                    Mode.Normal -> drawNormal(posList)
                    Mode.Line -> drawLine(posList)
                }

                glEnable(GL_ALPHA_TEST)
            }
        }
        if (seeThroughWalls) {
            GlStateManager.enableDepth()
        }
    }

    private fun drawNormal(trailList: ArrayList<TrailPoint>) {
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

    }

    private fun drawLine(posList: ArrayList<TrailPoint>) {
        draw(GL_LINE_STRIP) {
            posList.forEach { point ->
                point.setColor(1.0)
                vertex(point.pos)
            }
        }

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
            if (mc.world == null || mc.player == null || mc.player.isDead) {
                return true
            }
            return (mc.player.ticksExisted - initTick).toDouble() > length + 10.0
        }

        private fun getAlpha(): Double {
            val i = mc.player.ticksExisted.toDouble() - initTick.toDouble() + RenderTessellator.partialTicks
            return normalize(i, 0.0, length, 1.0, 0.0)
        }
    }
}