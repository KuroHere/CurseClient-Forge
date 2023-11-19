package com.curseclient.client.utility.world.placement

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.utility.world.WorldUtils.getHitVec
import com.curseclient.client.utility.world.WorldUtils.getHitVecOffset
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class PlaceInfo(
    val pos: BlockPos,
    val side: EnumFacing,
    val dist: Double,
    val hitVecOffset: Vec3d,
    val hitVec: Vec3d,
    val placedPos: BlockPos
) {
    companion object {
        fun SafeClientEvent.newPlaceInfo(pos: BlockPos, side: EnumFacing): PlaceInfo {
            val hitVecOffset = getHitVecOffset(side)
            val hitVec = getHitVec(pos, side)

            return PlaceInfo(pos, side, player.getPositionEyes(1.0f).distanceTo(hitVec), hitVecOffset, hitVec, pos.offset(side))
        }
    }

    fun getPlacePacket(hand: EnumHand) =
        CPacketPlayerTryUseItemOnBlock(this.pos, this.side, hand, hitVecOffset.x.toFloat(), hitVecOffset.y.toFloat(), hitVecOffset.z.toFloat())
}