package com.curseclient.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnresolvedMixinReference")
@Pseudo
@Mixin(targets = "Config", remap = false)
public class MixinConfig {
    @Inject(method = "isFastRender", at = @At("HEAD"), cancellable = true, remap = false)
    private static void isFastRender(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}