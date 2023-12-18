package com.curseclient.client.gui.impl.particles.image

import baritone.api.utils.Helper.mc
import com.curseclient.gui.impl.particles.image.Particle
import com.curseclient.client.utility.tuples.Pair
import com.curseclient.client.utility.tuples.mutable.MutablePair
import net.minecraft.client.gui.ScaledResolution
import java.util.*

class ParticleEngine {

    private val particleImages = listOf(
        ParticleImage(1, Pair.of(297, 301)),
        ParticleImage(2, Pair.of(303, 310)),
        ParticleImage(3, Pair.of(748, 781)),
        ParticleImage(4, Pair.of(227, 283)),
        ParticleImage(5, Pair.of(251, 302)),
        ParticleImage(6, Pair.of(253, 228)),
        ParticleImage(7, Pair.of(419, 476)),
        ParticleImage(8, Pair.of(564, 626))
    )

    private val particleTypes = MutablePair.of(0, 0)
    private val particles = mutableListOf<Particle>()
    private val toRemove = mutableListOf<Particle>()

    fun render() {
        val sr = ScaledResolution(mc)

        if (particles.size < 6) {
            particles.add(Particle(sr, getParticleImage()))
        }

        particles.sortByDescending { p ->
            val pImg = p.getParticleImage()
            pImg.dimensions.first + pImg.dimensions.second
        }

        for (particle in particles) {
            particle.setX(particle.initialX - (particle.ticksValue * 20))
            particle.setY(particle.initialY + (particle.ticksValue * (particle.ticksValue * particle.speed) / 7))

            particle.draw()

            //val particleHeight = particle.getParticleImage().dimensions.second / 2f
            val particleWidth = particle.getParticleImage().dimensions.first / 2f

            if (particle.xValue + particleWidth < 0 || particle.yValue > sr.scaledHeight || particle.xValue > sr.scaledWidth) {
                toRemove.add(particle)

                if (particle.getParticleImage().particleType == ParticleType.BIG) {
                    particleTypes.computeSecond { maxOf(0, it - 1) }
                } else {
                    particleTypes.computeFirst { maxOf(0, it - 1) }
                }
            }
            particle.setTicks(particle.ticksValue + 0.03f)
        }

        if (toRemove.isNotEmpty()) {
            particles.removeAll(toRemove)
            toRemove.clear()
        }
    }

    private fun getParticleImage(): ParticleImage {
        val particleType = getParticleType()
        val particleList = particleImages.filter { it.particleType == particleType }
        return particleList[(Random().nextFloat() * (particleList.size - 1)).toInt()]
    }

    private companion object {
        const val BIG_LIMIT = 2
        const val SMALL_LIMIT = 4
    }

    private fun getParticleType(): ParticleType {
        return if (particleTypes.first == 0 && particleTypes.second == 0) {
            particleTypes.computeSecond { it + 1 }
            ParticleType.BIG
        } else if (particleTypes.first < SMALL_LIMIT) {
            particleTypes.computeFirst { it + 1 }
            ParticleType.SMALL
        } else if (particleTypes.second < BIG_LIMIT) {
            particleTypes.computeSecond { it + 1 }
            ParticleType.BIG
        } else {
            println(particleTypes)
            throw RuntimeException("pranked gg.")
        }
    }
}