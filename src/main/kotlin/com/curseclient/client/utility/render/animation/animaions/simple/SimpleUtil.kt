package com.curseclient.client.utility.render.animation.animaions.simple

import com.curseclient.client.utility.math.MathUtils.clamp
import kotlin.math.max
import kotlin.math.min

object SimpleUtil {

    fun animate(target: Float, current: Float, speed: Double, delta: Long): Float {
        var cur = current
        val diff = cur - target
        val add = delta * (speed / 50)
        if (diff > speed) {
            if (cur - add > target) {
                cur -= add.toFloat()
            } else {
                cur = target
            }
        } else if (diff < -speed) {
            if (cur + add < target) {
                cur += add.toFloat()
            } else {
                cur = target
            }
        } else {
            cur = target
        }
        return cur
    }

    fun animate(target: Double, current: Double, speed: Double): Double {
        var cur = current
        var spd = speed
        if (cur == target) return cur
        val larger = target > cur
        if (spd < 0.0) {
            spd = 0.0
        } else if (spd > 1.0) {
            spd = 1.0
        }
        val dif = maxOf(target, cur) - minOf(target, cur)
        var factor = dif * spd
        if (factor < 0.1) {
            factor = 0.1
        }
        if (larger) {
            cur += factor
            if (cur >= target) cur = target
        } else if (target < cur) {
            cur -= factor
            if (cur <= target) cur = target
        }
        return cur
    }

    fun animate(target: Float, current: Float, speed: Float): Float {
        var cur = current
        var spd = speed
        if (cur == target) return cur
        val larger = target > cur
        if (spd < 0.0f) {
            spd = 0.0f
        } else if (spd > 1.0f) {
            spd = 1.0f
        }
        val dif: Float = maxOf(target, cur) - minOf(target, cur)
        var factor = dif * spd.toDouble()
        if (factor < 0.1) {
            factor = 0.1
        }
        if (larger) {
            cur += factor.toFloat()
            if (cur >= target) cur = target
        } else if (target < cur) {
            cur -= factor.toFloat()
            if (cur <= target) cur = target
        }
        return cur
    }

    fun changer(current: Float, add: Double, min: Float, max: Float): Double {
        var cur = current.toDouble()
        cur += add
        if (cur > max) {
            cur = max.toDouble()
        }
        if (cur < min) {
            cur = min.toDouble()
        }
        return cur
    }

    fun changer(current: Float, add: Float, min: Int, max: Float): Float {
        var cur = current
        cur += add
        if (cur > max) {
            cur = max
        }
        if (cur < min) {
            cur = min.toFloat()
        }
        return cur
    }

    fun changer(current: Float, add: Float, min: Float, max: Float): Float {
        var cur = current
        cur += add
        if (cur > max) {
            cur = max
        }
        if (cur < min) {
            cur = min
        }
        return cur
    }

    fun getAnimationProgressFloat(startTime: Long, duration: Int): Float {
        return clamp((System.currentTimeMillis() - startTime) / duration.toFloat(), 0.0f, 1.0f)
    }

    fun getAnimationProgressDouble(startTime: Long, duration: Int): Double {
        return clamp((System.currentTimeMillis() - startTime) / duration.toDouble(), 0.0, 1.0)
    }
}


