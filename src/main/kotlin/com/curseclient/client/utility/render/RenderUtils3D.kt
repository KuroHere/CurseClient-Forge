package com.curseclient.client.utility.render

import com.curseclient.CurseClient
import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.utility.extension.mixins.renderPosX
import com.curseclient.client.utility.extension.mixins.renderPosY
import com.curseclient.client.utility.extension.mixins.renderPosZ
import com.curseclient.client.utility.extension.mixins.timer
import com.curseclient.client.utility.render.ColorUtils.b
import com.curseclient.client.utility.render.ColorUtils.g
import com.curseclient.client.utility.render.ColorUtils.r
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL32
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin


object RenderUtils3D {
    val mc: Minecraft = Minecraft.getMinecraft()
    private val glCapMap: Map<Int, Boolean> = HashMap()

    val viewerPos get() =
        Vec3d(mc.renderManager.viewerPosX, mc.renderManager.viewerPosY, mc.renderManager.viewerPosZ)

    fun SafeClientEvent.drawTrace(e: EntityLivingBase, partialTicks: Float, color: Color, width: Float) {
        if (mc.renderViewEntity == null) return
        if (mc.renderManager.renderViewEntity == null) return

        val height = e.entityBoundingBox.maxY - e.entityBoundingBox.minY

        GlStateManager.enableBlend()
        GlStateManager.shadeModel(GL_SMOOTH)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        glDisable(GL_DEPTH_TEST)
        glDisable(GL_LIGHTING)
        glLineWidth(width)
        glPushMatrix()
        glDepthMask(false)
        glColor4d(color.red / 255.0, color.green / 255.0, color.blue / 255.0, color.alpha / 255.0)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_TEXTURE_2D)
        glBegin(GL_LINES)
        val v = Vec3d(0.0, 0.0, 1.0).rotatePitch(-Math.toRadians(mc.renderViewEntity!!.rotationPitch.toDouble()).toFloat())
            .rotateYaw(-Math.toRadians(mc.renderViewEntity!!.rotationYaw.toDouble()).toFloat())

