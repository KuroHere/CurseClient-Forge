package com.curseclient.client.module.modules.hud.targetHUD

import baritone.api.utils.Helper
import com.curseclient.client.event.events.EventUpdate
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.module.modules.client.HudEditor
import com.curseclient.client.module.modules.combat.CrystalAura
import com.curseclient.client.module.modules.combat.KillAura
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.math.MathUtils.roundToPlaces
import com.curseclient.client.utility.math.MathUtils.toIntSign
import com.curseclient.client.utility.render.ColorUtils.setAlphaD
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.animation.EaseUtils.ease
import com.curseclient.client.utility.render.animation.NewEaseType
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.graphic.GLUtils
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import javax.vecmath.Vector2d
import kotlin.math.max


object TargetHUD : DraggableHudModule(
    "TargetHUD",
    "Draw target info on the screen",
    HudCategory.HUD,
) {
    private val widthSetting by setting("Width", 130.0, 90.0, 150.0, 1.0)
    private val heightSetting by setting("Height", 35.0, 25.0, 40.0, 1.0)
    private val animationSpeed by setting("Animation Speed", 1.0, 0.1, 10.0, 0.05)

    private var progress = 0.0

    var position = Vector2d(0.0, 0.0)

    private var info = TargetInfo.BLANK
    private var healthProgress = 0.0
    private var target: EntityLivingBase? = null

    init {
        safeListener<EventUpdate> {
            if ((Helper.mc.objectMouseOver != null) and (Helper.mc.objectMouseOver.entityHit != null)) {
                if (Helper.mc.objectMouseOver.entityHit is EntityLivingBase) {
                    target = Helper.mc.objectMouseOver.entityHit as EntityLivingBase
                }
            }
        }
    }

    override fun onRender() {
        update()

        if (progress < 0.01) return
        glTranslated(pos.x + getWidth() * 0.5, pos.y + getHeight() * 0.5, 0.0)

        val p = progress.ease(NewEaseType.OutBack)
        glScaled(p, p, 1.0)

        val pos1 = Vec2d(-getWidth() * 0.5, -getHeight() * 0.5)
        val pos2 = Vec2d(getWidth() * 0.5, getHeight() * 0.5)

        drawTargetHud(pos1, pos2)
    }

    private fun drawTargetHud(pos1: Vec2d, pos2: Vec2d) {
        val highlightColor1 = HUD.getColor().setAlphaD(0.06)
        val highlightColor2 = highlightColor1.setAlphaD(0.0)

        val headBgColor = Color(29, 29, 29)
        val bgColor = Color(21, 21, 21)

        val healthBarBgColor = Color(12, 12, 12)
        val healthBarColor1 = HUD.getColor(0, 0.75)
        val healthBarColor2 = HUD.getColor(2, 0.75)

        val fontColor = HUD.getColor()

        val h = pos2.y - pos1.y

        // Background
        RectBuilder(pos1, pos2).draw {
            color(bgColor)
            radius(5.0)
        }

        // Highlight
        RectBuilder(pos1.plus(1.0), pos2.minus(1.0)).draw {
            colorV(highlightColor1, highlightColor2)
            radius(3.9)
        }

        // Head background
        RectBuilder(pos1, pos1.plus(h)).draw {
            color(headBgColor)
            radius(5.0)
        }

        RectBuilder(pos1.plus(h * 0.5, 0.0), pos1.plus(h)).draw {
            color(headBgColor)
        }

        // Shadow
        RectBuilder(pos1.plus(h, 0.0), pos1.plus(h + 5.0, h)).draw {
            colorH(Color(0, 0, 0, 90), Color(0, 0, 0, 0))
        }

        val headPos1 = pos1.plus(3.0)
        val headPos2 = pos1.plus(h).minus(3.0)

        RenderUtils2D.drawBlurredRect(headPos1, headPos2, 8, Color.BLACK.setAlphaD(0.1))
        info.drawHead(headPos1, headPos2)

        // Healthbar background
        val healthBarCenter = lerp(pos1.y, pos2.y, 0.75)
        val healthBgPos1 = Vec2d(pos1.x + h + 4.0, healthBarCenter - 2.0)
        val healthBgPos2 = Vec2d(pos2.x - 4.0, healthBarCenter + 2.0)
        RenderUtils2D.drawBlurredRect(healthBgPos1, healthBgPos2, 8, Color.BLACK.setAlphaD(0.5))
        RectBuilder(healthBgPos1, healthBgPos2).draw {
            color(healthBarBgColor)
            radius(100.0)
        }

        // Healthbar
        val sliderX = lerp(healthBgPos1.x, healthBgPos2.x, healthProgress)
        val healthSliderPos = Vec2d(sliderX, healthBgPos2.y)
        RectBuilder(healthBgPos1, healthSliderPos).draw {
            colorH(healthBarColor1, healthBarColor2)
            radius(100.0)
        }

        // Health
        val hfr = Fonts.DEFAULT
        val hScale = 0.8

        val healthTextXRange = Vec2d(healthBgPos1.x + 1.0, healthBgPos2.x - 1.0 - hfr.getStringWidth(info.displayHealth, hScale))
        val hTextPos = Vec2d(clamp(sliderX - hfr.getStringWidth(info.displayHealth, hScale) * 0.5, healthTextXRange.x, healthTextXRange.y), healthBgPos1.y - 1.0 - hfr.getHeight(hScale) * 0.5)
        hfr.drawString(info.displayHealth, hTextPos, false, fontColor, hScale)

        // Name
        val fr = Fonts.DEFAULT_BOLD
        val textPos = Vec2d(healthBgPos1.x, hTextPos.y - 3.0 - fr.getHeight() * 0.5)
        fr.drawString(info.name, textPos, false, fontColor)

    }

    private fun update() {
        val ca = if (CrystalAura.isEnabled()) CrystalAura.target else null
        val ka = if (KillAura.isEnabled()) KillAura.target else null

        val target = (ca ?: ka)?.let { TargetInfo(it) }
        val self = if (HudEditor.isEnabled()) TargetInfo.SELF else null

        var shouldForceClose = false

        info = target ?: self ?: run {
            shouldForceClose = true
            info
        }

        info.entity?.let {
            info = TargetInfo(info.name, it.health.toDouble(), info.maxHealth, info.entity)
        }

        if (info.name == "CurseClient  " && !HudEditor.isEnabled()) info = TargetInfo(info.name, 0.0, info.maxHealth, info.entity)

        val dir = (!shouldForceClose && info.health > 0.01).toIntSign().toDouble()
        progress = clamp(progress + dir * GLUtils.deltaTimeDouble() * 3.0 * animationSpeed, 0.0, 1.0)
        if (progress == 0.0) info = TargetInfo.BLANK

        healthProgress = lerp(healthProgress, info.healthProgress, GLUtils.deltaTimeDouble() * 7.0)
        if (healthProgress > 0.999) healthProgress = 1.0 else if (healthProgress < 0.001) healthProgress = 0.0
    }

    private class TargetInfo(val name: String, val health: Double, val maxHealth: Double, val entity: EntityLivingBase?) {
        val healthProgress get() = clamp(health / max(0.1, maxHealth), 0.0, 1.0)
        val displayHealth get() = (health.roundToPlaces(1)).toString()

        constructor(entityIn: EntityLivingBase): this(entityIn.displayName.formattedText, entityIn.health.toDouble(), entityIn.maxHealth.toDouble(), entityIn)

        companion object {
            val BLANK = TargetInfo("", 0.0, 1.0, null)
            val SELF = TargetInfo("CurseClient  ", 20.0, 20.0, null)
        }

        fun drawHead(pos1: Vec2d, pos2: Vec2d) {
            (entity as? AbstractClientPlayer)?.locationSkin?.let { skin ->
                glColor3d(1.0, 1.0, 1.0)
                mc.textureManager.bindTexture(skin)

                val uv1 = Vec2d(8.0, 8.0) // head left top
                val uv2 = Vec2d(16.0, 16.0) // head right bottom

                val textureSize = 64.0

                // normalized uv cords
                val nuv1 = uv1.div(textureSize)
                val nuv2 = uv2.div(textureSize)

                Tessellator.getInstance().apply {
                    buffer.apply {
                        begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX)

                        pos(pos1.x, pos2.y, 0.0)
                        tex(nuv1.x, nuv2.y)
                        endVertex()

                        pos(pos2.x, pos2.y, 0.0)
                        tex(nuv2.x, nuv2.y)
                        endVertex()

                        pos(pos2.x, pos1.y, 0.0)
                        tex(nuv2.x, nuv1.y)
                        endVertex()

                        pos(pos1.x, pos1.y, 0.0)
                        tex(nuv1.x, nuv1.y)
                        endVertex()
                    }

                    draw()
                }
            }
        }
    }


    override fun getWidth() = widthSetting
    override fun getHeight() = heightSetting
}