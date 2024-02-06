package com.curseclient.client.utility.render.animation.animaions.flag


fun interface InterpolateFunction {
    fun invoke(time: Long, prev: Float, current: Float): Float
}