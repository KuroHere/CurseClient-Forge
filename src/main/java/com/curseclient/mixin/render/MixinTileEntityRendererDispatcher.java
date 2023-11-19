package com.curseclient.mixin.render;

import com.curseclient.client.module.modules.client.PerformancePlus;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TileEntityRendererDispatcher.class)
public class MixinTileEntityRendererDispatcher {
    @Shadow public static TileEntityRendererDispatcher instance;

    @Redirect(method = "render(Lnet/minecraft/tileentity/TileEntity;FI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;getMaxRenderDistanceSquared()D"))
    double getRenderDistance(TileEntity instance) {
        Double distance = PerformancePlus.getTileEntityRenderRange(instance);

        if (distance == null) {
            return instance.getMaxRenderDistanceSquared();
        }

        return distance * distance;
    }
}
