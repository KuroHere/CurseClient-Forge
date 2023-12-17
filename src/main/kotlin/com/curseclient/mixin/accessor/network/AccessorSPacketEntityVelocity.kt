package com.curseclient.mixin.accessor.network

import net.minecraft.network.play.server.SPacketEntityVelocity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(SPacketEntityVelocity::class)
interface AccessorSPacketEntityVelocity {
    @get:Accessor("motionX")
    @set:Accessor("motionX")
    var motionX : Int

    @get:Accessor("motionY")
    @set:Accessor("motionY")
    var motionY : Int

    @get:Accessor("motionZ")
    @set:Accessor("motionZ")
    var motionZ : Int
}