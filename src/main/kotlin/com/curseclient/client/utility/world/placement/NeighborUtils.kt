package com.curseclient.client.utility.world.placement

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.utility.world.WorldUtils.getHitVec
import com.curseclient.client.utility.world.WorldUtils.getHitVecOffset
import com.curseclient.client.utility.world.WorldUtils.getVisibleSidesSmart
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

object NeighborUtils {
    fun SafeClientEvent.getNeighbor(
        pos: BlockPos,
        attempts: Int = 3,
        range: Float = 4.25f,
        visibleSideCheck: Boolean = false,
        sides: Array<EnumFacing> = EnumFacing.values(),
        hitboxCheck: (BlockPos) -> Boolean = { world.checkNoEntityCollision(AxisAlignedBB(pos), null) }
    ) =
        getNeighbor(player.getPositionEyes(1.0f), pos, attempts, range, visibleSideCheck, sides, pos, 0, hitboxCheck)

    private fun SafeClientEvent.getNeighbor(
        eyePos: Vec3d,
        pos: BlockPos,
        attempts: Int,
        range: Float,
        visibleSideCheck: Boolean,
        sides: Array<EnumFacing>,
        origin: BlockPos,
        lastDist: Int,
        hitboxCheck: (BlockPos) -> Boolean
    ): PlaceInfo? {
        for (side in sides) {
            val result = checkNeighbor(eyePos, pos, side, range, visibleSideCheck, true, origin, lastDist, hitboxCheck)
            if (result != null) return result
        }

        if (attempts > 1) {
            for (side in sides) {
                val newPos = pos.offset(side)
                if (!world.getBlockState(newPos).material.isReplaceable) continue

                return getNeighbor(eyePos, newPos, attempts - 1, range, visibleSideCheck, sides, origin, lastDist + 1, hitboxCheck)
                    ?: continue
            }
        }

        return null
    }

    private fun SafeClientEvent.checkNeighbor(
        eyePos: Vec3d,
        pos: BlockPos,
        side: EnumFacing,
        range: Float,
        visibleSideCheck: Boolean,
        checkReplaceable: Boolean,
        origin: BlockPos,
        lastDist: Int,
        hitboxCheck: (BlockPos) -> Boolean
    ): PlaceInfo? {
        val offsetPos = pos.offset(side)
        val oppositeSide = side.opposite

        val distToOrigin = (offsetPos.x - origin.x).sq + (offsetPos.y - origin.y).sq + (offsetPos.z - origin.z).sq
        if (distToOrigin <= lastDist.sq) return null

        val hitVec = getHitVec(offsetPos, oppositeSide)
        val dist = eyePos.distanceTo(hitVec)

        if (dist > range) return null
        if (visibleSideCheck && !getVisibleSidesSmart(offsetPos, true).contains(oppositeSide)) return null
        if (checkReplaceable && world.getBlockState(offsetPos).material.isReplaceable) return null
        if (!world.getBlockState(pos).material.isReplaceable) return null
        if (!hitboxCheck(pos)) return null

        val hitVecOffset = getHitVecOffset(oppositeSide)
        return PlaceInfo(offsetPos, oppositeSide, dist, hitVecOffset, hitVec, pos)
    }

    private inline val Int.sq: Int get() = this * this
}