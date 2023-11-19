package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.ConnectionEvent
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.listener
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.math.MathUtils.roundToPlaces
import com.curseclient.client.utility.math.MathUtils.toIntSign
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.graphic.GLUtils.matrix
import com.curseclient.client.utility.render.graphic.GLUtils.renderGL
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.RenderUtils3D
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import kotlin.math.abs
import kotlin.math.min

object HealthParticles : Module(
    "HealthParticles",
    "Spawns particles when entity's health is changed",
    Category.VISUAL
) {
    private val size by setting("Size", 1.0, 0.5, 2.0, 0.05)
    private val self by setting("Self", false)

    private val cache = HashMap<Int, Double>() // <EntityID, Health>
    private val particles = ArrayList<HealthParticle>()

    init {
        safeListener<TickEvent.ClientTickEvent> { event ->
            if (event.phase != TickEvent.Phase.START) return@safeListener
            update()

            particles.forEach { it.tick() }
            particles.removeIf { it.shouldRemoved }
        }

        safeListener<Render3DEvent> {
            renderGL {
                GlStateManager.disableDepth()
                particles.forEach { it.draw() }
            }
        }

        listener<ConnectionEvent.Connect> {
            cache.clear()
            particles.clear()
        }
    }

    private fun SafeClientEvent.update() {
        if (player.ticksExisted % 4 != 0) return

        world.loadedEntityList.filterIsInstance<EntityLivingBase>().forEach {
            if (it.entityId == player.entityId && !self) return@forEach
            if (player.getDistance(it) > 32.0) return@forEach

            val id = it.entityId
            val health = it.health.toDouble().roundToPlaces(1)

            if (!cache.containsKey(id) ) {
                cache[id] = health
                return@forEach
            }

            val prev = cache.getValue(id)
            if (prev == health) return@forEach

            val diff = (it.health - prev).roundToPlaces(1)
            val particle = HealthParticle(it.positionVector.add(0.0, it.height / 2.0, 0.0), diff)
            particles.add(particle)

            cache[id] = health
        }
    }

    override fun onEnable() =
        particles.clear()

    private class HealthParticle(posIn: Vec3d, val health: Double) {
        private var prevPos = Vec3d.ZERO
        var pos: Vec3d = Vec3d.ZERO

        private var motionX = 0.0
        private var motionY = 0.0
        private var motionZ = 0.0

        var livingTicks = 0
        var maxLivingTicks = 100
        var shouldRemoved = false

        init {
            prevPos = posIn
            pos = posIn

            val r = Random()
            motionX = (r.nextDouble() - 0.5) * 0.1
            motionY = (r.nextDouble() - 0.5) * 0.25
            motionZ = (r.nextDouble() - 0.5) * 0.1

            maxLivingTicks = (80.0 + r.nextDouble() * 40.0).toInt()
        }

        fun tick() {
            livingTicks++

            prevPos = pos
            pos = pos.add(motionX, motionY, motionZ)

            motionX *= 0.95
            motionY *= 0.95
            motionZ *= 0.95

            shouldRemoved = livingTicks > maxLivingTicks || pos.distanceTo(mc.player.positionVector) > 16.0
        }

        fun draw() {
            val interpolatedPos = MathUtils.lerp(prevPos, pos, mc.renderPartialTicks.toDouble())
            val p = interpolatedPos.subtract(RenderUtils3D.viewerPos)

            matrix {
                GL11.glTranslated(p.x, p.y, p.z)
                GL11.glNormal3f(0.0f, 1.0f, 0.0f)
                GL11.glRotatef(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
                GL11.glRotatef((mc.gameSettings.thirdPersonView != 2).toIntSign().toFloat() * mc.renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
                GL11.glScaled(-0.015 * size, -0.015 * size, 0.015 * size)

                val p1 = min(livingTicks.toDouble(), 10.0) / 10.0
                val p2 = MathUtils.clamp((maxLivingTicks - livingTicks).toDouble() / 10.0, 0.0, 1.0)
                val alpha = ((if (livingTicks <= 10) p1 else p2) * 255.0).toInt()
                val c = (if (health > 0.0) Color.GREEN else Color.RED).setAlpha(alpha)

                val text = abs(health).toString()

                val pos1 = Vec2d(-Fonts.DEFAULT.getStringWidth(text), -Fonts.DEFAULT.getHeight())
                val pos2 = Vec2d(Fonts.DEFAULT.getStringWidth(text), Fonts.DEFAULT.getHeight())

                RenderUtils2D.drawBlurredRect(pos1, pos2, 10, c.setAlpha(alpha / 3))
                Fonts.DEFAULT.drawString(text, Vec2d(-Fonts.DEFAULT.getStringWidth(text), 0.0), false, c, 2.0)
            }
        }
    }
}