package com.curseclient.mixin.accessor.render;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface AccessorEntityRenderer {

    @Invoker("setupCameraTransform")
    void invokeSetupCameraTransform(float partialTicks, int pass);
    @Invoker void invokeRenderHand(float partialTicks, int pass);

    @Accessor boolean getLightmapUpdateNeeded();
}