        glVertex3d(v.x, player.getEyeHeight() + v.y, v.z)
        val x = e.lastTickPosX + (e.posX - e.lastTickPosX) * partialTicks
        val y = e.lastTickPosY + (e.posY - e.lastTickPosY) * partialTicks
        val z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * partialTicks
        glVertex3d(x - mc.renderManager.renderPosX, y - mc.renderManager.renderPosY + height / 2.0, z - mc.renderManager.renderPosZ)
        glEnd()
        glDepthMask(true)
        GlStateManager.shadeModel(GL_FLAT)
        glDisable(GL_LINE_SMOOTH)
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }

    fun drawEntityBox(entity: Entity, color: Color, outline: Boolean) {
        val renderManager = mc.renderManager
        val timer: net.minecraft.util.Timer = mc.timer
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        enableGlCap(GL_BLEND)
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST)
        glDepthMask(false)
        val x: Double = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks
            - renderManager.renderPosX)
        val y: Double = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks
            - renderManager.renderPosY)
        val z: Double = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks
            - renderManager.renderPosZ)
        val entityBox: AxisAlignedBB = entity.entityBoundingBox
        val axisAlignedBB = AxisAlignedBB(
            entityBox.minX - entity.posX + x - 0.05,
            entityBox.minY - entity.posY + y,
            entityBox.minZ - entity.posZ + z - 0.05,
            entityBox.maxX - entity.posX + x + 0.05,
            entityBox.maxY - entity.posY + y + 0.15,
            entityBox.maxZ - entity.posZ + z + 0.05
        )
        if (outline) {
            glLineWidth(1f)
            enableGlCap(GL_LINE_SMOOTH)
            glColor(color.red, color.green, color.blue, 95);
            drawSelectionBoundingBox(axisAlignedBB)
        }
        glColor(color.red, color.green, color.blue, (if (outline) 26 else 35))
        drawFilledBox(axisAlignedBB)
        GlStateManager.resetColor()
        glDepthMask(true)
        resetCaps()
    }

    private fun drawLine3D(x: Double, y: Double, z: Double, x1: Double, y1: Double, z1: Double, thickness: Float, hex: Int) {
        val red = (hex shr 16 and 0xFF) / 255.0f
        val green = (hex shr 8 and 0xFF) / 255.0f
        val blue = (hex and 0xFF) / 255.0f
        val alpha = (hex shr 24 and 0xFF) / 255.0f
        glLineWidth(thickness)
        glEnable(GL32.GL_DEPTH_CLAMP)
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(x, y, z).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        glDisable(GL32.GL_DEPTH_CLAMP)
    }

    private fun drawBoundingBox(bb: AxisAlignedBB, width: Float, red: Float, green: Float, blue: Float, alpha: Float) {
        glLineWidth(width)
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, 0.0f).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, 0.0f).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, 0.0f).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, 0.0f).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, 0.0f).endVertex()
        tessellator.draw()
    }

    fun drawBoundingBox(bb: AxisAlignedBB, width: Float) {
        glLineWidth(width)
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).endVertex()
        tessellator.draw()
    }

    fun drawBoundingBox(bb: AxisAlignedBB, width: Float, color: Int) {
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f
        drawBoundingBox(bb, width, red, green, blue, alpha)
    }


    fun renderFaceMesh(bb: AxisAlignedBB, face: EnumFacing, stepSize: Double, width: Float, hex: Int) {
        when (face) {
            EnumFacing.NORTH -> {
                run {
                    var i = bb.minX
                    while (i <= bb.maxX) {
                        drawLine3D(i, bb.minY, bb.minZ, i, bb.maxY, bb.minZ, width, hex)
                        i += stepSize
                    }
                }
                var i = bb.minY
                while (i <= bb.maxY) {
                    drawLine3D(bb.minX, i, bb.minZ, bb.maxX, i, bb.minZ, width, hex)
                    i += stepSize
                }
            }
            EnumFacing.SOUTH -> {
                run {
                    var i = bb.minX
                    while (i <= bb.maxX) {
                        drawLine3D(i, bb.minY, bb.maxZ, i, bb.maxY, bb.maxZ, width, hex)
                        i += stepSize
                    }
                }
                var i = bb.minY
                while (i <= bb.maxY) {
                    drawLine3D(bb.minX, i, bb.maxZ, bb.maxX, i, bb.maxZ, width, hex)
                    i += stepSize
                }
            }
            EnumFacing.EAST -> {
                run {
                    var i = bb.minZ
                    while (i <= bb.maxZ) {
                        drawLine3D(bb.maxX, bb.minY, i, bb.maxX, bb.maxY, i, width, hex)
                        i += stepSize
                    }
                }
                var i = bb.minY
                while (i <= bb.maxY) {
                    drawLine3D(bb.maxX, i, bb.minZ, bb.maxX, i, bb.maxZ, width, hex)
                    i += stepSize
                }
            }
            EnumFacing.WEST -> {
                run {
                    var i = bb.minZ
                    while (i <= bb.maxZ) {
                        drawLine3D(bb.minX, bb.minY, i, bb.minX, bb.maxY, i, width, hex)
                        i += stepSize
                    }
                }
                var i = bb.minY
                while (i <= bb.maxY) {
                    drawLine3D(bb.minX, i, bb.minZ, bb.minX, i, bb.maxZ, width, hex)
                    i += stepSize
                }
            }
            EnumFacing.UP -> {
                run {
                    var i = bb.minX
                    while (i <= bb.maxX) {
                        drawLine3D(i, bb.maxY, bb.minZ, i, bb.maxY, bb.maxZ, width, hex)
                        i += stepSize
                    }
                }
                var i = bb.minZ
                while (i <= bb.maxZ) {
                    drawLine3D(bb.minX, bb.maxY, i, bb.maxX, bb.maxY, i, width, hex)
                    i += stepSize
                }
            }
            EnumFacing.DOWN -> {
                run {
                    var i = bb.minX
                    while (i <= bb.maxX) {
                        drawLine3D(i, bb.minY, bb.minZ, i, bb.minY, bb.maxZ, width, hex)
                        i += stepSize
                    }
                }
                var i = bb.minZ
                while (i <= bb.maxZ) {
                    drawLine3D(bb.minX, bb.minY, i, bb.maxX, bb.minY, i, width, hex)
                    i += stepSize
                }
            }
        }
    }

    fun glColor(red: Int, green: Int, blue: Int, alpha: Int) {
        GlStateManager.color(red / 255f, green / 255f, blue / 255f, alpha / 255f)
    }

    fun glColor(color: Color) {
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f
        val alpha = color.alpha / 255f
        GlStateManager.color(red, green, blue, alpha)
    }

    fun glColor(hex: Int) {
        val alpha = (hex shr 24 and 0xFF) / 255f
        val red = (hex shr 16 and 0xFF) / 255f
        val green = (hex shr 8 and 0xFF) / 255f
        val blue = (hex and 0xFF) / 255f
        GlStateManager.color(red, green, blue, alpha)
    }


    private fun drawSelectionBoundingBox(boundingBox: AxisAlignedBB) {
        val tessellator = Tessellator.getInstance()
        val builder = tessellator.buffer
        builder.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)

        // Lower Rectangle
        builder.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        builder.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        builder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        builder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        builder.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()

        // Upper Rectangle
        builder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        builder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        builder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        builder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        builder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()

        // Upper Rectangle
        builder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        builder.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        builder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        builder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        builder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        builder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        tessellator.draw()
    }

    private fun drawFilledBox(axisAlignedBB: AxisAlignedBB) {
        val tessellator = Tessellator.getInstance()
        val builder = tessellator.buffer
        builder.begin(7, DefaultVertexFormats.POSITION)
        builder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        builder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        tessellator.draw()
    }

    fun begin3D() {
        GlStateManager.disableAlpha()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.disableTexture2D()
        GlStateManager.disableDepth()
        //GlStateManager.disableLighting();
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
    }

    fun end3D() {
        glDisable(GL_LINE_SMOOTH)
        //GlStateManager.enableLighting();
        GlStateManager.enableDepth()
        GlStateManager.enableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.enableAlpha()
    }

    /**
     * GL CAP MANAGER
     *
     * TODO: Remove gl cap manager and replace by something better
     */

    fun resetCaps() {
        glCapMap.forEach(RenderUtils3D::setGlState)
    }

    fun enableGlCap(cap: Int) {
        setGlCap(cap, true)
    }

    fun enableGlCap(vararg caps: Int) {
        for (cap in caps) setGlCap(cap, true)
    }

    fun disableGlCap(cap: Int) {
        setGlCap(cap, true)
    }

    fun disableGlCap(vararg caps: Int) {
        for (cap in caps) setGlCap(cap, false)
    }

    fun setGlCap(cap: Int, state: Boolean) {
        glCapMap.getOrDefault(cap, glGetBoolean(cap))
        setGlState(cap, state)
    }

    fun setGlState(cap: Int, state: Boolean) {
        if (state) glEnable(cap) else glDisable(cap)
    }

}