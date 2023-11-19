package com.curseclient.client.utility.render

import com.curseclient.client.utility.extension.entity.positionVectorPrev
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.render.esp.ESPBox
import com.curseclient.mixin.accessor.AccessorMinecraft
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.ActiveRenderInfo
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL32
import java.awt.Color

object RenderTessellator : Tessellator(0x200000) {
    private val mc = Minecraft.getMinecraft()

    val partialTicks get() =
        if (mc.isGamePaused) (mc as AccessorMinecraft).partialTicksPaused else mc.renderPartialTicks

    fun begin(mode: Int) =
        buffer.begin(mode, DefaultVertexFormats.POSITION_COLOR)

    fun render(mode: Int, block: () -> Unit) {
        begin(mode)
        block()
        render()
    }

    fun render() =
        draw()

    fun prepareGL() {
        GlStateManager.pushMatrix()
        GL11.glLineWidth(1f)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL32.GL_DEPTH_CLAMP)
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
        GlStateManager.disableAlpha()
        GlStateManager.shadeModel(GL11.GL_SMOOTH)
        GlStateManager.disableCull()
        GlStateManager.enableBlend()
        GlStateManager.depthMask(false)
        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()
    }

    fun releaseGL() {
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
        GlStateManager.enableCull()
        GlStateManager.shadeModel(GL11.GL_FLAT)
        GlStateManager.enableAlpha()
        GlStateManager.depthMask(true)
        GL11.glDisable(GL32.GL_DEPTH_CLAMP)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GlStateManager.color(1f, 1f, 1f, 1f)
        GL11.glLineWidth(1f)
        GlStateManager.popMatrix()
    }

    private val viewEntity get() = (mc.renderViewEntity ?: mc.player)

    val camPos: Vec3d
        get() = lerp(viewEntity.positionVectorPrev, viewEntity.positionVector, partialTicks.toDouble()).add(ActiveRenderInfo.getCameraPosition())

    fun drawBox(box: ESPBox, color: Color, sides: List<EnumFacing>) {
        val vertexList = ArrayList<Vec3d>()

        if (sides.contains(EnumFacing.DOWN))
            vertexList.addAll(SquareVec(box.minX, box.maxX, box.minZ, box.maxZ, box.minY, EnumFacing.DOWN).toQuad())

        if (sides.contains(EnumFacing.UP))
            vertexList.addAll(SquareVec(box.minX, box.maxX, box.minZ, box.maxZ, box.maxY, EnumFacing.UP).toQuad())

        if (sides.contains(EnumFacing.NORTH))
            vertexList.addAll(SquareVec(box.minX, box.maxX, box.minY, box.maxY, box.minZ, EnumFacing.NORTH).toQuad())

        if (sides.contains(EnumFacing.SOUTH))
            vertexList.addAll(SquareVec(box.minX, box.maxX, box.minY, box.maxY, box.maxZ, EnumFacing.SOUTH).toQuad())

        if (sides.contains(EnumFacing.WEST))
            vertexList.addAll(SquareVec(box.minY, box.maxY, box.minZ, box.maxZ, box.minX, EnumFacing.WEST).toQuad())

        if (sides.contains(EnumFacing.EAST)) {
            vertexList.addAll(SquareVec(box.minY, box.maxY, box.minZ, box.maxZ, box.maxX, EnumFacing.EAST).toQuad())
        }

        vertexList.forEach { pos ->
            buffer.pos(pos.x, pos.y, pos.z).color(color.red, color.green, color.blue, color.alpha).endVertex()
        }
    }

    fun drawOutline(box: ESPBox, color: Color, sides: List<EnumFacing>) {
        val vertexList = LinkedHashSet<Pair<Vec3d, Vec3d>>()

        if (sides.contains(EnumFacing.DOWN))
            vertexList.addAll(SquareVec(box.minX, box.maxX, box.minZ, box.maxZ, box.minY, EnumFacing.DOWN).toLines())

        if (sides.contains(EnumFacing.UP))
            vertexList.addAll(SquareVec(box.minX, box.maxX, box.minZ, box.maxZ, box.maxY, EnumFacing.UP).toLines())

        if (sides.contains(EnumFacing.NORTH))
            vertexList.addAll(SquareVec(box.minX, box.maxX, box.minY, box.maxY, box.minZ, EnumFacing.NORTH).toLines())

        if (sides.contains(EnumFacing.SOUTH))
            vertexList.addAll(SquareVec(box.minX, box.maxX, box.minY, box.maxY, box.maxZ, EnumFacing.SOUTH).toLines())

        if (sides.contains(EnumFacing.WEST))
            vertexList.addAll(SquareVec(box.minY, box.maxY, box.minZ, box.maxZ, box.minX, EnumFacing.WEST).toLines())

        if (sides.contains(EnumFacing.EAST))
            vertexList.addAll(SquareVec(box.minY, box.maxY, box.minZ, box.maxZ, box.maxX, EnumFacing.EAST).toLines())

        for ((p1, p2) in vertexList) {
            buffer.pos(p1.x, p1.y, p1.z).color(color.red, color.green, color.blue, color.alpha).endVertex()
            buffer.pos(p2.x, p2.y, p2.z).color(color.red, color.green, color.blue, color.alpha).endVertex()
        }
    }

    fun drawLineTo(position: Vec3d, color: Color, thickness: Float) {
        GlStateManager.glLineWidth(thickness)
        buffer.pos(camPos.x, camPos.y, camPos.z).color(color.red, color.green, color.blue, color.alpha).endVertex()
        buffer.pos(position.x, position.y, position.z).color(color.red, color.green, color.blue, color.alpha).endVertex()
    }

    private data class SquareVec(val minX: Double, val maxX: Double, val minZ: Double, val maxZ: Double, val y: Double, val facing: EnumFacing) {
        fun toLines(): Array<Pair<Vec3d, Vec3d>> {
            val quad = this.toQuad()
            return arrayOf(
                Pair(quad[0], quad[1]),
                Pair(quad[1], quad[2]),
                Pair(quad[2], quad[3]),
                Pair(quad[3], quad[0])
            )
        }

        fun toQuad(): Array<Vec3d> {
            return if (this.facing.horizontalIndex != -1) {
                val quad = this.to2DQuad()
                Array(4) { i ->
                    val vec = quad[i]
                    if (facing.axis == EnumFacing.Axis.X) {
                        Vec3d(vec.y, vec.x, vec.z)
                    } else {
                        Vec3d(vec.x, vec.z, vec.y)
                    }
                }
            } else this.to2DQuad()
        }

        fun to2DQuad(): Array<Vec3d> {
            return arrayOf(
                Vec3d(this.minX, this.y, this.minZ),
                Vec3d(this.minX, this.y, this.maxZ),
                Vec3d(this.maxX, this.y, this.maxZ),
                Vec3d(this.maxX, this.y, this.minZ)
            )
        }
    }
}