package com.curseclient.client.utility.player

import com.curseclient.client.utility.math.MathUtils
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.hypot


object RotationUtils {
    val mc: Minecraft = Minecraft.getMinecraft()

    fun rotationsToEntity(entity: Entity): Vec2f {
        val box = entity.entityBoundingBox

        val eyePos = getEyePosition()

        if (mc.player.entityBoundingBox.intersects(box)) {
            return rotationsToVec(box.center)
        }

        val x = eyePos.x.coerceIn(box.minX, box.maxX)
        val y = eyePos.y.coerceIn(box.minY, box.maxY)
        val z = eyePos.z.coerceIn(box.minZ, box.maxZ)

        val hitVec = Vec3d(x, y, z)
        return rotationsToVec(hitVec)
    }

    fun rotationsToVec(pos: Vec3d): Vec2f {
        return rotationsToVec(getEyePosition(), pos)
    }

    fun rotationsToVec(posFrom: Vec3d, posTo: Vec3d): Vec2f {
        return getRawRotationsTo(posTo.subtract(posFrom))
    }

    fun rotationsToCenter(entity: Entity): Vec2f {
        return rotationsToVec(entity.entityBoundingBox.center)
    }

    fun getEyePosition(): Vec3d {
        return mc.player.getPositionEyes(1f)
    }

    private fun getRawRotationsTo(pos: Vec3d): Vec2f {
        val xz = hypot(pos.x, pos.z)
        val yaw = normalizeAngle(Math.toDegrees(atan2(pos.z, pos.x)) - 90.0)
        val pitch = normalizeAngle(Math.toDegrees(-atan2(pos.y, xz)))
        return Vec2f(yaw.toFloat(), pitch.toFloat())
    }
    
    fun lerpRotation(current: Vec2f, target: Vec2f, smoothFactorYaw: Float, smoothFactorPitch: Float): Vec2f {
        //yaw
        val yawFrom = current.x + 180
        val yawTo = target.x + 180

        var delta = MathUtils.clamp((yawTo - yawFrom - floor((yawTo - yawFrom) / 360) * 360).toDouble(), 0.0, 360.0)
        if (delta > 180) delta -= 360
        var outputYaw = yawFrom + delta * MathUtils.clamp(smoothFactorYaw.toDouble(), 0.0, 1.0)
        if (outputYaw > 360) outputYaw -= 360
        if (outputYaw < 0) outputYaw += 360

        outputYaw -= 180

        //pitch
        val outputPitch = current.y + (target.y - current.y) * smoothFactorPitch
        return Vec2f(outputYaw.toFloat(), outputPitch)
    }

    fun lerpYaw(from: Float, to: Float, t: Float): Float {
        //yaw
        val yawFrom = from + 180
        val yawTo = to + 180

        var delta = MathUtils.clamp((yawTo - yawFrom - floor((yawTo - yawFrom) / 360) * 360).toDouble(), 0.0, 360.0)
        if (delta > 180) delta -= 360
        var outputYaw = yawFrom + delta * MathUtils.clamp(t.toDouble(), 0.0, 1.0)
        if (outputYaw > 360) outputYaw -= 360
        if (outputYaw < 0) outputYaw += 360

        outputYaw -= 180

        //pitch
        return outputYaw.toFloat()
    }
    
    fun normalizeAngle(angleIn: Double): Double {
        var angle = angleIn
        angle %= 360.0
        if (angle >= 180.0) {
            angle -= 360.0
        }
        if (angle < -180.0) {
            angle += 360.0
        }
        return angle
    }

    fun normalizeAngle(angleIn: Float): Float {
        var angle = angleIn
        angle %= 360.0f

        if (angle >= 180.0f) {
            angle -= 360.0f
        } else if (angle < -180.0f) {
            angle += 360.0f
        }

        return angle
    }

    fun calcAbsAngleDiff(a: Float, b: Float): Float {
        return abs(a - b) % 180.0f
    }

    fun calcAngleDiff(a: Float, b: Float): Float {
        val diff = a - b
        return normalizeAngle(diff)
    }
}