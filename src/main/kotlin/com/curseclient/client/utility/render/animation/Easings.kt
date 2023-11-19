package com.curseclient.client.utility.render.animation

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


object Easings {
    val easings = arrayOf("none", "cubic", "quint", "quad", "quart", "expo", "sine", "circ")
    fun toOutEasing(easing: String?, value: Double): Double {
        return when (easing) {
            "cubic" -> cubicOut(value)
            "quint" -> quintOut(value)
            "quad" -> quadOut(value)
            "quart" -> quartOut(value)
            "expo" -> expoOut(value)
            "sine" -> sineOut(value)
            "circ" -> circOut(value)
            else -> value
        }
    }

    fun toInEasing(easing: String?, value: Double): Double {
        return when (easing) {
            "cubic" -> cubicIn(value)
            "quint" -> quintIn(value)
            "quad" -> quadIn(value)
            "quart" -> quartIn(value)
            "expo" -> expoIn(value)
            "sine" -> sineIn(value)
            "circ" -> circIn(value)
            else -> value
        }
    }

    //cubic
    fun cubicIn(value: Double): Double {
        return value * value * value
    }

    fun cubicOut(value: Double): Double {
        return 1 - (1 - value).pow(3.0)
    }

    fun cubicInOut(value: Double): Double {
        return if (value < 0.5) 4 * value * value * value else 1 - (-2 * value + 2).pow(3.0) / 2
    }

    //quint
    fun quintIn(value: Double): Double {
        return value * value * value * value * value
    }

    fun quintOut(value: Double): Double {
        return 1 - (1 - value).pow(5.0)
    }

    fun quintInOut(value: Double): Double {
        return if (value < 0.5) 16 * value * value * value * value * value else 1 - (-2 * value + 2).pow(5.0) / 2
    }

    //quad
    fun quadIn(value: Double): Double {
        return value * value
    }

    fun quadOut(value: Double): Double {
        return 1 - (1 - value) * (1 - value)
    }

    fun quadInOut(value: Double): Double {
        return if (value < 0.5) 2 * value * value else 1 - (-2 * value + 2).pow(2.0) / 2
    }

    //Quart
    fun quartIn(value: Double): Double {
        return value * value * value * value
    }

    fun quartOut(value: Double): Double {
        return 1 - (1 - value).pow(4.0)
    }

    fun quartInOut(value: Double): Double {
        return if (value < 0.5) 8 * value * value * value * value else 1 - (-2 * value + 2).pow(4.0) / 2
    }

    //expo
    fun expoIn(value: Double): Double {
        return if (value == 0.0) 0.0 else 2.0.pow(10 * value - 10)
    }

    fun expoOut(value: Double): Double {
        return if (value == 1.0) 1.0 else 1 - 2.0.pow(-10 * value)
    }

    fun expoInOut(value: Double): Double {
        return if (value == 0.0) 0.0 else if (value == 1.0) 1.0 else if (value < 0.5) 2.0.pow(20 * value - 10) / 2 else (2 - 2.0.pow(-20 * value + 10)) / 2
    }

    //sine
    fun sineIn(value: Double): Double {
        return 1 - cos(value * Math.PI / 2)
    }

    fun sineOut(value: Double): Double {
        return sin(value * Math.PI / 2)
    }

    fun sineInOut(value: Double): Double {
        return -(cos(Math.PI * value) - 1) / 2
    }

    //circ
    fun circIn(value: Double): Double {
        return 1 - sqrt(1 - value.pow(2.0))
    }

    fun circOut(value: Double): Double {
        return sqrt(1 - (value - 1).pow(2.0))
    }

    fun circInOut(value: Double): Double {
        return if (value < 0.5) (1 - sqrt(1 - (2 * value).pow(2.0))) / 2 else (sqrt(1 - (-2 * value + 2).pow(2.0)) + 1) / 2
    }
}