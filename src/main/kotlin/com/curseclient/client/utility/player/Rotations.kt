package com.curseclient.client.utility.player

import com.curseclient.client.module.modules.combat.KillAura
import com.curseclient.client.module.modules.combat.KillAura.maxPitch
import com.curseclient.client.utility.math.GCDFix.getFixedRotation
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private var yawStatic = 0.0f
private var pitchStatic = 0.0f

enum class RotationsMode{
    CENTER,
    SMART,
    MATRIX
}

object Rotations {
    val mc: Minecraft = Minecraft.getMinecraft()
    fun getRotationsByMode(entity: Entity, mode: RotationsMode): Vec2f {
        var output: Vec2f?

        output = when(mode){
            RotationsMode.CENTER -> {
                getCenter(entity)
            }

            RotationsMode.SMART -> {
                getSMART(entity)
            }

            RotationsMode.MATRIX -> {
                getMatrix(entity)
            }
        }

        if (output.y > maxPitch) output = Vec2f(output.x, maxPitch.toFloat())
        if (output.y < -maxPitch) output = Vec2f(output.x, -maxPitch.toFloat())

        return output
    }

    private fun getCenter(entity: Entity): Vec2f {
        return RotationUtils.rotationsToCenter(entity)
    }

    private fun getSMART(entity: Entity): Vec2f {
        val box = entity.entityBoundingBox
        val eyePos = RotationUtils.getEyePosition()
        val center = entity.entityBoundingBox.center

        if (RotationUtils.mc.player.entityBoundingBox.intersects(box)) {
            return RotationUtils.rotationsToVec(box.center)
        }

        val y = eyePos.y.coerceIn(box.minY, box.maxY)

        val hitVec = Vec3d(center.x, y, center.z)
        return RotationUtils.rotationsToVec(hitVec)
    }

    private fun getMatrix(entity: Entity): Vec2f {
        val vec = entity.positionVector.add(Vec3d(0.0, MathHelper.clamp(entity.eyeHeight.toDouble() * (getDist(entity) / (KillAura.reach + entity.width)), 0.2, mc.player.getEyeHeight().toDouble()), 0.0))
        val diffX = vec.x - mc.player.posX
        val diffY = vec.y - (mc.player.posY + mc.player.getEyeHeight().toDouble())
        val diffZ = vec.z - mc.player.posZ
        val dist = MathHelper.sqrt(diffX * diffX + diffZ * diffZ).toDouble()
        var yawTo = (Math.toDegrees(atan2(diffZ, diffX)) - 90.0 + getFixedRotation((sin((System.currentTimeMillis() / 30L).toDouble()) * 2.0).toFloat()).toDouble()).toFloat()
        var pitchTo = (-Math.toDegrees(atan2(diffY, dist)) + getFixedRotation((cos((System.currentTimeMillis() / 30L).toDouble()) * 2.0).toFloat()).toDouble()).toFloat()
        yawTo = mc.player.rotationYaw + getFixedRotation(MathHelper.wrapDegrees(yawTo - mc.player.rotationYaw))
        pitchTo = mc.player.rotationPitch + getFixedRotation(MathHelper.wrapDegrees(pitchTo - mc.player.rotationPitch))
        pitchTo = MathHelper.clamp(pitchTo, -90.0f, 90.0f)

        yawStatic = getFixedRotation(rotate(yawStatic, yawTo, 90.0f, 90.0f))
        pitchStatic = getFixedRotation(rotate(pitchStatic, pitchTo, 1.0f, 12.0f))

        return Vec2f(yawStatic, pitchStatic)
    }

    private fun rotate(from: Float, to: Float, minstep: Float, maxstep: Float): Float {
        var f = MathHelper.wrapDegrees(to - from) * MathHelper.clamp(0.6f, 0.0f, 1.0f)
        f = if (f < 0.0f) MathHelper.clamp(f, -maxstep, -minstep) else MathHelper.clamp(f, minstep, maxstep)
        return if (abs(f) > abs(MathHelper.wrapDegrees(to - from))) {
            to
        } else from + f
    }

    private fun getDist(entity: Entity): Double {
        val vec = entity.positionVector.add(Vec3d(0.0, MathHelper.clamp(entity.posY - mc.player.posY + mc.player.getEyeHeight().toDouble(), 0.0, entity.height.toDouble()), 0.0))
        return mc.player.positionVector.add(Vec3d(0.0, (mc.player.height / 2.0f).toDouble(), 0.0)).distanceTo(vec)
    }
}