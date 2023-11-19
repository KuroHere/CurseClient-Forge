package com.curseclient.client.gui.impl.particles.image

import com.curseclient.client.utility.tuples.Pair
import net.minecraft.util.ResourceLocation

class ParticleImage(particleNumber: Int, dimensions: Pair<Int, Int>) {
    val dimensions: Pair<Int, Int> = dimensions
    val location: ResourceLocation
    val particleType: ParticleType

    init {
        particleType = if (dimensions.first > 350) ParticleType.BIG else ParticleType.SMALL
        location = ResourceLocation("textures/particle/particles$particleNumber.png")
    }
}