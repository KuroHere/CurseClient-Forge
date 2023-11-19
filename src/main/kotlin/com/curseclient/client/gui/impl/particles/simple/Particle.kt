package com.curseclient.client.gui.impl.particles.simple

import com.curseclient.client.gui.impl.particles.simple.util.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import java.util.*


/**
 * Particle API
 * This Api is free2use
 * But u have to mention me.
 *
 * @author Vitox
 * @version 3.0
 */
internal class Particle(x: Int, y: Int) {
    var x: Float
    var y: Float
    val size: Float
    private val ySpeed = Random().nextInt(5).toFloat()
    private val xSpeed = Random().nextInt(5).toFloat()
    var height = 0
    var width = 0

    init {
        this.x = x.toFloat()
        this.y = y.toFloat()
        size = genRandom()
    }

    private fun lint1(f: Float): Float {
        return 1.02.toFloat() * (1.0f - f) + 1.0.toFloat() * f
    }

    private fun lint2(f: Float): Float {
        return 1.02.toFloat() + f * (1.0.toFloat() - 1.02.toFloat())
    }

    fun connect(x: Float, y: Float) {
        RenderUtils.connectPoints(this.x, this.y, x, y)
    }

    fun setX(x: Int) {
        this.x = x.toFloat()
    }

    fun setY(y: Int) {
        this.y = y.toFloat()
    }

    fun interpolation() {
        for (n in 0..64) {
            val f = n / 64.0f
            val p1 = lint1(f)
            val p2 = lint2(f)
            if (p1 != p2) {
                y -= f
                x -= f
            }
        }
    }

    fun fall() {
        val mc = Minecraft.getMinecraft()
        val scaledResolution = ScaledResolution(mc)
        y = y + ySpeed
        x = x + xSpeed
        if (y > mc.displayHeight) y = 1f
        if (x > mc.displayWidth) x = 1f
        if (x < 1) x = scaledResolution.scaledWidth.toFloat()
        if (y < 1) y = scaledResolution.scaledHeight.toFloat()
    }

    fun fall2() {
        val mc = Minecraft.getMinecraft()
        val scaledResolution = ScaledResolution(mc)
        y = y + ySpeed
        x = x + xSpeed
        if (x > mc.displayWidth) x = 1f
        y -= 1f
        if (x < 1) x = scaledResolution.scaledWidth.toFloat()
        y -= 1f
    }

    private fun genRandom(): Float {
        return (0.3f + Math.random() * (0.6f - 0.3f + 1.0f)).toFloat()
    }
}