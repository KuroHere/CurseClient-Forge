package com.curseclient.client.module.impls.visual

import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.RenderUtils2D.glColor
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityEnderPearl
import net.minecraft.entity.item.EntityExpBottle
import net.minecraft.entity.projectile.*
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11

object Predict: Module(
    "Predict",
    "Predict entity fall trajectory",
    Category.VISUAL
) {
    private val width by setting("LineWidth", 1.0, 0.1, 3.0, 0.1)

    private val pearl by setting("Pearl", false)
    private val snowBall by setting("SnowBall", false)
    private val potions by setting("Potions", false)
    private val egg by setting("Egg", false)
    private val arrow by setting("Arrow", false)
    private val fireball by setting("FireBall", false)
    private val exp by setting("Exp", false)

    init {
        safeListener<Render3DEvent> {
            drawLine(it)
        }
    }

    private fun drawLine(event: Render3DEvent) {
        val ix = -(mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * event.partialTicks)
        val iy = -(mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * event.partialTicks)
        val iz = -(mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * event.partialTicks)

        GlStateManager.pushMatrix()
        GlStateManager.translate(ix, iy, iz)
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GL11.glLineWidth(width.toFloat())
        GL11.glBegin(GL11.GL_LINES)

        for (entity in mc.world.loadedEntityList) {
            when {
                entity is EntityEnderPearl && pearl -> calculateEntity(entity, 0.03, 0.8, false)
                entity is EntityArrow && arrow -> calculateEntity(entity, 0.05, 0.6, false)
                entity is EntitySnowball && snowBall -> calculateEntity(entity, 0.05, 0.6, false)
                entity is EntityEgg && egg -> calculateEntity(entity, 0.05, 0.6, false)
                entity is EntityPotion && potions -> calculateEntity(entity, 0.05, 0.6, false)
                entity is EntityExpBottle && exp -> calculateEntity(entity, 0.05, 0.6, false)
                entity is EntitySmallFireball && fireball -> calculateEntity(entity, 0.05, 0.6, false)
            }
        }

        GL11.glEnd()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
        GlStateManager.resetColor()
        GlStateManager.popMatrix()
    }

    private fun calculateEntity(
        e: Entity,
        g: Double,
        water: Double,
        r: Boolean
    ) {
        var motionX = e.motionX
        var motionY = e.motionY
        var motionZ = e.motionZ
        var x = e.posX
        var y = e.posY
        var z = e.posZ
        Trails.ix = e.posX
        Trails.iy = e.posY
        Trails.iz = e.posZ

        for (i in 0 until 300) {
            if (r) glColor(-1)
            Trails.ix = x
            Trails.iy = y
            Trails.iz = z
            x += motionX
            y += motionY
            z += motionZ

            if (mc.world.getBlockState(BlockPos(x.toInt(), y.toInt(), z.toInt())).block == Blocks.WATER) {
                motionX *= water
                motionY *= water
                motionZ *= water
            } else {
                motionX *= 0.99
                motionY *= 0.99
                motionZ *= 0.99
            }

            motionY -= g
            val pos = Vec3d(x, y, z)

            if (mc.world.rayTraceBlocks(Vec3d(Trails.ix, Trails.iy, Trails.iz), pos) != null) {
                if (mc.world.rayTraceBlocks(Vec3d(Trails.ix, Trails.iy, Trails.iz), pos)!!.typeOfHit == RayTraceResult.Type.ENTITY) {
                    break
                }
                break
            }

            if (y <= 0) break
            if (e.motionZ == 0.0 && e.motionX == 0.0 && e.motionY == 0.0) continue

            Trails.lastPoss[e] = Vec3d(Trails.ix, Trails.iy, Trails.iz)

            val numberOfSegments = 2
            var j = 0
            while (j <= numberOfSegments) {
                val colors: FloatArray = ColorUtils.rgb(ColorUtils.getColorStyle(((i * 2).toFloat()), HUD.themeColor))

                val startColor = floatArrayOf(colors[0], colors[1], colors[2], 1.0f)
                val endColor = floatArrayOf(colors[0], colors[1], colors[2], 1.0f)

                j++

                val deltaX = (x - Trails.ix) / numberOfSegments
                val deltaY = (y - Trails.iy) / numberOfSegments
                val deltaZ = (z - Trails.iz) / numberOfSegments

                val deltaColor = FloatArray(4)
                for (o in 0 until 4) {
                    deltaColor[o] = (endColor[o] - startColor[o]) / numberOfSegments
                }

                for (o in 0 until numberOfSegments) {
                    val currentColor = floatArrayOf(
                        startColor[0] + o * deltaColor[0],
                        startColor[1] + o * deltaColor[1],
                        startColor[2] + o * deltaColor[2],
                        startColor[3] + o * deltaColor[3]
                    )

                    GL11.glColor4f(currentColor[0], currentColor[1], currentColor[2], currentColor[3])

                    GL11.glVertex3d(Trails.ix + o * deltaX, Trails.iy + o * deltaY, Trails.iz + o * deltaZ)
                    GL11.glVertex3d(Trails.ix + (o + 1) * deltaX, Trails.iy + (o + 1) * deltaY, Trails.iz + (o + 1) * deltaZ)
                }
            }
            Trails.i1[e] = i
        }
    }
}