package com.curseclient.client.gui.impl.particles.moving

import baritone.api.utils.Helper.mc
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import java.awt.Color

/*
 I get it from Atomic client (1.17+)
 and adjust somethin to using in curse
 */
class MovingParticle(var x: Double, var y: Double, private var velocity: Vec2f, var brightness: Float, var color: Color) {

    constructor(initialPos: Vec2f, vel: Vec2f, color: Color) : this(initialPos.x.toDouble(), initialPos.y.toDouble(), vel, 0f, color)

    fun move(rest: Array<MovingParticle>) {
        val sr = ScaledResolution(mc)
        val nx = x + velocity.x
        val ny = y + velocity.y
        val w = sr.scaledWidth
        val h = sr.scaledHeight
        if (nx > w || nx < 0) {
            velocity = Vec2f(-velocity.x * 2, 0f)
        } else x = nx
        if (ny > h || ny < 0) velocity = Vec2f(0f, -velocity.y * 2)
        else y = ny
        if (nx > w) x = (w - 1).toDouble()
        if (nx < 0) x = 1.0
        if (ny > h) y = (h - 1).toDouble()
        if (ny < 0) y = 1.0
        brightness += (Math.random() - 0.5).toFloat() / 8
        brightness = if (brightness > 0.5f) 0.5f else if (brightness < 0) 0f else brightness
        val perX = x / sr.scaledWidth
        val perY = y / sr.scaledHeight
        val r = (perX * 255).toInt()
        val g = Math.abs(255 - r)
        val b = (perY * 255).toInt()
        color = Color(r, g, b)

        var velXO = 0.0
        var velYO = 0.0
        val maxDist = Math.sqrt((w + h).toDouble() * 3)
        for (movingParticle in rest.filter { it != this }) {
            val p = Vec3d(movingParticle.x, movingParticle.y, 0.0)
            val p1 = Vec3d(this.x, this.y, 0.0)
            val d = p1.distanceTo(p)
            if (d < maxDist) {
                val dInv = Math.abs(maxDist - d) / maxDist
                val xDiff = (this.x - movingParticle.x) / maxDist
                val yDiff = (this.y - movingParticle.y) / maxDist
                velXO += xDiff * dInv
                velYO += yDiff * dInv
            }
        }
        velocity = Vec2f((velocity.x + (velXO / 2)).coerceIn(-3.0, 3.0).toFloat(), (velocity.y + (velYO / 2)).coerceIn(-3.0, 3.0).toFloat())
        //velocity = Vec2f(velocity.x/1.01f,velocity.y/1.01f);
    }
}
