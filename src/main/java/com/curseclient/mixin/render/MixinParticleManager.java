package com.curseclient.mixin.render;

import com.curseclient.client.module.impls.client.PerformancePlus;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class MixinParticleManager {

    @Inject(method = "addBlockDestroyEffects", at = @At("HEAD"), cancellable = true)
    void addBlockDestroyEffectsInject(BlockPos pos, IBlockState state, CallbackInfo ci) {
        if (!PerformancePlus.INSTANCE.isEnabled()) return;
        if (!PerformancePlus.INSTANCE.getHideBlockParticles()) return;
        ci.cancel();
    }

    @Inject(method = "addBlockHitEffects(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)V", at = @At("HEAD"), cancellable = true)
    void addBlockHitEffectsInject(BlockPos pos, EnumFacing side, CallbackInfo ci) {
        if (!PerformancePlus.INSTANCE.isEnabled()) return;
        if (!PerformancePlus.INSTANCE.getHideBlockParticles()) return;
        ci.cancel();
    }
}