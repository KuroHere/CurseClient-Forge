package com.curseclient.client.gui.impl.particles.simple

import com.curseclient.client.gui.impl.particles.simple.util.RenderUtils
import com.curseclient.client.module.modules.client.ClickGui
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.graphic.GlStateUtils
import com.curseclient.client.utility.render.shader.GradientUtil
import net.minecraft.client.Minecraft
import java.awt.Color
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate


/**
 * Particle API This Api is free2use But u have to mention me.
 *
 * @author Vitox
 * @version 3.0
 */
class ParticleGenerator(private val amount: Int) {
    private val particles: MutableList<Particle> = ArrayList<Particle>()
    private var prevWidth = 0
    private var prevHeight = 0

    fun draw(mouseX: Int, mouseY: Int) {
        if (particles.isEmpty() || prevWidth != Minecraft.getMinecraft().displayWidth || prevHeight != Minecraft.getMinecraft().displayHeight) {
            particles.clear()
            create()
        }
        prevWidth = Minecraft.getMinecraft().displayWidth
        prevHeight = Minecraft.getMinecraft().displayHeight

        val screenHeight = Minecraft.getMinecraft().displayHeight.toFloat() * 0.35f

        val particlesToRemove = mutableListOf<Particle>()

        for (particle in particles) {
            particle.fall()
            particle.interpolation()
            val range = 50
            val mouseOver = mouseX >= particle.x - range && mouseY >= particle.y - range && mouseX <= particle.x + range && mouseY <= particle.y + range

            if (particle.y <= screenHeight) {
                particlesToRemove.add(particle)
                continue
            }

            if (mouseOver) {
                particles.stream()
                    .filter(Predicate<Particle> { part: Particle ->
                        (part.x > particle.x && part.x - particle.x < range && particle.x - part.x < range
                            && (part.y > particle.y && part.y - particle.y < range
                            || particle.y > part.y && particle.y - part.y < range))
                    })
                    .forEach(Consumer<Particle> { connectable: Particle -> particle.connect(connectable.x, connectable.y) })
            }

            val c1 = if (ClickGui.colorMode == ClickGui.ColorMode.Client)
                HUD.getColor(0)
            else if (ClickGui.pulse) ColorUtils.pulseColor(ClickGui.buttonColor1, 0, 1) else ClickGui.buttonColor1

            GlStateUtils.blend(true)
            GlStateUtils.depth(false)
            GlStateUtils.texture2d(false)
            GlStateUtils.lineSmooth(true)
            GlStateUtils.depthMask(false)

            RenderUtils.drawCircle(particle.x, particle.y, particle.size, c1.rgb)

            GlStateUtils.depthMask(true)
            GlStateUtils.lineSmooth(false)
            GlStateUtils.texture2d(true)
            GlStateUtils.depth(true)
            GlStateUtils.blend(false)
        }

        particles.removeAll(particlesToRemove)
    }

    private fun create() {
        val random = Random()
        for (i in 0 until amount) particles.add(Particle(random.nextInt(Minecraft.getMinecraft().displayWidth), random.nextInt(Minecraft.getMinecraft().displayHeight)))
    }
}