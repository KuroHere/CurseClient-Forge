package com.curseclient.mixin.accessor.network

import net.minecraft.network.play.server.SPacketPlayerPosLook
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(SPacketPlayerPosLook::class)
interface AccessorSPacketPosLook {
    @get:Accessor("yaw")
    @set:Accessor("yaw")
    var yaw : Float

    @get:Accessor("pitch")
    @set:Accessor("pitch")
    var pitch : Float
}