package com.curseclient.mixin.accessor.entity

import net.minecraft.client.entity.EntityPlayerSP
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(EntityPlayerSP::class)
interface AccessorEntityPlayerSP {
    @get:Accessor("lastReportedYaw")
    @set:Accessor("lastReportedYaw")
    var lastReportedYaw : Float

    @get:Accessor("lastReportedPitch")
    @set:Accessor("lastReportedPitch")
    var lastReportedPitch : Float
}