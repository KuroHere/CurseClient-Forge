package com.curseclient.client.module.impls.visual

import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.module.impls.combat.CrystalAura
import com.curseclient.client.module.impls.combat.KillAura
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.math.MathUtils.roundToPlaces
import com.curseclient.client.utility.math.MathUtils.toIntSign
import com.curseclient.client.utility.player.TargetingUtils
import com.curseclient.client.utility.render.ColorUtils.setAlphaD
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.animation.ease.EaseUtils
import com.curseclient.client.utility.render.animation.ease.EaseUtils.ease
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.graphic.GLUtils
import com.curseclient.client.utility.render.graphic.GLUtils.matrix
import com.curseclient.client.utility.render.graphic.GLUtils.renderGL
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.mixin.accessor.AccessorRenderManager
import com.curseclient.mixin.accessor.render.AccessorEntityRenderer
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.AxisAlignedBB
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11.*
import org.lwjgl.util.glu.GLU
import java.awt.Color
import javax.vecmath.Vector3d
import javax.vecmath.Vector4d
import kotlin.math.max
import kotlin.math.pow

object FollowTargetHud : Module(
    "FollowTargetHUD",
    "Draw follow target info.",
    Category.VISUAL
) {

    private val widthSetting by setting("Width", 130.0, 90.0, 150.0, 1.0)
    private val heightSetting by setting("Height", 35.0, 25.0, 40.0, 1.0)
    private val animationSpeed by setting("Animation Speed", 1.0, 0.1, 10.0, 0.05)
    private val ease by setting("ease", EaseUtils.EaseType.OutBack)

    private val range by setting("Range", 20.0, 0.0, 250.0, 1.0)
    private val scale by setting("ScaleFactor", 5.0, 1.0, 5.0, 0.1)
    private val xOffset by setting("XOffset", 1.0, -10.0, 10.0, 0.1)
    private val yOffset by setting("YOffset", 0.5, -5.0, 5.0, 0.1)

    private var progress = 0.0
    private var healthProgress = 0.0

    private var info = TargetInfo.BLANK

    init {
        safeListener<Render3DEvent> { it ->
            val target =
                TargetingUtils.getTarget(range, false)
                    ?: KillAura.target
                    ?: CrystalAura.target
                    ?: return@safeListener

            if (isValid(target) && RenderUtils2D.isInViewFrustrum(target)) {
                renderGL {
                    val p = progress.ease(ease)
                    val scaleFactor: Double = scale

                    val xOffset: Double = xOffset / xOffset.pow(2.0)
                    val yOffset: Double = yOffset / yOffset.pow(2.0)
                    val scaling: Double = scale / scale.pow(2.0)

                    val x = target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.renderPartialTicks
                    val y = target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.renderPartialTicks
                    val z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.renderPartialTicks
                    val axisAlignedBB2 = target.entityBoundingBox
                    val axisAlignedBB = AxisAlignedBB(
                        axisAlignedBB2.minX - target.posX + x - 0.05,
                        axisAlignedBB2.minY - target.posY + y,
                        axisAlignedBB2.minZ - target.posZ + z - 0.05,
                        axisAlignedBB2.maxX - target.posX + x + 0.05,
                        axisAlignedBB2.maxY - target.posY + y + 0.15,
                        axisAlignedBB2.maxZ - target.posZ + z + 0.05
                    )
                    val vectors = arrayOf(
                        Vector3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ),
                        Vector3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ),
                        Vector3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ),
                        Vector3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ),
                        Vector3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ),
                        Vector3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ),
                        Vector3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ),
                        Vector3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ)
                    )
                    (mc.entityRenderer as AccessorEntityRenderer).invokeSetupCameraTransform(it.partialTicks, 0)

                    var position: Vector4d? = null
                    for (vector in vectors) {
                        val projectedVector = project2D(
                            scaleFactor.toFloat(),
                            (vector.x - (mc.renderManager as AccessorRenderManager).renderPosX),
                            (vector.y - (mc.renderManager as AccessorRenderManager).renderPosY),
                            (vector.z - (mc.renderManager as AccessorRenderManager).renderPosZ)
                        )
                        if (projectedVector != null && projectedVector.z > 0 && projectedVector.z < 1) {
                            if (position == null) {
                                position = Vector4d(projectedVector.x, projectedVector.y, projectedVector.z, 0.0)
                            }
                            position.x = projectedVector.x.coerceAtMost(position.x)
                            position.y = projectedVector.y.coerceAtMost(position.y)
                            position.z = projectedVector.x.coerceAtLeast(position.z)
                            position.w = projectedVector.y.coerceAtLeast(position.w)
                        }
                    }

                    position?.let {
                        mc.entityRenderer.setupOverlayRendering()
                        val posX = it.x
                        val posY = it.y
                        //val endPosX = it.z
                        //val endPosY = it.w

                        matrix {
                            glTranslated(posX + xOffset * 0.5, posY + yOffset * 0.5, 0.0)

                            glScaled(scaling * p, scaling * p, scaling)
                            drawTargetHUD()
                        }

                    }
                }
            }
            glEnable(GL_DEPTH_TEST)
            mc.entityRenderer.setupOverlayRendering()
        }
    }

    private fun drawTargetHUD() {
        update()
        val width = getWidth()
        val height = getHeight()

        val pos1 = Vec2d(-width / 2.0, -height / 2.0)
        val pos2 = Vec2d(width / 2.0, height / 2.0)

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

        var shouldForceClose = false

        info = target ?: run {
            shouldForceClose = true
            info
        }

        info.entity?.let {
            info = TargetInfo(info.name, it.health.toDouble(), info.maxHealth, info.entity)
        }

        val dir = (!shouldForceClose && info.health > 0.01).toIntSign().toDouble()
        progress = clamp(progress + dir * GLUtils.deltaTimeDouble() * 3.0 * animationSpeed, 0.0, 1.0)
        if (progress == 0.0) info = TargetInfo.BLANK

        healthProgress = lerp(healthProgress, info.healthProgress, GLUtils.deltaTimeDouble() * 7.0)
        if (healthProgress > 0.999) healthProgress = 1.0 else if (healthProgress < 0.001) healthProgress = 0.0
    }

    fun project2D(
        scaleFactor: Float,
        x: Double,
        y: Double,
        z: Double
    ): Vector3d? {
        val xPos = x.toFloat()
        val yPos = y.toFloat()
        val zPos = z.toFloat()
        val viewport = GLAllocation.createDirectIntBuffer(16)
        val modelview = GLAllocation.createDirectFloatBuffer(16)
        val projection = GLAllocation.createDirectFloatBuffer(16)
        val vector = GLAllocation.createDirectFloatBuffer(4)
        glGetFloat(GL_MODELVIEW_MATRIX, modelview)
        glGetFloat(GL_PROJECTION_MATRIX, projection)
        glGetInteger(GL_VIEWPORT, viewport)
        if (GLU.gluProject(xPos, yPos, zPos, modelview, projection, viewport, vector)) return Vector3d((vector[0] / scaleFactor).toDouble(), (((Display.getHeight() - vector[1]) / scaleFactor).toDouble()), vector[2].toDouble())
        return null
    }

    private fun isValid(entity: Entity) = entity is EntityPlayer


    private class TargetInfo(
        val name: String,
        val health: Double,
        val maxHealth: Double,
        val entity: EntityLivingBase?
    ) {
        val healthProgress get() = clamp(health / max(0.1, maxHealth), 0.0, 1.0)
        val displayHealth get() = (health.roundToPlaces(1)).toString()

        constructor(entityIn: EntityLivingBase): this(entityIn.displayName.formattedText, entityIn.health.toDouble(), entityIn.maxHealth.toDouble(), entityIn)

        companion object {
            val BLANK = TargetInfo("", 0.0, 1.0, null)
        }

        fun drawHead(
            pos1: Vec2d,
            pos2: Vec2d
        ) {
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

    fun getWidth() = widthSetting
    fun getHeight() = heightSetting
}