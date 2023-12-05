package com.curseclient.client.module.modules.visual.TwoDESP

import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.module.modules.combat.AntiBot.isBot
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.mixins.renderPosX
import com.curseclient.client.utility.extension.mixins.renderPosY
import com.curseclient.client.utility.extension.mixins.renderPosZ
import com.curseclient.client.utility.extension.mixins.timer
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.mixin.accessor.render.AccessorEntityRenderer
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.EntityRenderer
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.math.AxisAlignedBB
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU
import javax.vecmath.Vector3d
import javax.vecmath.Vector4d
import java.awt.Color
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.*

object TwoDESP : Module(
    "2DESP",
    "2D version of ESP",
    Category.VISUAL
) {
    private val viewport: IntBuffer = GLAllocation.createDirectIntBuffer(16)
    private val modelview: FloatBuffer = GLAllocation.createDirectFloatBuffer(16)
    private val projection: FloatBuffer = GLAllocation.createDirectFloatBuffer(16)
    private val vector: FloatBuffer = GLAllocation.createDirectFloatBuffer(4)
    private val mode by setting("Mode", Mode.Classic)

    enum class Mode {
        Real,
        Classic
    }

    init {
        safeListener<Render3DEvent> {
            if (mode.name == "Classic") {
                var amount = 0
                for (entity in mc.world.playerEntities) {
                    if (entity != null) {
                        val name = entity.name
                        if (!entity.isDead && entity != mc.player && name.isNotEmpty() && !entity.isBot() && name != " " && RenderUtils2D.isInViewFrustrum(entity)) {
                            val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX
                            val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY - 0.2
                            val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ

                            GL11.glPushMatrix()
                            GL11.glTranslated(x, y, z)
                            GlStateManager.disableDepth()

                            GL11.glRotated((-mc.renderManager.playerViewY).toDouble(), 0.0, 1.0, 0.0)

                            val width = 1.1f
                            val height = 2.2f
                            val lineWidth = 0.07f

                            draw2DBox(width.toDouble(), height.toDouble(), lineWidth.toDouble(), 0.04, Color(0, 0, 0, 165))

                            if (entity.hurtTime > 0)
                                draw2DBox(width.toDouble(), height.toDouble(), lineWidth.toDouble(), 0.0, Color(255, 30, 30, 255))
                            else
                                draw2DBox(width.toDouble(), height.toDouble(), lineWidth.toDouble(), 0.0, HUD.getColor(0))

                            GlStateManager.enableDepth()
                            GL11.glPopMatrix()
                            amount++
                        }
                    }
                }
            }
            if (mode.name == "Real") {
                val scaleResolution = ScaledResolution(mc)
                val entityRenderer: EntityRenderer = mc.entityRenderer

                val scaleFactor = scaleResolution.scaleFactor
                val renderMng = mc.renderManager

                var amount = 0
                for (p in mc.world.playerEntities) {
                    if (p != null) {
                        val name = p.name
                        if (!p.isDead && p != mc.player && !p.isInvisible && !p.isBot() && name.isNotEmpty() && name != " " && RenderUtils2D.isInViewFrustrum(p)) {
                            val partialTicks = mc.timer.renderPartialTicks
                            val x = p.lastTickPosX + (p.posX - p.lastTickPosX) * partialTicks
                            val y = p.lastTickPosY + (p.posY - p.lastTickPosY) * partialTicks
                            val z = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * partialTicks

                            val width = p.width / 1.5
                            val height = p.height + if (p.isSneaking) -0.3 else 0.2
                            val aabb = AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width)
                            val vectors = listOf(Vector3d(aabb.minX, aabb.minY, aabb.minZ), Vector3d(aabb.minX, aabb.maxY, aabb.minZ), Vector3d(aabb.maxX, aabb.minY, aabb.minZ), Vector3d(aabb.maxX, aabb.maxY, aabb.minZ), Vector3d(aabb.minX, aabb.minY, aabb.maxZ), Vector3d(aabb.minX, aabb.maxY, aabb.maxZ), Vector3d(aabb.maxX, aabb.minY, aabb.maxZ), Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ))

                            var position: Vector4d? = null
                            if (position != null) {
                                val partialTicks = mc.timer.renderPartialTicks
                                val entityRendererAccessor = mc.entityRenderer as AccessorEntityRenderer
                                entityRendererAccessor.invokeSetupCameraTransform(partialTicks, 0)
                            }

                            for (v in vectors) {
                                val v = project2D(scaleFactor, v.x - renderMng.viewerPosX, v.y - renderMng.viewerPosY, v.z - renderMng.viewerPosZ)
                                if (v != null && v.z >= 0.0 && v.z < 1.0) {
                                    if (position == null)
                                        position = Vector4d(v.x, v.y, v.z, 0.0)
                                    position.x = v.x.coerceAtMost(position.x)
                                    position.y = v.y.coerceAtMost(position.y)
                                    position.z = v.x.coerceAtLeast(position.z)
                                    position.w = v.y.coerceAtLeast(position.w)
                                }
                            }

                            if (position != null) {
                                entityRenderer.setupOverlayRendering()
                                val posX = position.x
                                val posY = position.y
                                val endPosX = position.z
                                val endPosY = position.w

                                val w = 0.5f

                                val c = HUD.getColor(0)

                                Utils.lineNoGl(posX - w, posY, posX + w - w, endPosY, c)
                                Utils.lineNoGl(posX, endPosY - w, endPosX, endPosY, c)
                                Utils.lineNoGl(posX - w, posY, endPosX, posY + w, c)
                                Utils.lineNoGl(endPosX - w, posY, endPosX, endPosY, c)

                                val percentage = (endPosY - posY) * p.health / p.maxHealth

                                val distance = 2.0

                                val fractions = floatArrayOf(0.0f, 0.5f, 1.0f)
                                val colors = arrayOf(Color.RED, Color.YELLOW, Color.GREEN)
                                val progress = p.health / p.maxHealth
                                val healthColor = if (p.health >= 0.0f) ColorUtils.blendColors(fractions, colors, progress).brighter() else Color.RED

                                Utils.lineNoGl(posX - w - distance, endPosY - percentage, posX + w - w - distance, endPosY, healthColor)
                            }
                            amount++
                        }
                    }
                }
            }
        }

    }

    fun draw2DBox(width: Double, height: Double, lineWidth: Double, offset: Double, c: Color) {
        Utils.rect(-width / 2 - offset, -offset, width / 4, lineWidth, c)
        Utils.rect(width / 2 - offset, -offset, -width / 4, lineWidth, c)
        Utils.rect(width / 2 - offset, height - offset, -width / 4, lineWidth, c)
        Utils.rect(-width / 2 - offset, height - offset, width / 4, lineWidth, c)
        Utils.rect(-width / 2 - offset, height - offset, lineWidth, -height / 4, c)
        Utils.rect(width / 2 - lineWidth - offset, height - offset, lineWidth, -height / 4, c)
        Utils.rect(width / 2 - lineWidth - offset, -offset, lineWidth, height / 4, c)
        Utils.rect(-width / 2 - offset, -offset, lineWidth, height / 4, c)
    }

    fun project2D(scaleFactor: Int, x: Double, y: Double, z: Double): Vector3d? {
        GL11.glGetFloat(2982, modelview)
        GL11.glGetFloat(2983, projection)
        GL11.glGetInteger(2978, viewport)
        return if (GLU.gluProject(x.toFloat(), y.toFloat(), z.toFloat(), modelview, projection, viewport, vector)) Vector3d(vector.get(0).toDouble() / scaleFactor, (Display.getHeight() - vector.get(1)).toDouble() / scaleFactor, vector.get(2).toDouble()) else null
    }
}
