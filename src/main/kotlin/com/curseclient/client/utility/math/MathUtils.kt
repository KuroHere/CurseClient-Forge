package com.curseclient.client.utility.math

import baritone.api.utils.Helper.mc
import com.curseclient.client.utility.extension.mixins.renderPosX
import com.curseclient.client.utility.extension.mixins.renderPosY
import com.curseclient.client.utility.extension.mixins.renderPosZ
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.*


object MathUtils {

    fun interpolate(oldValue: Double, newValue: Double, interpolationValue: Double): Double {
        return oldValue + (newValue - oldValue) * interpolationValue
    }

    fun interpolateFloat(oldValue: Float, newValue: Float, interpolationValue: Double): Float {
        return interpolate(oldValue.toDouble(), newValue.toDouble(), interpolationValue.toFloat().toDouble()).toFloat()
    }

    fun interpolateInt(oldValue: Int, newValue: Int, interpolationValue: Double): Int {
        return interpolate(oldValue.toDouble(), newValue.toDouble(), interpolationValue.toFloat().toDouble()).toInt()
    }

    fun roundToHalf(d: Double): Double {
        return Math.round(d * 2) / 2.0
    }

    fun round(num: Double, increment: Double): Double {
        var bd = BigDecimal(num)
        bd = bd.setScale(increment.toInt(), RoundingMode.HALF_UP)
        return bd.toDouble()
    }

    fun round(value: Double, places: Int): Double {
        require(!(places < 0))
        var bd = BigDecimal(value)
        bd = bd.setScale(places, RoundingMode.HALF_UP)
        return bd.toDouble()
    }

    fun round(value: String?, places: Int): String {
        if (places < 0) {
            throw IllegalArgumentException()
        }
        var bd = BigDecimal(value)
        bd = bd.stripTrailingZeros()
        bd = bd.setScale(places, RoundingMode.HALF_UP)
        return bd.toString()
    }

    fun random(min: Double, max: Double): Double {
        return ThreadLocalRandom.current().nextDouble() * (max - min) + min
    }

    fun random(min: Float, max: Float): Float {
        return (Math.random() * (max - min) + min).toFloat()
    }

    fun calculateGaussianValue(x: Float, sigma: Float): Float {
        val PI = 3.141592653
        val output = 1.0 / sqrt(2.0 * PI * (sigma * sigma))
        return (output * exp(-(x * x) / (2.0 * (sigma * sigma)))).toFloat()
    }

    fun wrap(value: Float): Float {
        var value = value
        value %= 360.0f
        if (value >= 180.0f) value -= 360.0f
        if (value < -180.0f) value += 360.0f
        return value
    }

    fun Float.unwrap(): Float {
        var unwrappedAngle = this % 360
        if (unwrappedAngle < 0) {
            unwrappedAngle += 360
        }
        return unwrappedAngle
    }

    // linearly maps value from the range (a..b) to (c..d)
    fun map(value: Double, a: Double, b: Double, c: Double, d: Double): Double {
        // first map value from (a..b) to (0..1)
        var value = value
        value = (value - a) / (b - a)
        // then map it from (0..1) to (c..d) and return it
        return c + value * (d - c)
    }

    fun Double.roundToPlaces(places: Int): Double {
        val scale = 10.0.pow(places.toDouble())
        return round(this * scale) / scale
    }

    fun Float.roundToPlaces(places: Int): Float {
        val scale = 10.0f.pow(places)
        return round(this * scale) / scale
    }

