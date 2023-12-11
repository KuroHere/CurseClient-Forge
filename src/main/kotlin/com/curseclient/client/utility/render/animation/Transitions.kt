package com.curseclient.client.utility.render.animation

import java.awt.Color
import kotlin.math.pow

object Transitions {
    /**
     * @param value The current value
     * @param goal  The value to transition to
     * @param speed The speed of the operation (BIGGER = SLOWER!)
     * @return The new value
     */
    @JvmOverloads
    fun transition(value: Double, goal: Double, speed: Double, skipSize: Double = 0.02): Double {
        var speedValue = speed
        speedValue = if (speedValue < 1) 1.0 else speedValue
        var diff = goal - value
        var diffCalc = diff / speedValue
        if (Math.abs(diffCalc) < skipSize) {
            diffCalc = diff
        }
        return value + diffCalc
    }

    fun transition(value: Color, goal: Color, speed: Double): Color {
        val rn = Math.floor(transition(value.red.toDouble(), goal.red.toDouble(), speed)).toInt()
        val gn = Math.floor(transition(value.green.toDouble(), goal.green.toDouble(), speed)).toInt()
        val bn = Math.floor(transition(value.blue.toDouble(), goal.blue.toDouble(), speed)).toInt()
        val an = Math.floor(transition(value.alpha.toDouble(), goal.alpha.toDouble(), speed)).toInt()
        return Color(rn, gn, bn, an)
    }

    fun easeOutBack(x: Double): Double {
        val c1 = 1.30158
        val c3 = c1 + 1
        return 1 + c3 * (x - 1).pow(3.0) + c1 * (x - 1).pow(2.0)
    }
}
