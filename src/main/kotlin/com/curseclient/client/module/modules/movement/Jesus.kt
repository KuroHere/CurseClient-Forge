package com.curseclient.client.module.modules.movement

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.*
import com.curseclient.client.event.listener.Nameable
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.entity.calcIsInWater
import com.curseclient.client.utility.extension.settingName
import com.curseclient.client.utility.math.MathUtils.ceilToInt
import com.curseclient.client.utility.math.MathUtils.floorToInt
import com.curseclient.client.utility.math.MathUtils.toInt
import com.curseclient.client.utility.player.MovementUtils.calcMoveRad
import com.curseclient.client.utility.player.MovementUtils.isInputting
import net.minecraft.block.BlockLiquid
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.cos
import kotlin.math.sin

object Jesus : Module(
    "Jesus",
    "Allows you to swim faster",
    Category.MOVEMENT
) {
    private val mode by setting("Mode", Mode.NCP)

    // NCP
    private val applyMotion by setting("Apply Motion", true, { mode == Mode.NCP })

    // Matrix YPort
    private val matrixYPortSpeed by setting("Matrix YPort Speed", 1.1, 1.0, 2.0, 0.01, { mode == Mode.MatrixYPort })

    // Matrix New
    private val matrixNewSpeed by setting("Matrix New Speed", 1.7, 0.0, 2.0, 0.01, { mode == Mode.MatrixNew })

    private val box = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.99, 1.0)
    private var ticksInWater = 0

    override fun getHudInfo() = mode.settingName

    private enum class Mode(override val displayName: String): Nameable {
        NCP("NCP Solid"),
        NCPNew("NCP New"),
        NCPDolphin1("NCP Dolphin 1"),
        NCPDolphin2("NCP Dolphin 2"),
        MatrixYPort("Matrix YPort"),
        MatrixNew("Matrix New")
    }

    init {
        safeListener<TickEvent.ClientTickEvent>(-100) {
            if (mode == Mode.NCPNew && isLiquidAt(-0.02)) player.onGround = true
        }

        safeListener<StepEvent.Pre> {
            if (shouldSpoof() && listOf(Mode.MatrixYPort, Mode.MatrixNew).contains(mode))
                it.cancel()
        }

        safeListener<JumpEvent>(-100) {
            if (!shouldSpoof()) return@safeListener

            when (mode) {
                Mode.NCP, Mode.MatrixNew, Mode.MatrixYPort -> {
                    it.cancel()
                }
                else -> {}
            }
        }

        safeListener<PlayerPacketEvent.Data> {
            if (listOf(Mode.MatrixYPort, Mode.MatrixNew).contains(mode) &&
                world.getBlockState(BlockPos(Vec3d(it.x, it.y + 0.001, it.z))).block is BlockLiquid
            ) it.onGround = false

            if (!listOf(Mode.NCP, Mode.MatrixYPort, Mode.MatrixNew).contains(mode)) return@safeListener
            if (!shouldSpoof()) return@safeListener

            var height = 0.99
            when (mode) {
                Mode.NCP -> {
                    if (player.ticksExisted % 2 == 0) height += 0.011
                }
                Mode.MatrixYPort, Mode.MatrixNew -> {
                    if (player.ticksExisted % 2 != 0) height = 0.92
                    it.onGround = false
                }
                else -> {}
            }

            it.y += -0.99 + height
        }

        safeListener<MoveEvent> {
            ticksInWater = (ticksInWater + 1) * isLiquid(0.001).toInt()

            when (mode) {
                Mode.NCP -> {
                    if (!shouldSpoof()) {
                        if (!player.calcIsInWater() || player.fallDistance > 3.0f) return@safeListener

                        player.motionY = 0.1
                        player.motionX = 0.0
                        player.motionZ = 0.0
                        return@safeListener
                    }

                    if (!isInputting() || !applyMotion) return@safeListener

                    val dir = calcMoveRad()
                    player.motionX = -sin(dir) * 0.2873
                    player.motionZ = cos(dir) * 0.2873
                }

                Mode.NCPNew -> {
                    val dir = calcMoveRad()

                    val block1 = isLiquidAt(-0.021)
                    val block2 = isLiquidAt(0.02)
                    val block3 = isLiquidAt(0.3)

                    if (!block1) return@safeListener
                    player.motionY = -0.04

                    if (!player.calcIsInWater()) return@safeListener
                    player.motionY = -0.000000001 // UpdatedNCP meme

                    val shouldJump = player.ticksExisted % 15 == 0
                    if (shouldJump) player.motionY = 0.035

                    val speed = if (shouldJump || !isInputting()) 0.0 else 0.2873

                    player.motionX = -sin(dir) * speed
                    player.motionZ = cos(dir) * speed

                    if (block2) player.motionY = 0.01
                    if (block3) player.motionY = 0.18

                    if (block2 || block3) {
                        player.motionX = 0.0
                        player.motionZ = 0.0
                    }

                    if (player.collidedHorizontally && isInputting()) {
                        player.motionY = 0.15
                        player.motionX = -sin(dir) * 0.2873
                        player.motionZ = cos(dir) * 0.2873
                    }
                }

                Mode.NCPDolphin1 -> {
                    if (!player.calcIsInWater()) return@safeListener

                    if (isLiquid(0.2)) {
                        player.motionY = 0.18

                        player.motionX = 0.0
                        player.motionZ = 0.0
                        return@safeListener
                    }

                    player.motionY = 0.11

                    val dir = calcMoveRad()

                    if (!isInputting()) {
                        player.motionX = 0.0
                        player.motionZ = 0.0
                        return@safeListener
                    }

                    player.motionX = -sin(dir) * 0.2873
                    player.motionZ = cos(dir) * 0.2873
                }

                Mode.NCPDolphin2 -> {
                    if (!player.calcIsInWater()) return@safeListener

                    if (isLiquid(0.11)) {
                        player.motionY = 0.1

                        if (isLiquid(0.2))
                            player.motionY = 0.18

                        player.motionX = 0.0
                        player.motionZ = 0.0
                        return@safeListener
                    }

                    player.motionY = 0.05

                    val dir = calcMoveRad()

                    if (!isInputting()) {
                        player.motionX = 0.0
                        player.motionZ = 0.0
                        return@safeListener
                    }

                    player.motionX = -sin(dir) * 0.2873
                    player.motionZ = cos(dir) * 0.2873
                }

                Mode.MatrixYPort -> {
                    if (!shouldSpoof()) {
                        if (player.calcIsInWater()) {
                            player.motionY = 0.18
                            player.motionX = 0.0
                            player.motionZ = 0.0
                            return@safeListener
                        }

                        if (player.onGround && isLiquidAt(-0.01) && !isLiquidAt(0.01) && isInputting()) {
                            player.motionY = 0.3
                            player.motionX *= 0.7
                            player.motionZ *= 0.7
                        }

                        return@safeListener
                    }

                    if (player.collidedHorizontally) {
                        player.motionY = 0.1

                        player.motionX = 0.0
                        player.motionZ = 0.0
                        return@safeListener
                    }

                    if (!isInputting()) {
                        player.motionX = 0.0
                        player.motionZ = 0.0
                        return@safeListener
                    }

                    val dir = calcMoveRad()
                    val speed = if (player.ticksExisted % 2 != 0) matrixYPortSpeed else (matrixYPortSpeed - 1.0)
                    player.motionX = -sin(dir) * speed
                    player.motionZ = cos(dir) * speed
                }

                Mode.MatrixNew -> {
                    if (!shouldSpoof()) {
                        if (player.calcIsInWater()) {
                            player.motionY = 0.18
                            player.motionX = 0.0
                            player.motionZ = 0.0
                            return@safeListener
                        }

                        if (player.onGround && isLiquidAt(-0.01) && !isLiquidAt(0.01) && isInputting()) {
                            player.motionY = 0.3
                            player.motionX *= 0.7
                            player.motionZ *= 0.7
                        }

                        return@safeListener
                    }

                    if (player.collidedHorizontally) {
                        player.motionY = 0.1

                        player.motionX = 0.0
                        player.motionZ = 0.0
                        return@safeListener
                    }

                    if (ticksInWater > 5 && player.ticksExisted % 2 != 0) {
                        player.motionY = 0.235
                        player.isAirBorne = true

                        player.motionX *= 0.1
                        player.motionZ *= 0.1
                        return@safeListener
                    }

                    if (!isInputting()) {
                        player.motionX = 0.0
                        player.motionZ = 0.0
                        return@safeListener
                    }

                    val dir = calcMoveRad()
                    player.motionX = -sin(dir) * matrixNewSpeed
                    player.motionZ = cos(dir) * matrixNewSpeed
                }
            }
        }

        safeListener<CollisionEvent> { event ->
            if (event.block !is BlockLiquid) return@safeListener
            if (event.entity == null) return@safeListener
            if (event.entity != player) return@safeListener

            if (!arrayListOf(Mode.NCP, Mode.MatrixYPort, Mode.MatrixNew).contains(mode)) return@safeListener
            if (mode == Mode.NCP && player.fallDistance > 3.0f) return@safeListener

            if (player.calcIsInWater() || player.posY < event.pos.y) return@safeListener
            if (!isLiquid(-0.2)) return@safeListener

            event.collidingBoxes.add(box.offset(event.pos))
        }
    }

    private fun SafeClientEvent.shouldSpoof(): Boolean {
        return isLiquid(0.001) && !isLiquid(0.02)
    }

    private fun SafeClientEvent.isLiquid(dist: Double): Boolean {
        val y = player.posY + dist
        val flooredY = y.toInt()

        for (x in player.posX.floorToInt() until player.posX.ceilToInt()) {
            for (z in player.posZ.floorToInt() until player.posZ.ceilToInt()) {
                if (world.getBlockState(BlockPos(x, flooredY, z)).block is BlockLiquid) return true
            }
        }

        return false
    }

    private fun SafeClientEvent.isLiquidAt(yShift: Double): Boolean {
        val blockPos = BlockPos(player.posX, player.posY + yShift, player.posZ)
        val block = world.getBlockState(blockPos).block
        return block is BlockLiquid
    }
}