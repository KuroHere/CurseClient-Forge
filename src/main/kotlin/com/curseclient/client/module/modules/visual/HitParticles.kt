package com.curseclient.client.module.modules.visual

import baritone.api.utils.Helper
import com.curseclient.client.event.events.AttackEvent
import com.curseclient.client.event.events.EventUpdate
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.Nameable
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.misc.SoundUtils
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.math.MathUtils.toIntSign
import com.curseclient.client.utility.render.ColorUtils.glColor
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.RenderUtils3D
import com.curseclient.client.utility.render.graphic.GLUtils.draw
import com.curseclient.client.utility.render.graphic.GLUtils.matrix
import com.curseclient.client.utility.render.graphic.GLUtils.renderGL
import net.minecraft.block.Block
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Blocks
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


object HitParticles : Module(
    "HitParticles",
    "Spawns particles by hitting an entity",
    Category.VISUAL
) {
    private val mode by setting("Draw Mode", Mode.CircleOne)
    private val size by setting("Size", 1.0, 0.5, 2.0, 0.05, visible = {mode != Mode.Blood})
    private val amount by setting("Amount", 25.0, 3.0, 50.0, 1.0, visible = {mode != Mode.Blood})
    private val maxAmount by setting("Max Amount", 250.0, 100.0, 500.0, 1.0, visible = {mode != Mode.Blood})
    private val customHeight by setting("Custom Height", false, visible = {mode != Mode.Blood})
    private val spawnHeight by setting("Spawn Height", 0.5, 0.0, 1.0, 0.1, visible = {mode != Mode.Blood})
    private val speedH by setting("Speed H", 1.0, 0.1, 3.0, 0.1, visible = {mode != Mode.Blood})
    private val speedV by setting("Speed V", 0.5, 0.1, 3.0, 0.1, visible = {mode != Mode.Blood})
    private val duration by setting("Duration", 30.0, 5.0, 50.0, 0.2, visible = {mode != Mode.Blood})
    private val inertiaAmount by setting("Inertia Amount", 0.8, 0.0, 1.0, 0.05, visible = {mode != Mode.Blood})
    private val gravityAmount by setting("Gravity Amount", 0.0, 0.0, 1.0, 0.05, visible = {mode != Mode.Blood})
    private val seeThroughWalls by setting("ThroughWalls", false, visible = {mode != Mode.Blood})

    private val blood by setting("Blood", true, visible = {mode == Mode.Blood})
    private val sound by setting("Sound", true, visible = {mode == Mode.Blood})
    private val multiplier by setting("Multiplier", 2, 1, 10, 1, visible = {mode == Mode.Blood})

    private val particles = ArrayList<HitParticle>()
    private var target: EntityLivingBase? = null

    private enum class Mode(override val displayName: String) : Nameable {
        CircleOne("Circle 1"),
        CircleTwo("Circle 2"),
        Blood("Blood"),
        Bubble("Bubble")
    }

    init {
        safeListener<EventUpdate> {
            if ((Helper.mc.objectMouseOver != null) and (Helper.mc.objectMouseOver.entityHit != null)) {
                if (Helper.mc.objectMouseOver.entityHit is EntityLivingBase) {
                    target = Helper.mc.objectMouseOver.entityHit as EntityLivingBase
                }
            }
        }
        safeListener<AttackEvent.Pre> {
            for (i in 0..amount.toInt()) {
                val height = if (customHeight) spawnHeight else i.toDouble() / amount

                val pos = it.entity.positionVector.add(0.0, it.entity.height * height, 0.0)
                val particle = HitParticle(pos, particles.count())
                particles.add(particle)
            }

            target?.let {
                if (it !is EntityEnderCrystal && mode == Mode.Blood && blood) {
                    for (i in 0 until multiplier.toInt()) {
                        it.world.spawnParticle(
                            EnumParticleTypes.BLOCK_CRACK,
                            it.posX,
                            it.posY + it.height - 0.75,
                            it.posZ, 0.0, 0.0, 0.0,
                            Block.getStateId(Blocks.REDSTONE_BLOCK.defaultState)
                        )
                    }
                    if (sound) {
                        var bloodSound = ""
                        when (MathUtils.random(1f, 3f).toInt()) {
                            1 -> bloodSound = "blood1.wav"
                            2 -> bloodSound = "blood2.wav"
                            3 -> bloodSound = "blood3.wav"
                        }
                        SoundUtils.playSound { bloodSound }
                    }
                }
            }
        }

        safeListener<TickEvent.ClientTickEvent> { event ->
            if (event.phase != TickEvent.Phase.START) return@safeListener
            particles.forEach { it.tick() }
            particles.removeIf { it.livingTicks > it.maxLivingTicks }
            while (particles.count() > maxAmount) { particles.removeAt(0) }
        }

        safeListener<Render3DEvent> {
            renderGL {
                particles.forEach { it.draw() }
            }
        }
    }

    override fun onEnable() =
        particles.clear()

    private class HitParticle(posIn: Vec3d, val index: Int) {
        private var prevPos = Vec3d.ZERO
        var pos: Vec3d = Vec3d.ZERO

        private var motionX = 0.0
        private var motionY = 0.0
        private var motionZ = 0.0

        var livingTicks = 0
        var maxLivingTicks = 100

        init {
            prevPos = posIn
            pos = posIn

            val r = Random()
            // -0.1..0.1 * speed
            motionX = (r.nextDouble() - 0.5) * 0.2 * speedH
            motionY = (r.nextDouble() - 0.5) * 0.2 * speedV
            motionZ = (r.nextDouble() - 0.5) * 0.2 * speedH

            maxLivingTicks = (duration * 10.0 + r.nextDouble() * 40.0).toInt()
        }

        fun tick() {
            livingTicks++

            prevPos = pos
            pos = pos.add(motionX, motionY, motionZ)

            motionY -= gravityAmount * 0.01

            motionX *= 0.9 + (inertiaAmount / 10.0)
            motionY *= 0.9 + (inertiaAmount / 10.0)
            motionZ *= 0.9 + (inertiaAmount / 10.0)
        }

        fun draw() {
            val interpolatedPos = lerp(prevPos, pos, mc.renderPartialTicks.toDouble())
            val p = interpolatedPos.subtract(RenderUtils3D.viewerPos)

            matrix {
                if (seeThroughWalls) {
                    GlStateManager.disableDepth()
                }
                glTranslated(p.x, p.y, p.z)
                glNormal3f(0.0f, 1.0f, 0.0f)
                glRotatef(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
                glRotatef((mc.gameSettings.thirdPersonView != 2).toIntSign().toFloat() * mc.renderManager.playerViewX, 1.0f, 0.0f, 0.0f)

                glScaled(-0.005 * size, -0.005 * size, 0.005 * size)

                val p1 = min(livingTicks.toDouble(), 10.0) / 10.0
                val p2 = clamp((maxLivingTicks - livingTicks).toDouble() / 10.0, 0.0, 1.0)
                val alpha = ((if (livingTicks <= 10) p1 else p2) * 255.0).toInt()

                val c1 = HUD.getColor(index)
                val c2 = HUD.getColor(index, 0.6)
                val c3 = HUD.getColor(index, 0.3)

                val dist = RenderUtils3D.viewerPos.distanceTo(pos)
                val s = clamp(16 / dist, 5.0, 16.0).toInt()

                when (mode) {
                    Mode.CircleOne -> {
                        if (dist < 6) circle(15f, c3.setAlpha((alpha * 0.2).toInt()), s)
                        if (dist < 8) circle(10f, c2.setAlpha((alpha * 0.7).toInt()), s)
                        circle(7f, c1.setAlpha(alpha), s)
                    }
                    Mode.CircleTwo -> {
                        circle(15f, c3.setAlpha(alpha), s)
                        circle(7f, c1.setAlpha(alpha), s)
                    }
                    Mode.Bubble -> {
                        circle(12f, c1.setAlpha(alpha), s)
                        circle(10f, c2.setAlpha(alpha), s)
                    }
                    Mode.Blood -> {}
                }
                if (seeThroughWalls) {
                    GlStateManager.enableDepth()
                }
            }
        }
    }

    private fun circle(radius: Float, color: Color, sections: Int) {
        val dAngle = 2 * Math.PI / sections
        color.glColor()

        draw(GL_TRIANGLE_FAN) {
            for (i in 0 until sections) {
                glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                glVertex2d(radius * sin(i * dAngle), radius * cos(i * dAngle))
            }
        }

        GlStateManager.resetColor()
    }
}