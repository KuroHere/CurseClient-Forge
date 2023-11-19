package com.curseclient.client.module.modules.movement

import com.curseclient.client.event.events.MoveEvent
import com.curseclient.client.event.events.PacketEvent
import com.curseclient.client.event.listener.runSafe
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.entity.isInsideBlock
import com.curseclient.client.utility.extension.settingName
import com.curseclient.client.utility.player.MovementUtils.calcMoveRad
import com.curseclient.client.utility.player.MovementUtils.isInputting
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.cos
import kotlin.math.sin

object Phase: Module(
    "Phase",
    "Allows you to walk into block",
    Category.MOVEMENT
) {
    private val mode by setting("Mode", Mode.Vanilla)
    private val insideOnly by setting("Inside Only", false, { listOf(Mode.Vanilla, Mode.Setback).contains(mode) })

    private const val PHASE_DIST = 0.0624

    private enum class Mode {
        Vanilla,
        Setback,
        Skip
    }

    override fun getHudInfo() = mode.settingName

    override fun onDisable() {
        runSafe {
            player.noClip = false
        }
    }

    init {
        safeListener<TickEvent.ClientTickEvent>{
            when (mode) {
                Mode.Vanilla -> {

                }
                else -> {}
            }
        }

        safeListener<MoveEvent> {
            when (mode) {
                Mode.Vanilla -> {
                    if (insideOnly && !mc.player.isInsideBlock) return@safeListener
                    if (player.collidedHorizontally) {
                        val dir = calcMoveRad()
                        val tpVec = player.positionVector.add(-sin(dir) * 0.001, 0.0, cos(dir) * 0.001)
                        player.setPosition(tpVec.x, tpVec.y, tpVec.z)
                    }
                }

                Mode.Setback -> {
                    if (insideOnly && !mc.player.isInsideBlock) return@safeListener
                    if (player.collidedHorizontally) {
                        player.setVelocity(0.0, 0.0, 0.0)
                        val dir = calcMoveRad()

                        val xOffset = -sin(dir) * PHASE_DIST
                        val zOffset = cos(dir) * PHASE_DIST

                        val moveVec = player.positionVector.add(xOffset, 0.0, zOffset)
                        val setbackVec = moveVec.add(
                            0.0,
                            1337.0,
                            0.0
                        )

                        val movePacket = CPacketPlayer.Position(moveVec.x, moveVec.y, moveVec.z, true)
                        val setbackPacket = CPacketPlayer.Position(setbackVec.x, setbackVec.y, setbackVec.z, true)

                        connection.sendPacket(movePacket)
                        connection.sendPacket(setbackPacket)
                    }
                }

                Mode.Skip -> {
                    val phaseBB = player.entityBoundingBox.shrink(0.0625)
                    val phased = world.collidesWithAnyBlock(phaseBB)

                    val fullBB = player.entityBoundingBox
                    val isInside = world.collidesWithAnyBlock(fullBB)

                    val dir = calcMoveRad()
                    if (!isInputting()) return@safeListener

                    if (player.collidedHorizontally) {
                        val velocity = Vec3d(-sin(dir) * 0.001, 0.0, cos(dir) * 0.001)
                        val vec = player.positionVector.add(velocity)
                        player.setPosition(vec.x, vec.y, vec.z)
                        player.setVelocity(velocity.x, velocity.y, velocity.z)
                    }

                    val speed = if (phased) 0.24 else if (isInside) 0.01 else return@safeListener

                    player.motionX = -sin(dir) * speed
                    player.motionZ = cos(dir) * speed
                }
            }
        }

        safeListener<PacketEvent.Send> {
            if (mode != Mode.Skip) return@safeListener
            if (it.packet !is CPacketPlayer) return@safeListener

            val phaseBB = player.entityBoundingBox.shrink(0.0625)
            val phased = world.collidesWithAnyBlock(phaseBB)
            if (!phased) return@safeListener

            it.cancel()
        }
    }
}