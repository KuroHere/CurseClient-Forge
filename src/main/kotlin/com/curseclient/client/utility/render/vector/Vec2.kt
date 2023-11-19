package com.curseclient.client.utility.render.vector

import net.minecraft.util.math.Vec2f

class Vec2i(val x: Int, val y: Int)
class Vec2d(val x: Double, val y: Double) {
    constructor(x: Int, y: Int) : this(x.toDouble(), y.toDouble())

    operator fun div(vec2d: Vec2d) = div(vec2d.x, vec2d.y)

    operator fun div(divider: Double) = div(divider, divider)

    fun div(x: Double, y: Double) = Vec2d(this.x / x, this.y / y)

    operator fun times(vec2d: Vec2d) = times(vec2d.x, vec2d.y)

    operator fun times(multiplier: Double) = times(multiplier, multiplier)

    fun times(x: Double, y: Double) = Vec2d(this.x * x, this.y * y)

    operator fun minus(vec2d: Vec2d) = minus(vec2d.x, vec2d.y)

    operator fun minus(sub: Double) = minus(sub, sub)

    fun minus(x: Double, y: Double) = plus(-x, -y)

    operator fun plus(vec2d: Vec2d) = plus(vec2d.x, vec2d.y)

    operator fun plus(add: Double) = plus(add, add)

    fun plus(x: Double, y: Double) = Vec2d(this.x + x, this.y + y)

    fun toVec2f() = Vec2f(x.toFloat(), y.toFloat())

    override fun toString(): String {
        return "Vec2d[${this.x}, ${this.y}]"
    }

    companion object {
        val ZERO: Vec2d = Vec2d(0.0, 0.0)
    }
}
