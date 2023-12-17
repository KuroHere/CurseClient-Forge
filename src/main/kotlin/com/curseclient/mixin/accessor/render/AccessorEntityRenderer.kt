package com.curseclient.mixin.accessor.render

import net.minecraft.client.renderer.EntityRenderer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor
import org.spongepowered.asm.mixin.gen.Invoker

@Mixin(EntityRenderer::class)
interface AccessorEntityRenderer {
    @Invoker("setupCameraTransform")
    fun invokeSetupCameraTransform(
        partialTicks : Float,
        pass : Int
    )

    @Invoker
    fun invokeRenderHand(partialTicks : Float, pass : Int);

    @Accessor
    fun getLightmapUpdateNeeded() : Boolean
}