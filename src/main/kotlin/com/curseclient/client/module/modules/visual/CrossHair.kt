package com.curseclient.client.module.modules.visual

import baritone.api.utils.Helper
import com.curseclient.client.event.events.render.Render2DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.events.EventUpdate
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.ColorUtils.b
import com.curseclient.client.utility.render.ColorUtils.g
import com.curseclient.client.utility.render.ColorUtils.r
import com.curseclient.client.utility.render.RenderUtils2D
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color


object CrossHair : Module(
    name = "CrossHair",
    category = Category.VISUAL,
    description = "Re-Render the crosshair in your screen"
) {

    val crossHair by setting("Custom-CrossHair", true)
    val indicator by setting("Attack-Indicator", true)
    val hitMarkerValue by setting("HitMarker", true)
    val outline by setting("Outline", true)
    val dot by setting("Dot", false)
    val dotColor by setting("Dot-Color", Color(190, 60, 190))
    val dotRadius by setting("Dot-radius", 1.5, 0.3, 5.0, 0.1)
    val gapMode by setting("Gap-Mode", GapMode.Normal)
    val gapSize by setting("Gap-Size", 2.0, 0.5, 20.0, 0.5)

    val color by setting("CrossHair-Color", Color(190, 60, 190))
    val outlineColor by setting("Outline-Color", Color(0, 0, 0))
    val length by setting("Length", 5.5, 0.5, 50.0, 0.5)
    val width by setting("Width", 0.5, 0.1, 3.0, 0.1)

    private var target: EntityLivingBase? = null

    var moving_locate = 0f
    
    enum class GapMode {
        Normal,
        Dynamic
    }

    init {
        safeListener<EventUpdate> {
            if ((Helper.mc.objectMouseOver != null) and (Helper.mc.objectMouseOver.entityHit != null)) {
                if (Helper.mc.objectMouseOver.entityHit is EntityLivingBase) {
                    target = Helper.mc.objectMouseOver.entityHit as EntityLivingBase
                }
            }
        }
        safeListener<Render2DEvent> {
            val sr = ScaledResolution(this.mc)
            
            val screenMiddleX: Int = sr.scaledWidth / 2

            val screenMiddleY: Int = sr.scaledHeight / 2

            val width: Float = width.toFloat()
            val fill_color = Color(color.r, color.g, color.b)
            val outline_color = Color(outlineColor.r, outlineColor.g, outlineColor.b)
            val target = target/* ?: RaycastUtils.raycastEntity(Reach.hitReach.toDouble()) {
            it is EntityLivingBase
        } as EntityLivingBase? */
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
                RenderUtils2D.drawCircle(screenMiddleX.toFloat(), screenMiddleY.toFloat(), dotRadius.toFloat(), Color(dotColor.r, dotColor.g, dotColor.b).rgb)
            }

        }
    }
    
    fun isMoving(): Boolean {
        return Helper.mc.player.moveForward.toDouble() != 0.0 || Helper.mc.player.moveStrafing.toDouble() != 0.0
    }

}
