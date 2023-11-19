package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.module.modules.combat.KillAura
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.entity.interpolatedPosition
import com.curseclient.client.utility.render.animation.EaseUtils
import com.curseclient.client.utility.render.ColorUtils.glColor
import com.curseclient.client.utility.render.graphic.GLUtils
import com.curseclient.client.utility.render.graphic.GLUtils.matrix
import com.curseclient.client.utility.render.graphic.GLUtils.renderGL
import com.curseclient.client.utility.render.graphic.GLUtils.translateGL
import com.curseclient.client.utility.render.RenderUtils3D
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11.*
import kotlin.math.cos
import kotlin.math.sin

object TargetESP : Module(
    "TargetESP",
    "Beautiful killaura effects",
    Category.VISUAL
) {
    private val amount by setting("Amount", 1.0, 3.0, 10.0, 1.0)
    private val dist by setting("Distance", 0.5, 0.1, 1.0, 0.05, { amount > 1.0 })
    private val speed by setting("Speed", 1.0, 0.5, 2.0, 0.1)
    private val radius by setting("Radius", 1.0, 0.5, 2.0, 0.05)
    private val lineWidth by setting("Line Width", 2.0, 1.0, 5.0, 1.0)
    private val throughWalls by setting("Through Walls", false)

    init {
        safeListener<Render3DEvent> {
            if (!KillAura.isEnabled()) return@safeListener
            val target = KillAura.target ?: return@safeListener

            renderGL {
                if (throughWalls) GlStateManager.disableDepth()
                glLineWidth(lineWidth.toFloat())
                for (i in 0..amount.toInt()) {
                    matrix {
                        draw(target, i.toDouble() * dist)
                    }
                }
            }
        }
    }

    private fun SafeClientEvent.draw(entity: EntityLivingBase, ticksAhead: Double) {
        val t = player.ticksExisted.toDouble() + ticksAhead + mc.renderPartialTicks

        val speedTicks = 40.0 / speed
        val ticks = t % speedTicks
        val progress = EaseUtils.getEase((if (ticks > (speedTicks / 2.0)) speedTicks - ticks else ticks) / (speedTicks / 2.0), EaseUtils.EaseType.InOutQuad)

        entity.interpolatedPosition
            .subtract(RenderUtils3D.viewerPos)
            .add(0.0, entity.height * progress, 0.0)
            .translateGL()

        glRotated(t * -5.0, 0.0, 1.0, 0.0)

        GLUtils.draw(GL_LINE_STRIP) {
            for (i in 0..360 step 5) {
                val p = i.toDouble() / 360.0
                val colorProgress = (if (p > 0.5) 1.0 - p else p) * 2.0
                val color = HUD.getColorByProgress(colorProgress)
                color.glColor()

                val dir = Math.toRadians(i - 180.0)
                val boxWidth = (entity.entityBoundingBox.maxX - entity.entityBoundingBox.minX)
                val x = -sin(dir) * boxWidth * radius
                val z = cos(dir) * boxWidth * radius
                glVertex3d(x, 0.0, z)
            }
        }
    }
}