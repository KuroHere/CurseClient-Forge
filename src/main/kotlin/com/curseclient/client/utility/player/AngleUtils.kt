package com.curseclient.client.utility.player

import baritone.api.utils.Helper.mc
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import kotlin.math.atan2
import kotlin.math.hypot


object AngleUtils {
    val EnumFacing.yaw: Float
        get() = when (this) {
            EnumFacing.NORTH -> -180.0f
            EnumFacing.SOUTH -> 0.0f
            EnumFacing.EAST -> -90.0f
            EnumFacing.WEST -> 90.0f
            else -> 0.0f
        }

    fun calculateAngles(to: BlockPos): Rotation {
        val yaw = (Math.toDegrees(atan2(to.z - mc.player.getPositionEyes(1f).z, to.x - mc.player.getPositionEyes(1f).x)) - 90).toFloat()
        val pitch = Math.toDegrees(-atan2(to.y - mc.player.getPositionEyes(1f).y, hypot(to.x - mc.player.getPositionEyes(1f).x, to.z - mc.player.getPositionEyes(1f).z))).toFloat()

        // wrap the degrees to values between -180 and 180
        return Rotation(MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch))
    }

}