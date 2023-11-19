package com.curseclient.mixin.render;

import com.curseclient.client.module.modules.visual.ViewModel;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.*;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {
    @Inject(method = "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V", at = @At(value = "HEAD"), cancellable = true)
    private void transformSide(AbstractClientPlayer player, float partialTicks, float pitch, EnumHand hand, float swingProgress, ItemStack stack, float equippedProgress, CallbackInfo ci) {
        if (!ViewModel.INSTANCE.isEnabled()) return;

        ci.cancel();
        ViewModel.handleRender(player, partialTicks, pitch, hand, swingProgress, stack, equippedProgress);
    }
}
