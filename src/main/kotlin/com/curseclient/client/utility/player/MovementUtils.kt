package com.curseclient.client.utility.player

import com.curseclient.client.module.modules.player.FreeCam
import com.curseclient.client.utility.extension.entity.flooredPosition
import com.curseclient.client.utility.extension.entity.isCentered
import com.curseclient.client.utility.extension.entity.speed
import com.curseclient.client.utility.math.MathUtils.toInt
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP

object MovementUtils {
    private val mc = Minecraft.getMinecraft()

    private val roundedForward get() = getRoundedMovementInput(mc.player.movementInput.moveForward)
    private val roundedStrafing get() = getRoundedMovementInput(mc.player.movementInput.moveStrafe)
    private fun getRoundedMovementInput(input: Float) = when {
        input > 0f -> 1f
        input < 0f -> -1f
        else -> 0f
    }

    fun isInputting(): Boolean {
        if (FreeCam.isEnabled()) return false
        return getRoundedMovementInput(mc.player.movementInput.moveForward) != 0f || getRoundedMovementInput(mc.player.movementInput.moveStrafe) != 0f
    }

    val verticalMovement get() =
        mc.player.movementInput.jump.toInt() - mc.player.movementInput.sneak.toInt()

    fun calcMoveYaw(yawIn: Float = mc.player.rotationYaw, moveForward: Float = roundedForward, moveString: Float = roundedStrafing): Double {
        var strafe = 90 * moveString
        strafe *= if (moveForward != 0F) moveForward * 0.5F else 1F

        var yaw = yawIn - strafe
        yaw -= if (moveForward < 0F) 180 else 0

        return yaw.toDouble()
    }

    fun calcMoveRad(): Double{
        return Math.toRadians(calcMoveYaw())
    }

    fun EntityPlayerSP.centerPlayer(): Boolean {
        val center = net.minecraft.util.math.Vec3d(this.flooredPosition).add(0.5, 0.0, 0.5)
        val centered = isCentered(this.flooredPosition)

        if (!centered) {
            this.motionX = (center.x - this.posX) / 2.0
            this.motionZ = (center.z - this.posZ) / 2.0

            val speed = this.speed

            if (speed > 0.28) {
                val multiplier = 0.28 / speed
                this.motionX *= multiplier
                this.motionZ *= multiplier
            }
        }

        return centered
    }
}