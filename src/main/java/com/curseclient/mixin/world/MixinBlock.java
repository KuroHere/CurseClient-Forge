package com.curseclient.mixin.world;

import com.curseclient.CurseClient;
import com.curseclient.client.event.EventBus;
import com.curseclient.client.event.events.render.CanRenderInLayerEvent;
import com.curseclient.client.module.modules.visual.WallHack;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Block.class)
public abstract class MixinBlock {
    @Shadow @Final protected Material material;

    @Inject(method = "getAmbientOcclusionLightValue", at =  @At(value = "HEAD"), cancellable = true)
    private void onGetAmbientOcclusionLightValuePre(CallbackInfoReturnable<Float> cir) {
        if (WallHack.INSTANCE.isEnabled()) {
            cir.setReturnValue(1F);
        }
    }

    @Inject(method = "getLightValue(Lnet/minecraft/block/state/IBlockState;)I", at = @At("HEAD"), cancellable = true)
    public void onGetLightValuePre(CallbackInfoReturnable<Integer> cir) {
        if (WallHack.INSTANCE.isEnabled()) {
            WallHack.INSTANCE.processGetLightValue((Block) (Object) this, cir);
        }
    }

    @Inject(method = "canRenderInLayer", at = @At(value = "RETURN"), cancellable = true, remap = false)
    @Dynamic
    public void onCanRenderInLayerPre(IBlockState state, BlockRenderLayer layer, CallbackInfoReturnable<Boolean> cir) {
        CanRenderInLayerEvent event = new CanRenderInLayerEvent((Block) (Object) this);
        if (CurseClient.Companion.getInstance() != null)
            EventBus.INSTANCE.post(event);
        if (event.getBlockRenderLayer() != null) cir.setReturnValue(event.getBlockRenderLayer() == layer);
    }
}
