package com.curseclient.mixin.world;


import com.curseclient.client.event.EventBus;
import com.curseclient.client.event.events.render.ComputeVisibilityEvent;
import com.curseclient.client.event.events.world.SetOpaqueCubeEvent;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VisGraph.class)
public abstract class MixinVisGraph {

    @Inject(method = "computeVisibility", at = @At(value = "RETURN"), cancellable = true)
    private void onComputeVisibilityPost(CallbackInfoReturnable<SetVisibility> cir) {
        ComputeVisibilityEvent event = new ComputeVisibilityEvent();
        EventBus.INSTANCE.post(event);
        if (event.getCancelled()) cir.cancel();
    }

    @Inject(method = "setOpaqueCube", at = @At("HEAD"), cancellable = true)
    public void onSetOpaqueCubePre(BlockPos pos, CallbackInfo ci) {
        SetOpaqueCubeEvent event = new SetOpaqueCubeEvent(); ///< pos is unused
        EventBus.INSTANCE.post(event);
        if (event.getCancelled()) ci.cancel();
    }

}