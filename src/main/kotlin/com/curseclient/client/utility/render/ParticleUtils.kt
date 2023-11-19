package com.curseclient.client.utility.render

import com.curseclient.client.gui.impl.particles.simple.ParticleGenerator


object ParticleUtils {
    private val particleGenerator = ParticleGenerator(100)
    fun drawParticles(mouseX: Int, mouseY: Int) {
        particleGenerator.draw(mouseX, mouseY)
    }

}