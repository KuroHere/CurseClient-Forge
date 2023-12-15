package com.curseclient.client.utility.render.animation

import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.render.InterpolateFunction
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class SimpleAnimation(var value: Float) {
    private var lastMS: Long

    init {
        lastMS = System.currentTimeMillis()
    }

    fun setAnimation(value: Float, speed: Double) {
        var speed = speed
        val currentMS = System.currentTimeMillis()
        val delta = currentMS - lastMS
        lastMS = currentMS
        var deltaValue = 0.0
        if (speed > 28) {
            speed = 28.0
        }
        if (speed != 0.0) {
            deltaValue = abs((value - this.value).toDouble()) * 0.35f / (10.0 / speed)
        }
        this.value = AnimationUtils.animate(value, this.value, deltaValue, delta)
    }
}

object AnimationUtils {

    fun animate(target: Float, current: Float, speed: Double, delta: Long): Float {
        var current = current
        val diff = current - target
        val add = delta * (speed / 50)
        if (diff > speed) {
            if (current - add > target) {
                current -= add.toFloat()
            } else {
                current = target
            }
        } else if (diff < -speed) {
            if (current + add < target) {
                current += add.toFloat()
            } else {
                current = target
            }
        } else {
            current = target
        }
        return current
    }

    fun animate(target: Double, current: Double, speed: Double): Double {
        var current = current
        var speed = speed
        if (current == target) return current
        val larger = target > current
        if (speed < 0.0) {
            speed = 0.0
        } else if (speed > 1.0) {
            speed = 1.0
        }
        val dif = max(target, current) - min(target, current)
        var factor = dif * speed
        if (factor < 0.1) {
            factor = 0.1
        }
        if (larger) {
            current += factor
            if (current >= target) current = target
        } else if (target < current) {
            current -= factor
            if (current <= target) current = target
        }
        return current
    }

    fun animate(target: Float, current: Float, speed: Float): Float {
        var current = current
        var speed = speed
        if (current == target) return current
        val larger = target > current
        if (speed < 0.0f) {
            speed = 0.0f
        } else if (speed > 1.0f) {
            speed = 1.0f
        }
        val dif: Float = Math.max(target, current) - Math.min(target, current)
        var factor = dif * speed.toDouble()
        if (factor < 0.1) {
            factor = 0.1
        }
        if (larger) {
            current += factor.toFloat()
            if (current >= target) current = target
        } else if (target < current) {
            current -= factor.toFloat()
            if (current <= target) current = target
        }
        return current
    }

    fun changer(current: Float, add: Double, min: Float, max: Float): Double {
        var current = current.toDouble()
        current += add
        if (current > max) {
            current = max.toDouble()
        }
        if (current < min) {
            current = min.toDouble()
        }
        return current
    }

    fun changer(current: Float, add: Float, min: Int, max: Float): Float {
        var current = current
        current += add
        if (current > max) {
            current = max
        }
        if (current < min) {
            current = min.toFloat()
        }
        return current
    }

    fun changer(current: Float, add: Float, min: Float, max: Float): Float {
        var current = current
        current += add
        if (current > max) {
            current = max
        }
        if (current < min) {
            current = min
        }
        return current
    }

    fun getAnimationProgressFloat(startTime: Long, duration: Int): Float {
        return clamp((System.currentTimeMillis() - startTime) / duration.toFloat(), 0.0f, 1.0f)
    }

    fun getAnimationProgressDouble(startTime: Long, duration: Int): Double {
        return clamp((System.currentTimeMillis() - startTime) / duration.toDouble(), 0.0, 1.0)
    }

}

class AnimationFlag(private val interpolation: InterpolateFunction) {

    constructor(easing: EaseUtils, length: Float) : this({ time, prev, current ->
        easing.incOrDec(Delta.toDelta(time, length), prev, current)
    })

    var prev = 0.0f; private set
    var current = 0.0f; private set
    var time = System.currentTimeMillis(); private set

    fun forceUpdate(value: Float) {
        forceUpdate(value, value)
    }

    fun forceUpdate(prev: Float, current: Float) {
        if (prev.isNaN() || current.isNaN()) return

        this.prev = prev
        this.current = current
        time = System.currentTimeMillis()
    }

    fun update(current: Float) {
        if (!current.isNaN() && this.current != current) {
            prev = this.current
            this.current = current
            time = System.currentTimeMillis()
        }
    }

    fun getAndUpdate(current: Float): Float {
        val render = interpolation.invoke(time, prev, this.current)

        if (!current.isNaN() && current != this.current) {
            prev = render
            this.current = current
            time = System.currentTimeMillis()
        }

        return render
    }

    fun get(): Float {
        return interpolation.invoke(time, prev, current)
    }

    fun forceCurrent() {
        prev = current
        time = System.currentTimeMillis()
    }
}

