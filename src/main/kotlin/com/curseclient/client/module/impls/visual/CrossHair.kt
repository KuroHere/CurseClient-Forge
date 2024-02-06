package com.curseclient.client.module.impls.visual

import com.curseclient.client.event.events.EventUpdate
import com.curseclient.client.event.events.render.Render2DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.FPSCounter.fast
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.render.ColorUtils.b
import com.curseclient.client.utility.render.ColorUtils.g
import com.curseclient.client.utility.render.ColorUtils.r
import com.curseclient.client.utility.render.RenderUtils2D
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin


object CrossHair : Module(
    name = "CrossHair",
    category = Category.VISUAL,
    description = "Re-Render the crosshair in your screen"
) {

    private val mode by setting("Mode", Mode.Normal)
    // Circle
    private val colorMode by setting("ColorMode", ColorMode.Sync, { mode == Mode.Circle })
    private val circleColor by setting("Color", Color(0x2250b4b4), { mode == Mode.Circle })
    private val dynamic by setting("Dynamic", true)
    private val range by setting("Range", 30.0, 0.1, 120.0, 1.0, { mode == Mode.Circle })
    private val speed by setting("Speed", 3.0, 0.1, 20.0, 0.1, { mode == Mode.Circle })
    private val backSpeed by setting("BackSpeed", 5.0, 0.1, 20.0, 0.1, { mode == Mode.Circle })

    // Normal
    private val crossHair by setting("Custom-CrossHair", true, { mode == Mode.Normal })
    private val indicator by setting("Attack-Indicator", true, { mode == Mode.Normal })
    private val hitMarkerValue by setting("HitMarker", true, { mode == Mode.Normal })
    private val outline by setting("Outline", true, { mode == Mode.Normal })
    private val dot by setting("Dot", false, { mode == Mode.Normal })
    private val dotColor by setting("Dot-Color", Color(190, 60, 190), { mode == Mode.Normal })
    private val dotRadius by setting("Dot-radius", 1.5, 0.3, 5.0, 0.1, { mode == Mode.Normal })
    private val gapMode by setting("Gap-Mode", GapMode.Normal, { mode == Mode.Normal })
    private val gapSize by setting("Gap-Size", 2.0, 0.5, 20.0, 0.5, { mode == Mode.Normal })

    private val color by setting("CrossHair-Color", Color(190, 60, 190), { mode == Mode.Normal })
    private val outlineColor by setting("Outline-Color", Color(0, 0, 0), { mode == Mode.Normal })
    private val length by setting("Length", 5.5, 0.5, 50.0, 0.5, { mode == Mode.Normal })
    private val width by setting("Width", 0.5, 0.1, 3.0, 0.1, { mode == Mode.Normal })

    private var target: EntityLivingBase? = null

    private var moving_locate = 0f
    private var circleAnimation = 0f

    private var xAnim = 0f
    private var yAnim = 0f

    private var prevPitch = 0f
    private var prevYaw = 0f

    enum class ColorMode {
        Sync,
        Custom
    }

    enum class Mode {
        Normal,
        Circle
    }

    enum class GapMode {
        Normal,
        Dynamic
    }

    init {
        safeListener<EventUpdate> {
            if ((mc.objectMouseOver != null) and (mc.objectMouseOver.entityHit != null)) {
                if (mc.objectMouseOver.entityHit is EntityLivingBase) {
                    target = mc.objectMouseOver.entityHit as EntityLivingBase
                }
            }
        }

        safeListener<Render2DEvent> {
            when(mode) {
                Mode.Normal -> drawNormal()
                Mode.Circle -> drawCircle()
            }
        }
    }

    private fun drawCircle() {
        if (mc.gameSettings.thirdPersonView != 0) return

        val sr = ScaledResolution(mc)
        val midX = sr.scaledWidth / 2f
        val midY = sr.scaledHeight / 2f

        xAnim = if (prevYaw - mc.player.rotationYaw > 0) {
            fast(xAnim, (midX - range).toFloat(), speed.toFloat())
        } else if (prevYaw - mc.player.rotationYaw < 0) {
            fast(xAnim, (midX + range).toFloat(), speed.toFloat())
        } else {
            fast(xAnim, midX, backSpeed.toFloat())
        }

        yAnim = if (prevPitch - mc.player.rotationPitch > 0) {
            fast(yAnim, (midY - range).toFloat(), speed.toFloat())
        } else if (prevPitch - mc.player.rotationPitch < 0) {
            fast(yAnim, (midY + range).toFloat(), speed.toFloat())
        } else {
            fast(yAnim, midY, backSpeed.toFloat())
        }

        prevPitch = mc.player.rotationPitch
        prevYaw = mc.player.rotationYaw

        if(!dynamic) {
            xAnim = midX
            yAnim = midY
        }

        var progress = (360 * (mc.player.swingProgress)).toInt()
        progress = if (progress == 0) 360 else progress

        RenderUtils2D.drawCircle(xAnim, yAnim,0f, 360f, 4F, 2.9f, false, Color.BLACK.rgb)

        val calculateCooldown = mc.player.getCooledAttackStrength(1.0f)
        val endRadius = MathHelper.clamp(calculateCooldown * 360, 0f, 360f)

        this.circleAnimation = MathUtils.lerp(this.circleAnimation, -endRadius, 4f)

        if (colorMode == ColorMode.Custom) {
            RenderUtils2D.drawCircle(xAnim, yAnim, 270F, (progress + 270).toFloat(), 4f, 3f, false, circleColor.rgb)
        } else {
            RenderUtils2D.drawCircle(xAnim, yAnim, 0f, circleAnimation, 4f, 3f, false, HUD.themeColor)
        }
    }

    private fun drawNormal() {
        val sr = ScaledResolution(this.mc)

        val screenMiddleX: Int = sr.scaledWidth / 2

        val screenMiddleY: Int = sr.scaledHeight / 2

        val width: Float = width.toFloat()
        val fill_color = Color(color.r, color.g, color.b)
        val outline_color = Color(outlineColor.r, outlineColor.g, outlineColor.b)
        val target = target
        if (hitMarkerValue && target != null && target.hurtTime > 0) {
            GL11.glPushMatrix()
            GlStateManager.enableBlend()
            GlStateManager.disableTexture2D()
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
            GL11.glColor4f(1f, 1f, 1f, target.hurtTime.toFloat() / target.maxHurtTime.toFloat())
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glLineWidth(1f)
            GL11.glBegin(3)
            GL11.glVertex2f((sr.scaledWidth / 2f + gapSize).toFloat(), (sr.scaledHeight / 2f + gapSize).toFloat())
            GL11.glVertex2f((sr.scaledWidth / 2f + gapSize + length).toFloat(), (sr.scaledHeight / 2f + gapSize + length).toFloat())
            GL11.glEnd()
            GL11.glBegin(3)
            GL11.glVertex2f((sr.scaledWidth / 2f - gapSize).toFloat(), (sr.scaledHeight / 2f - gapSize).toFloat())
            GL11.glVertex2f((sr.scaledWidth / 2f - gapSize - length).toFloat(), (sr.scaledHeight / 2f - gapSize - length).toFloat())
            GL11.glEnd()
            GL11.glBegin(3)
            GL11.glVertex2f((sr.scaledWidth / 2f - gapSize).toFloat(), (sr.scaledHeight / 2f + gapSize).toFloat())
            GL11.glVertex2f((sr.scaledWidth / 2f - gapSize - length).toFloat(), (sr.scaledHeight / 2f + gapSize + length).toFloat())
            GL11.glEnd()
            GL11.glBegin(3)
            GL11.glVertex2f((sr.scaledWidth / 2f + gapSize).toFloat(), (sr.scaledHeight / 2f - gapSize).toFloat())
            GL11.glVertex2f((sr.scaledWidth / 2f + gapSize + length).toFloat(), (sr.scaledHeight / 2f - gapSize - length).toFloat())
            GL11.glEnd()
            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend()
            GL11.glPopMatrix()
        }

        if (crossHair && outline) {
            moving_locate = if (isMoving() && gapMode === GapMode.Dynamic) {
                gapSize.toFloat()
            } else { 0f }
            // Top Box
            RenderUtils2D.drawBorderedRect(
                screenMiddleX - width, (screenMiddleY - (gapSize + length) - moving_locate).toFloat(),
                screenMiddleX + width, (screenMiddleY - gapSize - moving_locate).toFloat(),
                0.5f,
                fill_color,
                outline_color)

            // Bottom Box
            RenderUtils2D.drawBorderedRect(
                screenMiddleX - width, (screenMiddleY + gapSize + moving_locate).toFloat(),
                screenMiddleX + width, (screenMiddleY + (gapSize + length) + moving_locate).toFloat(),
                0.5f,
                fill_color,
                outline_color)

            // Left Box
            RenderUtils2D.drawBorderedRect(
                (screenMiddleX - (gapSize + length) - moving_locate).toFloat(),
                screenMiddleY - width, (screenMiddleX - gapSize - moving_locate).toFloat(),
                screenMiddleY + width,
                0.5f,
                fill_color,
                outline_color)
            // Right Box
            RenderUtils2D.drawBorderedRect(
                (screenMiddleX + gapSize + moving_locate).toFloat(),
                screenMiddleY - width, (screenMiddleX + (gapSize + length) + moving_locate).toFloat(),
                screenMiddleY + width,
                0.5f,
                fill_color,
                outline_color)
        }
        if (indicator) {
            val f = mc.player.getCooledAttackStrength(0.0f)
            val indWidthInc: Float = ((screenMiddleX + (gapSize + length) + moving_locate - (screenMiddleX - (gapSize + length) - moving_locate)) / 17f).toFloat()
            if (f < 1.0f) {
                val finWidth = indWidthInc * (f * 17f)
                RenderUtils2D.drawBorderedRect((screenMiddleX - (gapSize + length) - moving_locate).toFloat(),
                    (screenMiddleY + (gapSize + length) + moving_locate + 2).toFloat(),
                    (screenMiddleX - (gapSize + length) - (moving_locate) + finWidth).toFloat(),
                    (screenMiddleY + (gapSize + length) + moving_locate + 2 + width * 2).toFloat(),
                    0.5f, fill_color, outline_color)
            }
        }
        if (dot) {
            drawCircle(screenMiddleX.toFloat(), screenMiddleY.toFloat(), dotRadius.toFloat(), Color(dotColor.r, dotColor.g, dotColor.b).rgb)
        }
    }

    private fun drawCircle(
        x: Float,
        y: Float,
        radius: Float,
        color: Int
    ) {
        val alpha = (color shr 24 and 255).toFloat() / 255.0f
        val red = (color shr 16 and 255).toFloat() / 255.0f
        val green = (color shr 8 and 255).toFloat() / 255.0f
        val blue = (color and 255).toFloat() / 255.0f
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
        GL11.glColor4f(red, green, blue, alpha)
        GL11.glBegin(GL11.GL_POLYGON)
        for (i in 0..360) {
            GL11.glVertex2d(x + sin(i.toDouble() * 3.141526 / 180.0) * radius.toDouble(), y + cos(i.toDouble() * 3.141526 / 180.0) * radius.toDouble())
        }
        GL11.glEnd()
        GlStateManager.resetColor()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }
    
    private fun isMoving() = mc.player.moveForward.toDouble() != 0.0 || mc.player.moveStrafing.toDouble() != 0.0
}
