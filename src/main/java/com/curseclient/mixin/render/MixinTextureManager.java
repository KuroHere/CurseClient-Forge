package com.curseclient.mixin.render;

import com.curseclient.client.module.modules.client.PerformancePlus;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureManager.class)
public class MixinTextureManager {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    void onTick(CallbackInfo ci) {
        boolean flag = PerformancePlus.shouldUpdateTextures();
        if (!flag) ci.cancel();
    }
}
