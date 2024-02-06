package com.curseclient.mixin.render;

import com.curseclient.client.event.EventBus;
import com.curseclient.client.event.events.ArmorRenderEvent;
import com.curseclient.client.module.impls.visual.GlintModifier;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerArmorBase.class)
public class MixinLayerArmorBase {

    @Inject(method = "renderArmorLayer", at=@At("HEAD"), cancellable = true)
    public void onRenderArmorLayer(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn, CallbackInfo ci) {
        ArmorRenderEvent event = ArmorRenderEvent.Companion.get(slotIn);
        EventBus.INSTANCE.post(event);
        if(event.getCancelled()) {
            ci.cancel();
        }
    }

    @Redirect(method = "renderEnchantedGlint", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V"))
    private static void onRenderEnchantedGlint(float colorRed, float colorGreen, float colorBlue, float colorAlpha) {
        if (colorBlue == 0.608F && GlintModifier.INSTANCE.isEnabled()) {
            GlStateManager.color(GlintModifier.getColor().getRed() / 255F, GlintModifier.getColor().getGreen() / 255F, GlintModifier.getColor().getBlue() / 255F, GlintModifier.getColor().getAlpha() / 255F);
        } else {
            GlStateManager.color(colorRed, colorGreen, colorBlue, colorAlpha);
        }
    }
}

