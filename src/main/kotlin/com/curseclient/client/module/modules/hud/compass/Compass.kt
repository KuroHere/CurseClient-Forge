package com.curseclient.client.module.modules.hud.compass

import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.module.modules.hud.Radar
import com.curseclient.client.module.modules.visual.CrossHair
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.ColorUtils.setDarkness
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.RenderUtils2D.initStencilToWrite
import com.curseclient.client.utility.render.RenderUtils2D.readStencilBuffer
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.shader.GaussianBlur
import com.curseclient.client.utility.render.shader.GradientUtil
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.shader.RoundedUtil
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object Compass: DraggableHudModule(
    "Compass",
    "Draw direction radar on your HUD",
    HudCategory.HUD
) {
    private val w by setting("Width", 210.0, 20.0, 220.0, 1.0)
    private val moving by setting("Moving", false)
    private val bgColor by setting("BackGround", Color.BLACK.setAlpha(150))
    private val bgBlur by setting("Blur", false)
    val radius by setting("BlurRadius", 20, 5, 50, 1, { bgBlur })
    private val compression by setting("Compression", 2, 1, 5, 1, { bgBlur })

    private var offsetX = 0.0
    private var offsetY = 0.0


    override fun onRender() {
        super.onRender()
        val pos1 = Vec2d(pos.x, pos.y)
        val pos2 = pos1.plus(getWidth(), getHeight())

        if (moving) {
            val swingAmount = if (mc.player.yOffset > 0.4) {
                2.0
            } else
                1.6

            if (isMoving()) {
                val time = System.currentTimeMillis() / 100.0
                offsetX = cos(time) * swingAmount
                offsetY = cos(time) * swingAmount
            } else {
                if (offsetX != 0.0 || offsetY != 0.0) {
                    val moveSpeed = 0.2

                    offsetX -= offsetX * moveSpeed
                    offsetY -= offsetY * moveSpeed

                    if (offsetX < 0.01 && offsetY < 0.01) {
                        offsetX = 0.0
                        offsetY = 0.0
                    }
                }
            }

            GL11.glPushMatrix()
            GL11.glTranslatef(offsetX.toFloat(), offsetY.toFloat(), 0f)

            drawCompass(pos1, pos2)

            GL11.glPopMatrix()
        } else
            drawCompass(pos1, pos2)
    }

    private fun drawCompass(pos1: Vec2d, pos2: Vec2d) {
        val playerYaw = mc.player.rotationYaw
        val rotationYaw = MathUtils.wrap(playerYaw)

        val c1 = HUD.getColor(0)
        val c2 = HUD.getColor(10)

        val frb = Fonts.DEFAULT_BOLD
        val fr = Fonts.DEFAULT

        val compassDirections = listOf("N", "E", "S", "W", "NE", "SE", "SW", "NW", "N", "NE", "E", "NW", "W")
        val compassDirectionsNumber = listOf(15, 30, 60, 75, 105, 120, 150, 165, 195, 210, 240, 255, 285, 300, 330, 345, 285, 255, 240, 210, 195, 165, 150, 120, 105, 75)

        val offsets = listOf(180, -90, 0, 90, -135, -45, 45, 135, -180, 225, 270, -225, -270)

        val numberOffsets = listOf(15, 30, 60, 75, 105, 120, 150, 165, 195, 210, 240, 255, 285, -60, -30, -15, -75, -105, -120, -150, -165, -195, -210, -240, -255, -285)

        val minSize = minOf(compassDirectionsNumber.size, numberOffsets.size)

        GlStateManager.pushMatrix()

        if (bgBlur) {
            GaussianBlur.startBlur()
            RoundedUtil.drawGradientRound(pos1.x.toFloat(), pos1.y.toFloat(), getWidth().toFloat(), getHeight().toFloat(), 1.4f, bgColor, bgColor, bgColor, bgColor)
            GaussianBlur.endBlur(radius, compression)
        }

        RectBuilder(pos1, pos2).apply {
            color(bgColor)
            radius(1.3)
            draw()
        }

        GlStateManager.popMatrix()

        // Scissor
        initStencilToWrite()
        RectBuilder(pos1, pos2).apply {
            color(Color.BLACK)
            radius(1.3)
            draw()
        }
        readStencilBuffer(1)
        GlStateManager.pushMatrix()

        for (index in 0 until minSize) {
            val directionNumber = compassDirectionsNumber[index]
            val offsetX = pos.x - rotationYaw + getWidth() / 2 + numberOffsets[index] - frb.getStringWidth(directionNumber.toString()) / 2.0f

            RenderUtils2D.drawLine(
                (pos.x - rotationYaw + getWidth() / 2 + numberOffsets[index]).toFloat(),
                pos.y.toFloat() + 1,
                (pos.x - rotationYaw + getWidth() / 2 + numberOffsets[index]).toFloat(),
                (pos.y + 3).toFloat(),
                1f,
                Color.WHITE.rgb
            )

            fr.drawString(
                directionNumber.toString(),
                Vec2d(offsetX, pos.y + 7.0),
                color = Color.WHITE.setAlpha(200),
                scale = 0.9
            )
        }

        for ((index, direction) in compassDirections.withIndex()) {
            val scale = if (direction in listOf("NE", "SE", "SW", "NW")) 1.165 else 1.4

            val offsetX = pos.x - rotationYaw + getWidth() / 2 + offsets[index] - frb.getStringWidth(direction, scale) / 2.0f
            val offsetY = if (direction in listOf("NE", "SE", "SW", "NW")) 7.3 else 8.2

            GradientUtil.applyGradientCornerRL(pos.x.toFloat(), pos.y.toFloat(), offsetX.toFloat(), offsetY.toFloat(), 1f, c1, c2) {
                frb.drawString(
                    direction,
                    Vec2d(offsetX, pos.y + offsetY),
                    false,
                    color = Color.WHITE,
                    scale = scale
                )
            }
        }

        GlStateManager.popMatrix()
        RenderUtils2D.uninitStencilBuffer()

        RenderUtils2D.drawTriangle((pos.x + getWidth() / 2).toFloat(), pos.y.toFloat() + getHeight().toFloat() - 3, 4f, c1.brighter().setAlpha(130).rgb, true, 5, 200)
        RectBuilder(pos1.plus(getWidth() / 2.0 - 7, getHeight() + 2), pos1.plus(getWidth() / 2.0 + 7, getHeight() + 10)).apply {
            shadow(pos.x + getWidth() / 2.0 - 7, pos.y + getHeight() + 2, 14.0, 8.0, 5, Color.BLACK.setAlpha(150))
            color(Color.BLACK.setAlpha(100))
            radius(1.0)
            draw()
        }
        val rotational = rotationYaw.unwrap().roundToInt()
        val yaw = when (rotational) {
            0 -> "S"
            45 -> "SW"
            90 -> "W"
            145 -> "NW"
            180 -> "N"
            225 -> "NE"
            270 -> "E"
            315 -> "SE"
            360 -> "S"
            else -> rotational.toString()
        }
        val textColor = when (yaw) {
            "S", "SW", "W", "NW", "N", "NE", "E", "SE" -> c2.brighter()
            else -> Color.WHITE
        }
        fr.drawString(yaw, Vec2d((pos.x + getWidth() / 2 - fr.getStringWidth(yaw, 0.42)), pos.y + fr.getHeight(0.8) + 6.5 + getHeight() / 2), true, textColor, 0.8)
    }

    private fun isMoving(): Boolean {
        return mc.player.moveForward.toDouble() != 0.0 || mc.player.moveStrafing.toDouble() != 0.0
    }

    private fun Float.unwrap(): Float {
        var unwrappedAngle = this % 360
        if (unwrappedAngle < 0) {
            unwrappedAngle += 360
        }
        return unwrappedAngle
    }

    override fun getWidth() = w
    override fun getHeight() = 15.0
}