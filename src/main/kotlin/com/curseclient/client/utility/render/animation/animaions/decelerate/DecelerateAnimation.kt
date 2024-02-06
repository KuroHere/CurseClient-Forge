package com.curseclient.client.utility.render.animation.animaions.decelerate

import com.curseclient.client.utility.render.animation.Direction

class DecelerateAnimation(ms: Int, endPoint: Double) : Animation(ms, endPoint) {

    constructor(ms: Int, endPoint: Double, direction: Direction) : this(ms, endPoint)

    override fun getEquation(x: Double): Double {
        return 1 - ((x - 1) * (x - 1))
    }
}
