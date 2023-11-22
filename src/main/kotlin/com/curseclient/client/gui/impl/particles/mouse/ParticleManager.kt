package com.curseclient.client.gui.impl.particles.mouse

import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.animation.AnimationFlag
import com.curseclient.client.utility.render.animation.AnimationUtils
import com.curseclient.client.utility.render.animation.Easing
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.collections.ArrayList

class ParticleManager {
    private val particles: MutableList<Particle> = ArrayList()
    private var lastParticleTime: Long = System.currentTimeMillis()
    private val particleAlpha: AnimationFlag = AnimationFlag { time, prev, current ->
        AnimationUtils.animate(current, prev, 1.0, time)
    }

    fun addParticle(x: Double, y: Double) {
        particles.firstOrNull { !it.isMoving() }?.updateCoordinates(x, y)
            ?: particles.add(Particle(x, y))

        onParticlesAdded()
    }

    private fun onParticlesAdded() {
        lastParticleTime = System.currentTimeMillis()
    }


    fun renderParticles(mouseX: Int, mouseY: Int) {
        val tempParts: MutableList<Particle> = ArrayList()

        for (p in particles) {
            p.updateParticles()
            if (p.isDead) {
                tempParts.add(p)
            }
        }

        particles.removeAll(tempParts)

        particles.forEach { p ->
            p.getParticles().forEach { pos ->
                val color = HUD.getColor(1).setAlpha(particleAlpha.prev.toInt())
                drawParticle(pos.first, pos.second, 5 + color.rgb)

            }
            if (!p.isMoving()) {
                p.createSmallParticles().forEach { smallParticle ->
                    p.renderSmallParticles(smallParticle.first, smallParticle.second)
                }
            }
            particleAlpha.update(calculateAlpha(p, mouseX, mouseY))
            if (p.isDead) {
                tempParts.add(p)

            }
        }
        particles.removeIf { it.isDead }

    }

    private fun drawParticle(x: Double, y: Double, age: Int) {
        val fontRenderer = Minecraft.getMinecraft().fontRenderer

        val color = getColorForAge(age)
        val alpha = (color ushr 24 and 255) / 255.0f
        val red = (color ushr 16 and 255) / 255.0f
        val green = (color ushr 8 and 255) / 255.0f
        val blue = (color and 255) / 255.0f

        GlStateManager.color(red, green, blue, alpha)

        val text = generateRandomText()
        val xOffset = -fontRenderer.getStringWidth(text) / 2
        val yOffset = -fontRenderer.FONT_HEIGHT / 2

        fontRenderer.drawStringWithShadow(text, (x + xOffset).toFloat(), (y + yOffset).toFloat(), color)

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    private fun generateRandomText(length: Int = 1): String {
        val chars = ('A'..'Z') + ('a'..'z') + ('0'..'9') // Include other characters if needed
        return (1..length).map { chars.random() }.joinToString("")
    }

    private fun getColorForAge(age: Int): Int {
        val blueComponent = (20 - age) * 12 // Adjust the multiplier based on the desired gradient
        return (255 shl 24) or (0 shl 16) or (0 shl 8) or blueComponent
    }

    private fun calculateAlpha(p: Particle, mouseX: Int, mouseY: Int): Float {
        val distanceThreshold = 50
        val fadeDuration = 500

        val currentTime = System.currentTimeMillis()
        val particlesInCursorRange = particles.filter { particle ->
            particle.getParticles().any { pos ->
                val distance = Math.sqrt(Math.pow(pos.first - mouseX.toDouble(), 2.0) + Math.pow(pos.second - mouseY.toDouble(), 2.0))
                distance < distanceThreshold
            }
        }

        return if (particlesInCursorRange.isNotEmpty()) {
            255.0f
        } else {
            particleAlpha.forceUpdate(0.0f)
            AnimationUtils.getAnimationProgressFloat(currentTime - particleAlpha.time, fadeDuration)
        }
    }
}
