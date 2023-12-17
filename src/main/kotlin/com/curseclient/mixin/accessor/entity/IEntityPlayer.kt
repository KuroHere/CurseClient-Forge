package com.curseclient.mixin.accessor.entity

import com.mojang.authlib.GameProfile
import net.minecraft.entity.player.EntityPlayer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(EntityPlayer::class)
interface IEntityPlayer {
    @Accessor("gameProfile")
    fun hookGetGameProfile() : GameProfile
}