    @JvmStatic
    fun getInterpolatedPos(entity: Entity, ticks: Float): Vec3d {
        return Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ).add(getInterpolatedAmount(entity, ticks))
    }

    @JvmStatic
    fun getInterpolatedAmount(entity: Entity, ticks: Float): Vec3d {
        return getInterpolatedAmount(entity, ticks.toDouble(), ticks.toDouble(), ticks.toDouble())
    }

    @JvmStatic
    fun getInterpolatedAmount(entity: Entity, x: Double, y: Double, z: Double): Vec3d {
        return Vec3d((entity.posX - entity.lastTickPosX) * x, (entity.posY - entity.lastTickPosY) * y, (entity.posZ - entity.lastTickPosZ) * z)
    }

    @JvmStatic
    fun getInterpolateVec3dPos(pos: Vec3d, renderPartialTicks: Float): Vec3d {
        return Vec3d(calculateDistanceWithPartialTicks(pos.x, pos.x, renderPartialTicks) - mc.renderManager.renderPosX, calculateDistanceWithPartialTicks(pos.y, pos.y - 0.021, renderPartialTicks) - mc.renderManager.renderPosY, calculateDistanceWithPartialTicks(pos.z, pos.z, renderPartialTicks) - mc.renderManager.renderPosZ)
    }

    @JvmStatic
    fun getInterpolateEntityClose(entity: Entity, renderPartialTicks: Float): Vec3d {
        return Vec3d(calculateDistanceWithPartialTicks(entity.posX, entity.lastTickPosX, renderPartialTicks) - mc.renderManager.renderPosX, calculateDistanceWithPartialTicks(entity.posY, entity.lastTickPosY, renderPartialTicks) - mc.renderManager.renderPosY, calculateDistanceWithPartialTicks(entity.posZ, entity.lastTickPosZ, renderPartialTicks) - mc.renderManager.renderPosZ)
    }

    private inline fun calculateDistanceWithPartialTicks(n: Double, n2: Double, renderPartialTicks: Float): Double {
        return n2 + (n - n2) * renderPartialTicks
    }

    fun getInterpolatedRenderPos(entity: Entity, ticks: Float): Vec3d {
        return interpolateEntity(entity, ticks).subtract(Minecraft.getMinecraft().renderManager.viewerPosX, Minecraft.getMinecraft().renderManager.viewerPosY, Minecraft.getMinecraft().renderManager.viewerPosZ)
    }

    fun interpolateEntity(entity: Entity, time: Float): Vec3d {
        return Vec3d(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * time, entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * time, entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * time)
    }

    fun decimalPlaces(value: Double) = value.toString().split('.').getOrElse(1) { "0" }.length

    fun Boolean.toInt(): Int {
        return if (this) 1 else 0
    }

    fun Boolean.toIntSign(): Int {
        return if (this) 1 else -1
    }

    fun square(input: Double): Double {
        return input * input
    }

    fun Double.floorToInt() = floor(this).toInt()
    fun Double.ceilToInt() = ceil(this).toInt()

    fun IntRange.random() =
        Random().nextInt((endInclusive + 1) - start) + start

    fun clamp(value: Double, min: Double, max: Double): Double {
        return max(min(value, max), min)
    }

    fun clamp(value: Float, min: Float, max: Float): Float {
        return max(min(value, max), min)
    }

    fun lerp(a: Float, b: Float, t: Float): Float {
        return a + ((b - a) * clamp(t, 0.0f, 1.0f))
    }

    fun lerp(a: Double, b: Double, t: Double): Double {
        return a + ((b - a) * clamp(t, 0.0, 1.0))
    }

    fun lerp(a: Vec3d, b: Vec3d, t: Double): Vec3d {
        val x = lerp(a.x, b.x, t)
        val y = lerp(a.y, b.y, t)
        val z = lerp(a.z, b.z, t)

        return Vec3d(x, y, z)
    }

    fun lerp(a: Vec2d, b: Vec2d, t: Double): Vec2d {
        val x = lerp(a.x, b.x, t)
        val y = lerp(a.y, b.y, t)

        return Vec2d(x, y)
    }

    fun normalize(value: Double, minIn: Double, maxIn: Double, minOut: Double, maxOut: Double) =
        lerp(minOut, maxOut, (value - minIn) / (maxIn - minIn))


}