package com.curseclient.mixin.render;

import com.curseclient.client.event.EventBus;
import com.curseclient.client.event.events.render.RenderPutColorMultiplierEvent;
import net.minecraft.client.renderer.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.IntBuffer;

@Mixin(value = BufferBuilder.class)
public abstract class MixinBufferBuilder {

    @ModifyArg(method = "putColorMultiplier", at = @At(value = "INVOKE", target = "Ljava/nio/IntBuffer;put(II)Ljava/nio/IntBuffer;", remap = false), index = 1)
    private int onPutColourMultiplier(int oldAlpha) {
        RenderPutColorMultiplierEvent event = new RenderPutColorMultiplierEvent();
        EventBus.INSTANCE.post(event);
        if (event.getCancelled()) oldAlpha = ((int) event.getOpacity()) << 24 | oldAlpha & 0xFFFFFF;
        return oldAlpha;
    }

}
