package com.curseclient.client.utility.render.animation.ease

import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.render.graphic.cubic
import com.curseclient.client.utility.render.graphic.quint
import com.curseclient.client.utility.render.graphic.sq
import kotlin.math.*

object EaseUtils {
    @JvmStatic
    fun easeInSine(x: Double) = 1 - cos((x * PI) / 2)

    @JvmStatic
    fun easeOutSine(x: Double) = sin((x * PI) / 2)

    @JvmStatic
    fun easeInOutSine(x: Double) = -(cos(PI * x) - 1) / 2

    @JvmStatic
    fun easeInQuad(x: Double) = x * x


    @JvmStatic
    fun easeOutQuad(x: Double) = 1 - (1 - x) * (1 - x)

    @JvmStatic
    fun easeInOutQuad(x: Double) = if(x < 0.5){2 * x * x}else{1 - (-2 * x + 2).pow(2) / 2}

    @JvmStatic
    fun easeInCubic(x: Double) = x * x * x

    @JvmStatic
    fun easeOutCubic(x: Double) = 1 - (1 - x).pow(3)

    @JvmStatic
    fun easeInOutCubic(x: Double) = if(x < 0.5){4 * x * x * x}else{1 - (-2 * x + 2).pow(3) / 2}

    @JvmStatic
    fun easeInQuart(x: Double) = x * x * x * x

    @JvmStatic
    fun easeOutQuart(x: Double) = 1 - (1 - x).pow(4)

    @JvmStatic
    fun easeInOutQuart(x: Double) = if(x < 0.5){8 * x * x * x * x}else{1 - (-2 * x + 2).pow(4) / 2}

    @JvmStatic
    fun easeInQuint(x: Double) = x * x * x * x * x

    @JvmStatic
    fun easeOutQuint(x: Double) = 1 - (1 - x).pow(5)

    @JvmStatic
    fun easeInOutQuint(x: Double) = if(x < 0.5){16 * x * x * x * x * x}else{1 - (-2 * x + 2).pow(5) / 2}

    @JvmStatic
    fun easeInExpo(x: Double) = if(x == 0.0){0.0}else{2.0.pow(10 * x - 10)}

    @JvmStatic
    fun easeOutExpo(x: Double) = if(x == 1.0){1.0}else{1 - 2.0.pow(-10 * x)}

    @JvmStatic
    fun easeInOutExpo(x: Double) = if(x == 0.0){0.0}
        else{if(x == 1.0){1.0}
        else{if(x < 0.5){2.0.pow(20 * x - 10) / 2}
        else{(2 - 2.0.pow(-20 * x + 10)) / 2}}}

    @JvmStatic
    fun easeInCirc(x: Double) = 1 - sqrt(1 - x.pow(2))

    @JvmStatic
    fun easeOutCirc(x: Double) = sqrt(1 - (x - 1).pow(2))

    @JvmStatic
    fun easeInOutCirc(x: Double) = if(x < 0.5){(1 - sqrt(1 - (2 * x).pow(2))) / 2}
        else{(sqrt(1 - (-2 * x + 2).pow(2)) + 1) / 2}

    @JvmStatic
    fun easeInBack(x: Double): Double {
        val c1 = 1.70158
        val c3 = c1 + 1

        return c3 * x * x * x - c1 * x * x
    }

    @JvmStatic
    fun easeOutBack(x: Double): Double {
        val c1 = 1.70158
        val c3 = c1 + 1

        return 1 + c3 * (x - 1).pow(3) + c1 * (x - 1).pow(2)
    }

