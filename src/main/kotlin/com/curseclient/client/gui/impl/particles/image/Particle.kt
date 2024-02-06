package com.curseclient.client.gui.impl.particles.image

import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.utility.render.animation.animaions.simple.SimpleUtil
import com.curseclient.client.utility.render.animation.Direction
import com.curseclient.client.module.impls.client.ClickGui
import com.curseclient.client.utility.render.shader.RoundedUtil.color
import com.curseclient.client.utility.render.shader.RoundedUtil.setAlphaLimit
import com.curseclient.client.utility.math.FPSCounter.deltaTime
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.animation.animaions.decelerate.DecelerateAnimation
import com.curseclient.client.utility.render.shader.RoundedUtil.drawImage
import com.curseclient.client.utility.render.shader.gradient.GradientUtil
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.Random

class Particle(sr: ScaledResolution, private val particleImage: ParticleImage) {
    private var fadeInAnimation: DecelerateAnimation? = null
    private val rotateAnimation: DecelerateAnimation = DecelerateAnimation(10000, 1.0)

    private var xScale = 0.0f
    var xValue: Float = 0.0f
    var yValue: Float = 0.0f
    val initialX: Float
    val initialY: Float
    val speed: Float
    private val rotation: Float
    var ticksValue: Float
    private var seed: Int = 0

    init {
        val random = Random()
        val randomX = sr.scaledWidth + random.nextFloat() * (sr.scaledWidth / 2f)
        val randomY = random.nextFloat() * -sr.scaledHeight
        initialX = randomX + randomX * (seed * 0.1f)
        initialY = randomY + randomY * (seed * 0.1f)
        ticksValue = random.nextFloat() * (sr.scaledHeight / 4f)
        speed = if (particleImage.particleType == ParticleType.BIG) 1.5f else 3f
        rotation = random.nextFloat() * 360f
        seed = (seed + 1) % 8
    }

    fun draw() {
        if (fadeInAnimation == null) fadeInAnimation = DecelerateAnimation(1000, 1.0)
        rotateAnimation.theDirection =
            if (fadeInAnimation!!.finished(Direction.FORWARDS)) Direction.FORWARDS else Direction.BACKWARDS

        val rainbow = when(ClickGui.colorMode) {
            ClickGui.ColorMode.Client -> HUD.getColor(0 + 1)
            ClickGui.ColorMode.Static -> ClickGui.buttonColor1
            else -> ClickGui.buttonColor2
        }
        xScale = SimpleUtil.animate(50.0, xScale.toDouble(), 0.0125f * deltaTime).toFloat()
        val rescaled = xScale / 100f
        val imgWidth = particleImage.dimensions.first / 4f
        val imgHeight = particleImage.dimensions.second / 4f
        val particleX = xValue + imgWidth / 2f
        val particleY = yValue + imgHeight / 2f

        GlStateManager.resetColor()
        setAlphaLimit(0F)
        color(rainbow.rgb, fadeInAnimation!!.getOutput().toFloat())
        GlStateManager.enableBlend()

        GL11.glPushMatrix()
        GL11.glTranslatef(particleX, particleY, 0f)
        GL11.glScaled(rescaled.toDouble(), rescaled.toDouble(), rescaled.toDouble())
        GL11.glRotatef((rotation * rotateAnimation.getOutput()).toFloat(), 0f, 0f, 1f)
        GL11.glTranslatef(-particleX, -particleY, 0f)
        val c1 = if (ClickGui.colorMode == ClickGui.ColorMode.Client)
            HUD.getColor(0)
        else if (ClickGui.pulse) ColorUtils.pulseColor(ClickGui.buttonColor1, 0, 1) else ClickGui.buttonColor1

        val c2 = when (ClickGui.colorMode) {
            ClickGui.ColorMode.Client -> HUD.getColor(10)
            ClickGui.ColorMode.Static -> if (ClickGui.pulse) ColorUtils.pulseColor(ClickGui.buttonColor1, 0, 1) else ClickGui.buttonColor1
            else -> ClickGui.buttonColor2
        }
        GradientUtil.applyGradientHorizontal(xValue, yValue, imgWidth, imgHeight, 1F, c1, c2) {
            drawImage(particleImage.location, xValue, yValue, imgWidth, imgHeight, Color(-1))
        }
        GL11.glPopMatrix()
    }

    fun getParticleImage(): ParticleImage {
        return particleImage
    }

    fun setTicks(ticks: Float) {
        this.ticksValue = ticks
    }

    fun getTicks(): Float {
        return ticksValue
    }

    fun setX(x: Float) {
        this.xValue = x
    }

    fun setY(y: Float) {
        this.yValue = y
    }
}