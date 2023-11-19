package com.curseclient.mixin.accessor.network;

import net.minecraft.network.play.server.SPacketSoundEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketSoundEffect.class)
public interface AccessorSPacketSoundEffect {
    @Accessor("soundPitch")
    void setPitch(float value);
}