    @JvmStatic
    fun easeInOutBack(x: Double): Double {
        val c1 = 1.70158
        val c2 = c1 * 1.525

        return if(x < 0.5){((2 * x).pow(2) * ((c2 + 1) * 2 * x - c2)) / 2}
        else{((2 * x - 2).pow(2) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2}
    }

    @JvmStatic
    fun easeInElastic(x: Double): Double {
        val c4 = (2 * Math.PI) / 3

        return if(x == 0.0){0.0}
        else{if(x == 1.0){1.0}
        else{(-2.0).pow(10 * x - 10) * sin((x * 10 - 10.75) * c4)}}
    }

    @JvmStatic
    fun easeOutElastic(x: Double): Double {
        val c4 = (2 * Math.PI) / 3

        return if(x == 0.0){0.0}
        else{if(x == 1.0){1.0}
        else{2.0.pow(-10 * x) * sin((x * 10 - 0.75) * c4) + 1}}
    }

    @JvmStatic
    fun easeInOutElastic(x: Double): Double {
        val c5 = (2 * Math.PI) / 4.5

        return if(x == 0.0){0.0}
        else{if(x == 1.0){1.0}
        else{if(x < 0.5){-(2.0.pow(20 * x - 10) * sin((20 * x - 11.125) * c5)) / 2}
        else{(2.0.pow(-20 * x + 10) * sin((20 * x - 11.125) * c5)) / 2 + 1}}}
    }

    @JvmStatic
    fun easeInBounce(x: Double) = 1 - easeOutBounce(1 - x)

    @JvmStatic
    fun easeOutBounce(animeX: Double): Double {
        var x=animeX
        val n1 = 7.5625
        val d1 = 2.75

        if (x < 1 / d1) {
            return n1 * x * x
        } else if (x < 2 / d1) {
            x -= 1.5
            return n1 * (x / d1) * x + 0.75
        } else if (x < 2.5 / d1) {
            x -= 2.25
            return n1 * (x / d1) * x + 0.9375
        } else {
            x -= 2.625
            return n1 * (x / d1) * x + 0.984375
        }
    }

    @JvmStatic
    fun easeInOutBounce(x: Double) = if(x < 0.5){(1 - easeOutBounce(1 - 2 * x)) / 2}
        else{(1 + easeOutBounce(2 * x - 1)) / 2}

    @JvmStatic
    fun incOrDecOpposite(x: Float, min: Float, max: Float): Float {
        val delta = when {
            max == min -> return min
            max > min -> inc(x)
            else -> inc(x)
        }
        return MathUtils.lerp(min, max, delta)
    }

    @JvmStatic
    fun incOrDec(x: Float, min: Float, max: Float): Float {
        return MathUtils.lerp(min, max, inc(x))
    }

    @Suppress("NAME_SHADOWING")
    fun inc(x: Float, min: Float, max: Float): Float {
        var min = min
        var max = max

        if (max == min) {
            return 0.0f
        } else if (max < min) {
            val oldMax = max
            max = min
            min = oldMax
        }

        if (x <= 0.0f) {
            return min
        } else if (x >= 1.0f) {
            return max
        }

        return MathUtils.lerp(min, max, inc0(x))
    }

    @JvmStatic
    fun inc(x: Float, max: Float): Float {
        if (max == 0.0f) {
            return 0.0f
        }

        if (x <= 0.0f) {
            return 0.0f
        } else if (x >= 1.0f) {
            return max
        }

        return inc0(x) * max
    }

    @JvmStatic
    fun inc(x: Float): Float {
        if (x <= 0.0f) {
            return 0.0f
        } else if (x >= 1.0f) {
            return 1.0f
        }

        return inc0(x)
    }

    /**
     * The basic function for easing.
     *
     * @param t the time (either frames or in seconds/milliseconds)
     * @param b the beginning value
     * @param c the value changed
     * @param d the duration time
     * @return the eased value
     */
    @JvmStatic
    fun ease(t: Float, b: Float, c: Float, d: Float) : Float{
        return ease(t, b, c, d)
    }


    @Suppress("NAME_SHADOWING")
    fun dec(x: Float, min: Float, max: Float): Float {
        var min = min
        var max = max

        if (max == min) {
            return 0.0f
        } else if (max < min) {
            val oldMax = max
            max = min
            min = oldMax
        }

        if (x <= 0.0f) {
            return max
        } else if (x >= 1.0f) {
            return min
        }

        return MathUtils.lerp(min, max, dec0(x))
    }

    @JvmStatic
    fun dec(x: Float, max: Float): Float {
        if (max == 0.0f) {
            return 0.0f
        }

        if (x <= 0.0f) {
            return max
        } else if (x >= 1.0f) {
            return 0.0f
        }

        return dec0(x) * max
    }

    @JvmStatic
    fun dec(x: Float): Float {
        if (x <= 0.0f) {
            return 1.0f
        } else if (x >= 1.0f) {
            return 0.0f
        }

        return dec0(x)
    }

    @JvmStatic
    private fun inc0(x: Float) = x

    @JvmStatic
    private fun dec0(x: Float) = 1.0f - inc0(x)

    enum class EaseType{
        Linear,
        InSine, OutSine, InOutSine,
        InQuad, OutQuad, InOutQuad,
        InCubic, OutCubic, InOutCubic,
        InQuint, OutQuint, InOutQuint,
        InExpo, OutExpo, InOutExpo,
        InCirc, OutCirc, InOutCirc,
        InBack, OutBack, InOutBack,
    }

    fun getEase(value: Double, type: EaseType, clamp: Boolean = true): Double {
        val x = if (clamp) clamp(value, 0.0, 1.0) else value
        return when (type) {
            EaseType.Linear -> x
            EaseType.InSine -> 1 - cos((x * PI) / 2)
            EaseType.OutSine -> sin((x * PI) / 2)
            EaseType.InOutSine -> -(cos(PI * x) - 1) / 2
            EaseType.InQuad -> x * x
            EaseType.OutQuad -> 1 - (1 - x) * (1 - x)
            EaseType.InOutQuad -> if (x < 0.5) 2 * x * x else 1 - (-2 * x + 2).pow(2) / 2
            EaseType.InCubic -> x * x * x
            EaseType.OutCubic -> 1 - (1 - x).pow(3)
            EaseType.InOutCubic -> if (x < 0.5) 4 * x * x * x else 1 - (-2 * x + 2).pow(3) / 2
            EaseType.InQuint -> x.quint
            EaseType.OutQuint -> 1.0 - (1.0 - x).quint
            EaseType.InOutQuint -> if (x < 0.5) 16.0 * x.quint else 1.0 - (-2 * x + 2).quint / 2.0
            EaseType.InExpo -> if (x == 0.0) 0.0 else 2.0.pow(10.0 * x - 10.0)
            EaseType.OutExpo -> if (x == 1.0) 1.0 else 1.0 - 2.0.pow(-10.0 * x)
            EaseType.InOutExpo -> when {
                x == 0.0 -> 0.0
                x == 1.0 -> 1.0
                x < 0.5 -> 2.0.pow(20.0 * x - 10.0) / 2.0
                else -> (2.0 - 2.0.pow(-20.0 * x + 10.0)) / 2.0
            }
            EaseType.InCirc -> 1.0 - sqrt(1.0 - x.sq)
            EaseType.OutCirc -> sqrt(1.0 - (x - 1.0).sq)
            EaseType.InOutCirc -> if (x < 0.5f) (1.0 - sqrt(1.0 - (2.0 * x).sq)) / 2.0f
            else (sqrt(1.0 - (-2.0 * x + 2.0).sq) + 1.0) / 2.0f
            EaseType.InBack -> 2.70158f * x.cubic - 1.70158f * x.sq
            EaseType.OutBack -> 1.0f + 2.70158f * (x - 1.0f).cubic + 1.70158f * (x - 1.0f).sq
            EaseType.InOutBack -> if (x < 0.5f) (2.0f * x).sq * ((2.5949094f + 1.0f) * 2.0f * x - 2.5949094f) / 2.0f
            else ((2.0f * x - 2.0f).sq * ((2.5949094f + 1.0f) * (x * 2.0f - 2.0f) + 2.5949094f) + 2.0f) / 2.0f
        }
    }

    fun Double.ease(type: NewEaseType) = type.getValue(this)
    fun Double.ease(easeType: EaseType) = getEase(this, easeType, false)
}

object Delta{
    @JvmStatic
    fun toDelta(start: Long, length: Int) = toDelta(start, length.toFloat())


    @JvmStatic
    fun toDelta(start: Long, length: Long) = toDelta(start, length.toFloat())

    @JvmStatic
    fun toDelta(start: Long, length: Double) = toDelta(start, length.toFloat())


    @JvmStatic
    fun toDelta(start: Long, length: Float) = (toDelta(start).toFloat() / length).coerceIn(0.0f, 2.0f)


    @JvmStatic
    fun toDelta(start: Long) = System.currentTimeMillis() - start
}

@Suppress("UNUSED")
enum class NewEaseType(val getValue: (valueIn: Double) -> Double) {
    OutBack({
        val c1 = 1.70158
        val c3 = 2.70158
        (1.0 + c3 * (it - 1.0).pow(3.0) + c1 * (it - 1.0).pow(2.0))
    }),

    InOutBack( {
        val c1 = 1.70158
        val c2 = c1 * 1.525

        if(it < 0.5){((2 * it).pow(2) * ((c2 + 1) * 2 * it - c2)) / 2}
        else{((2 * it - 2).pow(2) * ((c2 + 1) * (it * 2 - 2) + c2) + 2) / 2}
    })
}