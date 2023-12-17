package com.curseclient.mixin.accessor.entity

import net.minecraft.entity.player.EntityPlayer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(EntityPlayer::class)
interface AccessorEntityPlayer {
    @get:Accessor("speedInAir")
    @set:Accessor("speedInAir")
    var speedInAir : Float
}