package com.curseclient.mixin.accessor.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityPlayer.class)
public interface AccessorEntityPlayer {

    @Accessor("gameProfile")
    GameProfile hookGetGameProfile();

    @Accessor("speedInAir")
    float getSpeedInAir();

    @Accessor("speedInAir")
    void setSpeedInAir(float value);
}
