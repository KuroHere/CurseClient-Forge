package com.curseclient.client.module.impls.visual

import com.curseclient.client.event.events.render.Render2DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.MathUtils.interpolate
import com.curseclient.client.utility.player.RotationUtils.getOldYaw
import com.curseclient.client.utility.player.RotationUtils.getYawToEntity
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11.*


object Indicators: Module(
    "Indicators",
    "Classic radar.",
    Category.VISUAL
) {
    private val arrowShapeProperty by setting("Shape", ArrowsShape.EQUILATERAL)

    private val arrowsRadiusProperty by setting("Radius", 100, 10, 200, 1)
    private val outlineProperty by setting("Outline", false)
    private val fadeOutProperty by setting("Fade Out", true)
    private val scaleUpProperty by setting("Scale Up", true)
    private val arrowsSizeProperty by setting("Size", 6, 3, 30, 1)
    private val stretchProperty by setting("Stretch", 1.5, 1.0, 2.0, 0.05)


    private val playerAlphaMap: Map<EntityPlayer, Float> = HashMap()

    init {
        safeListener<Render2DEvent> {
            val lr = ScaledResolution(mc)
            val middleX = lr.scaledWidth / 2.0f
            val middleY = lr.scaledHeight / 2.0f
            val pt = it.partialTicks

            glPushMatrix()
            glTranslated(middleX + 0.5, middleY.toDouble(), 1.0)

            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            glDisable(GL_TEXTURE_2D)
            glEnable(GL_LINE_SMOOTH)
            glEnable(GL_POLYGON_SMOOTH)
            glLineWidth(1.0f)

            val distortion = stretchProperty
            val color = HUD.themeColor.getColor(player.entityId)

            val outline = outlineProperty
            val shape = arrowShapeProperty

            for (player in mc.world.playerEntities) {
                if (player is EntityOtherPlayerMP && player.isEntityAlive && !player.isInvisible) {

                        val yaw = interpolate(
                            getOldYaw(player).toDouble(),
                            getYawToEntity(player).toDouble(),
                            pt.toDouble()
                        ) -
                            interpolate(
                                mc.player.prevRotationYaw.toDouble(),
                                mc.player.rotationYaw.toDouble(),
                                pt.toDouble()
                            )
                        glPushMatrix()
                        glScaled(distortion, 1.0, 1.0)
                        glRotatef(yaw.toFloat(), 0f, 0f, 1f)
                        glTranslated(
                            0.0,
                            -arrowsRadiusProperty - arrowsSizeProperty,
                            0.0
                        )
                        val correction = 1 / distortion
                        glScaled(correction, 1.0, 1.0)

                        var offset = 0.0

                        var arrowSize = arrowsSizeProperty
                        val fadeOut = playerAlphaMap.getOrDefault(player, 1.0f)

                        if (scaleUpProperty)
                            arrowSize += arrowSize * fadeOut / 3

                        if (outline) {
                            glBegin(GL_LINE_LOOP)
                            glColor4ub(
                                (color shr 16 and 0xFF).toByte(),
                                (color shr 8 and 0xFF).toByte(),
                                (color and 0xFF).toByte(),
                                0xFF.toByte()
                            )
                            glVertex2d(0.0, 0.0)

                            when (shape) {
                                ArrowsShape.ARROW -> {
                                    offset = (arrowSize / 3.0)
                                    glVertex2d(-arrowSize + offset, arrowSize)
                                    glVertex2d(0.0, arrowSize - offset)
                                    glVertex2d(arrowSize - offset, arrowSize)
                                }

                                ArrowsShape.ISOSCELES -> {
                                    offset = (arrowSize / 3.0)
                                    glVertex2d(-arrowSize + offset, arrowSize)
                                    glVertex2d(arrowSize - offset, arrowSize)
                                }

                                ArrowsShape.EQUILATERAL -> {
                                    glVertex2d(-arrowSize, arrowSize)
                                    glVertex2d(arrowSize, arrowSize)
                                }
                            }

                            glEnd()
                        }

                        val colorAlpha = color shr 24 and 0xFF

                        glBegin(if (shape == ArrowsShape.ARROW) GL_POLYGON else GL_TRIANGLE_STRIP)

                        if (fadeOutProperty) {
                            glColor4ub(
                                (color shr 16 and 0xFF).toByte(),
                                (color shr 8 and 0xFF).toByte(),
                                (color and 0xFF).toByte(),
                                (colorAlpha.coerceAtLeast((fadeOut * 255).toInt())).toByte()
                            )
                        } else if (colorAlpha != 0xFF) {
                            glColor4ub(
                                (color shr 16 and 0xFF).toByte(),
                                (color shr 8 and 0xFF).toByte(),
                                (color and 0xFF).toByte(),
                                colorAlpha.toByte()
                            )
                        }
                        glVertex2d(0.0, 0.0)

                        when (shape) {
                            ArrowsShape.ARROW -> {
                                if (!outline)
                                    offset = (arrowSize / 3.0)
                                glVertex2d(-arrowSize + offset, arrowSize)
                                glVertex2d(0.0, arrowSize - offset)
                                glVertex2d(arrowSize - offset, arrowSize)
                            }

                            ArrowsShape.ISOSCELES -> {
                                if (!outline)
                                    offset = (arrowSize / 3.0)
                                glVertex2d(-arrowSize + offset, arrowSize)
                                glVertex2d(arrowSize - offset, arrowSize)
                            }

                            ArrowsShape.EQUILATERAL -> {
                                glVertex2d(-arrowSize, arrowSize)
                                glVertex2d(arrowSize, arrowSize)
                            }
                        }

                        glEnd()
                        glPopMatrix()
                    }
                }

            glDisable(GL_LINE_SMOOTH)
            glDisable(GL_POLYGON_SMOOTH)
            glEnable(GL_TEXTURE_2D)
            glDisable(GL_BLEND)
            glPopMatrix()
        }
    }

    private enum class ArrowsShape {
        EQUILATERAL,
        ARROW,
        ISOSCELES
    }
}