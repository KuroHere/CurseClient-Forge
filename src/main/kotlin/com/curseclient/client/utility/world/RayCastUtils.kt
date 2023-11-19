package com.curseclient.client.utility.world

import com.curseclient.client.event.SafeClientEvent
import net.minecraft.entity.Entity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

object RayCastUtils {
    fun SafeClientEvent.rayTrace(start: Vec3d, end: Vec3d, stopOnLiquid: Boolean = false, ignoreBlockWithoutBoundingBox: Boolean = false, returnLastUncollidableBlock: Boolean = false): RayTraceResult? =
        world.rayTraceBlocks(start, end, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock)

    fun SafeClientEvent.checkSideVisibility(blockPos: BlockPos, side: EnumFacing): Boolean {
        val eyePos = player.getPositionEyes(1.0f)
        val vec = side.directionVec
        val castVec = Vec3d(blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble())
            .add(0.5, 0.5, 0.5) // center
            .add(vec.x * 0.45, vec.y * 0.45, vec.z * 0.45)

        val traceResult = rayTrace(eyePos, castVec) ?: return false
        return traceResult.blockPos == blockPos && traceResult.sideHit == side
    }

    fun SafeClientEvent.getVisibleSidesRayTrace(blockPos: BlockPos): List<EnumFacing> {
        return EnumFacing.values().filter { side -> checkSideVisibility(blockPos, side) }
    }

    fun SafeClientEvent.getGroundPos(entity: Entity): Vec3d {
        val results = rayTraceBoundingBoxToGround(entity, false)
        if (results.all { it.typeOfHit == RayTraceResult.Type.MISS || (it.hitVec?.y ?: 911.0) < 0.0 }) {
            return Vec3d(0.0, -999.0, 0.0)
        }

        return results.maxByOrNull { it.hitVec?.y ?: -69420.0 }?.hitVec ?: Vec3d(0.0, -69420.0, 0.0)
    }

    private fun SafeClientEvent.rayTraceToGround(vec3d: Vec3d, stopOnLiquid: Boolean): RayTraceResult? {
        return rayTrace(
            vec3d,
            Vec3d(vec3d.x, -1.0, vec3d.z),
            stopOnLiquid,
            ignoreBlockWithoutBoundingBox = true,
            returnLastUncollidableBlock = false
        )
    }

    private fun SafeClientEvent.rayTraceBoundingBoxToGround(entity: Entity, stopOnLiquid: Boolean): List<RayTraceResult> {
        val boundingBox = entity.entityBoundingBox
        val xArray = arrayOf(floor(boundingBox.minX), floor(boundingBox.maxX))
        val zArray = arrayOf(floor(boundingBox.minZ), floor(boundingBox.maxZ))

        val results = ArrayList<RayTraceResult>(4)

        for (x in xArray) {
            for (z in zArray) {
                val result = rayTraceToGround(Vec3d(x, boundingBox.minY, z), stopOnLiquid)
                if (result != null) {
                    results.add(result)
                }
            }
        }

        return results
    }

    fun SafeClientEvent.castFromAngle(yaw: Float, pitch: Float, dist: Double): RayTraceResult? {
        val start = player.getPositionEyes(1.0f)
        val end = start.add(
            -sin(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble())) * dist,
            -sin(Math.toRadians(pitch.toDouble())) * dist,
            cos(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble())) * dist
        )

        return rayTrace(start, end)
    }
}