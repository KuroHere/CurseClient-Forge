package com.curseclient.client.utility.world

import com.curseclient.client.event.SafeClientEvent
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import java.util.*

object WorldUtils {
    private val mc = Minecraft.getMinecraft()

    fun BlockPos.getBlockState(): IBlockState {
        return mc.player.world.getBlockState(this)
    }

    fun SafeClientEvent.getBlockState(blockPos: BlockPos): IBlockState{
        return world.getBlockState(blockPos)
    }

    private val IBlockState.isFullBox: Boolean
        get() = mc.world?.let {
            this.getCollisionBoundingBox(it, BlockPos.ORIGIN)
        } == Block.FULL_BLOCK_AABB

    fun getHitVec(pos: BlockPos, facing: EnumFacing): Vec3d {
        val vec = facing.directionVec
        return Vec3d(vec.x * 0.5 + 0.5 + pos.x, vec.y * 0.5 + 0.5 + pos.y, vec.z * 0.5 + 0.5 + pos.z)
    }

    fun getHitVecOffset(facing: EnumFacing): Vec3d {
        val vec = facing.directionVec
        return Vec3d(vec.x * 0.5 + 0.5, vec.y * 0.5 + 0.5, vec.z * 0.5 + 0.5)
    }

    fun SafeClientEvent.getVisibleSides(pos: BlockPos, assumeAirAsFullBox: Boolean = false): List<EnumFacing> {
        val visibleSides = EnumSet.noneOf(EnumFacing::class.java)

        val eyePos = player.getPositionEyes(1.0f)
        val blockCenter = pos.toVec3dCenter()
        val blockState = world.getBlockState(pos)
        val isFullBox = assumeAirAsFullBox && blockState.block == Blocks.AIR || blockState.isFullBox

        return visibleSides
            .checkAxis(eyePos.x - blockCenter.x, EnumFacing.WEST, EnumFacing.EAST, !isFullBox)
            .checkAxis(eyePos.y - blockCenter.y, EnumFacing.DOWN, EnumFacing.UP, true)
            .checkAxis(eyePos.z - blockCenter.z, EnumFacing.NORTH, EnumFacing.SOUTH, !isFullBox)
            .map { it }
    }

    fun SafeClientEvent.getVisibleSidesSmart(pos: BlockPos, assumeAirAsFullBox: Boolean = false): List<EnumFacing> {
        val sidesRaw = getVisibleSides(pos, assumeAirAsFullBox)
        return sidesRaw.filter { !pos.add(it.directionVec).getBlockState().isFullBox }
    }

    private fun Vec3i.toVec3dCenter(): Vec3d {
        return Vec3d(x + 0.5, y + 0.5, z + 0.5)
    }

    private fun EnumSet<EnumFacing>.checkAxis(diff: Double, negativeSide: EnumFacing, positiveSide: EnumFacing, bothIfInRange: Boolean) =
        this.apply {
            when {
                diff < -0.5 -> {
                    add(negativeSide)
                }
                diff > 0.5 -> {
                    add(positiveSide)
                }
                else -> {
                    if (bothIfInRange) {
                        add(negativeSide)
                        add(positiveSide)
                    }
                }
            }
        }
}