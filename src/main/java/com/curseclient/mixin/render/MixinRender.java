package com.curseclient.mixin.render;

import com.curseclient.client.module.impls.client.PerformancePlus;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Render.class)
public class MixinRender<T extends Entity> {
    @Inject(method = "doRenderShadowAndFire", at = @At("HEAD"), cancellable = true)
    void onShadowAndFireRender(Entity entityIn, double x, double y, double z, float yaw, float partialTicks, CallbackInfo ci) {
        if (PerformancePlus.INSTANCE.isEnabled()) ci.cancel();
    }


}
