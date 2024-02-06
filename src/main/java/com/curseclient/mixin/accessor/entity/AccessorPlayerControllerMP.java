package com.curseclient.mixin.accessor.entity;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerControllerMP.class)
public interface AccessorPlayerControllerMP {
    @Accessor float getCurBlockDamageMP();
    @Accessor void setCurBlockDamageMP(float value);
    @Accessor void setBlockHitDelay(int blockHitDelay);
    @Accessor void setCurrentPlayerItem(int currentPlayerItem);
}
