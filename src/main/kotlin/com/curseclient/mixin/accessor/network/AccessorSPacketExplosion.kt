package com.curseclient.mixin.accessor.network

import net.minecraft.network.play.server.SPacketExplosion
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(SPacketExplosion::class)
interface AccessorSPacketExplosion {
    @get:Accessor("motionX")
    @set:Accessor("motionX")
    var motionX : Float

    @get:Accessor("motionY")
    @set:Accessor("motionY")
    var motionY : Float

    @get:Accessor("motionZ")
    @set:Accessor("motionZ")
    var motionZ : Float
}