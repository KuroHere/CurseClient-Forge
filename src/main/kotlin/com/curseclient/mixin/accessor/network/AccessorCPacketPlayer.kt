package com.curseclient.mixin.accessor.network

import net.minecraft.network.play.client.CPacketPlayer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(CPacketPlayer::class)
interface AccessorCPacketPlayer {
    @get:Accessor("x")
    @set:Accessor("x")
    var x : Double

    @get:Accessor("y")
    @set:Accessor("y")
    var y : Double

    @get:Accessor("z")
    @set:Accessor("z")
    var z : Double

    @get:Accessor("yaw")
    @set:Accessor("yaw")
    var yaw : Float

    @get:Accessor("pitch")
    @set:Accessor("pitch")
    var pitch : Float

    @get:Accessor("onGround")
    @set:Accessor("onGround")
    var onGround : Boolean
}