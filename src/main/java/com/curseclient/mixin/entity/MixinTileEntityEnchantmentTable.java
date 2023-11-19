package com.curseclient.mixin.entity;

import com.curseclient.client.module.modules.client.PerformancePlus;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityEnchantmentTable.class)
public class MixinTileEntityEnchantmentTable {
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    void onUpdate(CallbackInfo ci) {
        if (!PerformancePlus.INSTANCE.isEnabled() || !PerformancePlus.INSTANCE.getEnchantmentTable()) return;
        ci.cancel();
    }
}