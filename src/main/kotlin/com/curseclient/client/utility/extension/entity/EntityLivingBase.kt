package com.curseclient.client.utility.extension.entity

import com.curseclient.client.utility.math.MathUtils.ceilToInt
import com.curseclient.client.utility.math.MathUtils.floorToInt
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.render.RenderTessellator
import net.minecraft.block.BlockLiquid
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.math.hypot

private val mc = Minecraft.getMinecraft()

val EntityLivingBase.speed get() = hypot(motionX, motionZ)

fun EntityLivingBase.calcIsInWater(): Boolean {
    mc.world?.let {
        val y = (this.posY + 0.01).floorToInt()

        for (x in this.posX.floorToInt() until this.posX.ceilToInt()) {
            for (z in this.posZ.floorToInt() until this.posZ.ceilToInt()) {
                val pos = BlockPos(x, y, z)
                if (it.getBlockState(pos).block is BlockLiquid) return true
            }
        }
    }

    return false
}

val Entity.positionVectorPrev get() = Vec3d(this.prevPosX, this.prevPosY, this.prevPosZ)

val EntityLivingBase.lastTickPositionVector get() = Vec3d(this.lastTickPosX, this.lastTickPosY, this.lastTickPosZ)

val EntityLivingBase.interpolatedPosition get() = lerp(this.lastTickPositionVector, this.positionVector, RenderTessellator.partialTicks.toDouble())

val EntityLivingBase.flooredPosition get() = BlockPos(posX.floorToInt(), posY.floorToInt(), posZ.floorToInt())

val EntityLivingBase.isMoving
    get() = speed > 0.01

fun EntityLivingBase.isCentered(pos: BlockPos) =
    this.posX in pos.x + 0.31..pos.x + 0.69
        && this.posZ in pos.z + 0.31..pos.z + 0.69

val EntityLivingBase.isInsideBlock get() = world.collidesWithAnyBlock(this.entityBoundingBox)

fun EntityLivingBase.getInterpolatedBox(): AxisAlignedBB {
    val boxIn = entityBoundingBox
    val xDiff = boxIn.maxX - boxIn.minX
    val yDiff = boxIn.maxY - boxIn.minY
    val zDiff = boxIn.maxZ - boxIn.minZ

    val pos = interpolatedPosition
    return AxisAlignedBB(pos.x - xDiff / 2.0, pos.y, pos.z - zDiff / 2.0, pos.x + xDiff / 2.0, pos.y + yDiff, pos.z + zDiff / 2.0)
}