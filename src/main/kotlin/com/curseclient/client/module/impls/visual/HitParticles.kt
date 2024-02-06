package com.curseclient.client.module.impls.visual

import baritone.api.utils.Helper
import com.curseclient.client.event.events.AttackEvent
import com.curseclient.client.event.events.EventUpdate
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.Nameable
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.setting.setting
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
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

object HitParticles : Module(
    "HitParticles",
    "Spawns particles by hitting an entity",
    Category.VISUAL
) {
    private val mode by setting("Draw Mode", Mode.CircleOne)
    private val size by setting("Size", 1.0, 0.5, 2.0, 0.05)
    private val amount by setting("Amount", 25.0, 3.0, 50.0, 1.0)
    private val maxAmount by setting("Max Amount", 250.0, 100.0, 500.0, 1.0)
    private val customHeight by setting("Custom Height", false)
    private val spawnHeight by setting("Spawn Height", 0.5, 0.0, 1.0, 0.1)
    private val speedH by setting("Speed H", 1.0, 0.1, 3.0, 0.1)
    private val speedV by setting("Speed V", 0.5, 0.1, 3.0, 0.1)
    private val duration by setting("Duration", 30.0, 5.0, 50.0, 0.2)
    private val inertiaAmount by setting("Inertia Amount", 0.8, 0.0, 1.0, 0.05)
    private val gravityAmount by setting("Gravity Amount", 0.0, 0.0, 1.0, 0.05)
    private val seeThroughWalls by setting("ThroughWalls", false)

    private val particles = ArrayList<HitParticle>()
    private var target: EntityLivingBase? = null

    private enum class Mode(override val displayName: String) : Nameable {
        CircleOne("Circle 1"),
        CircleTwo("Circle 2"),
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

    private class HitParticle(
        posIn: Vec3d,
        val index: Int
    ) {
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

            if (blockPos(pos.x, pos.y, pos.z)) {
                motionY = -motionY / 1.0
            } else if (collisionCheck(pos.x, pos.y, pos.z)) {
                motionX = -motionX + motionZ
                motionZ = -motionZ + motionX
            }
        }

        fun collisionCheck(
            x : Double,
            y : Double,
            z : Double,
            size : Double = 0.0,
            sp : Double = sqrt(motionX * motionX + motionZ * motionZ) * 1
        ) =  blockPos(x, y, z) ||
            blockPos(x, y - size, z) ||
            blockPos(x, y + size, z) ||

            blockPos(x - sp, y, z - sp) ||
            blockPos(x + sp, y, z + sp) ||
            blockPos(x + sp, y, z - sp) ||
            blockPos(x - sp, y, z + sp) ||
            blockPos(x + sp, y, z) ||
            blockPos(x - sp, y, z) ||
            blockPos(x, y, z + sp) ||
            blockPos(x, y, z - sp) ||

            blockPos(x - sp, y - size, z - sp) ||
            blockPos(x + sp, y - size, z + sp) ||
            blockPos(x + sp, y - size, z - sp) ||
            blockPos(x - sp, y - size, z + sp) ||
            blockPos(x + sp, y - size, z) ||
            blockPos(x - sp, y - size, z) ||
            blockPos(x, y - size, z + sp) ||
            blockPos(x, y - size, z - sp) ||

            blockPos(x - sp, y + size, z - sp) ||
            blockPos(x + sp, y + size, z + sp) ||
            blockPos(x + sp, y + size, z - sp) ||
            blockPos(x - sp, y + size, z + sp) ||
            blockPos(x + sp, y + size, z) ||
            blockPos(x - sp, y + size, z) ||
            blockPos(x, y + size, z + sp) ||
            blockPos(x, y + size, z - sp)

        fun blockPos(
            x: Double,
            y: Double,
            z: Double
        ): Boolean {
            val excludedBlocks: Set<Block> = HashSet(listOf(
                Blocks.AIR, Blocks.WATER, Blocks.LAVA, Blocks.BED, Blocks.CAKE, Blocks.TALLGRASS,
                Blocks.FLOWER_POT, Blocks.RED_FLOWER, Blocks.YELLOW_FLOWER, Blocks.SAPLING, Blocks.VINE,
                Blocks.ACACIA_FENCE, Blocks.ACACIA_FENCE_GATE, Blocks.BIRCH_FENCE, Blocks.BIRCH_FENCE_GATE,
                Blocks.DARK_OAK_FENCE, Blocks.DARK_OAK_FENCE_GATE, Blocks.JUNGLE_FENCE, Blocks.JUNGLE_FENCE_GATE,
                Blocks.NETHER_BRICK_FENCE, Blocks.OAK_FENCE, Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE,
                Blocks.SPRUCE_FENCE_GATE, Blocks.ENCHANTING_TABLE, Blocks.END_PORTAL_FRAME, Blocks.DOUBLE_PLANT,
                Blocks.STANDING_SIGN, Blocks.WALL_SIGN, Blocks.SKULL, Blocks.DAYLIGHT_DETECTOR,
                Blocks.DAYLIGHT_DETECTOR_INVERTED, Blocks.STONE_SLAB, Blocks.WOODEN_SLAB, Blocks.CARPET,
                Blocks.DEADBUSH, Blocks.VINE, Blocks.REDSTONE_WIRE, Blocks.REEDS, Blocks.SNOW_LAYER
            ))
            val block: Block = mc.world.getBlockState(BlockPos(x, y, z)).block
            return !excludedBlocks.contains(block)
        }

        fun draw() {
            val interpolatedPos = lerp(prevPos, pos, mc.renderPartialTicks.toDouble())
            val p = interpolatedPos.subtract(RenderUtils3D.viewerPos)

            matrix {
                if (seeThroughWalls) {
                    glPushMatrix()
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
                }
                if (seeThroughWalls) {
                    glPopMatrix()
                    GlStateManager.enableDepth()
                }
            }
        }
    }

    private fun circle(
        radius: Float,
        color: Color,
        sections: Int
    ) {
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