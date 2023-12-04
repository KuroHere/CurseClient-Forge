package com.curseclient.client.gui.impl.particles.flow

import baritone.api.utils.Helper.mc
import com.curseclient.client.utility.render.animation.Transitions
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.math.Vec2f
import java.awt.Color


/*
 I get it from Atomic client (1.17+)
 and adjust somethin to using in curse
 */

class FlowParticle(initialPos: Vec2f, vel: Vec2f, color: Color) {
    private val base = Vec2f(2f, 2f)
    var x: Double = initialPos.x.toDouble()
    var y: Double = initialPos.y.toDouble()
    private var velocity: Vec2f = vel
    var brightness: Float = 0f
    var color: Color = color
    private val pullStrength: Double = Math.random() * 10 + 2
    private val speedMtp: Double = (Math.random()) + 1
     val previousPos: MutableList<PosEntry> = ArrayList()

    fun move() {
        val sr = ScaledResolution(mc)
        var nx = x + velocity.x
        var ny = y + velocity.y
        val w = sr.scaledWidth
        val h = sr.scaledHeight
        if (nx > w) nx = 0.0
        if (nx < 0) nx = w.toDouble()
        if (ny > h) ny = 0.0
        if (ny < 0) ny = h.toDouble()
        x = nx
        y = ny
        if (nx > w) x = w - 1.0
        if (nx < 0) x = 1.0
        if (ny > h) y = h - 1.0
        if (ny < 0) y = 1.0

        brightness += ((Math.random() - 0.5) / 8).toFloat()
        brightness = if (brightness > 0.5f) 0.5f else if (brightness < 0) 0f else brightness

        val perX = x / sr.scaledWidth
        val perY = y / sr.scaledHeight
        val r = (perX * 255).toInt()
        val g = Math.abs(255 - r)
        val b = (perY * 255).toInt()
        color = Color(r, g, b)

        val velXO = 0.0
        val velYO = 0.0

        val newX = (velocity.x + velXO / 2).toFloat()
        val newY = (velocity.y + velYO / 2).toFloat()
        velocity = Vec2f(newX.coerceIn(-3f, 3f), newY.coerceIn(-3f, 3f))

        val newVX = Transitions.transition(velocity.x.toDouble(), base.x * speedMtp, pullStrength, 0.0).toFloat()
        val newVY = Transitions.transition(velocity.y.toDouble(), base.y * speedMtp, pullStrength, 0.0).toFloat()
        velocity = Vec2f(newVX, newVY)

        previousPos.add(PosEntry(x, y))
        if (previousPos.size > 50) previousPos.removeAt(0)
    }
}

data class PosEntry(var x: Double, var y: Double)
