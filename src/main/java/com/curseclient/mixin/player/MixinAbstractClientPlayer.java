package com.curseclient.mixin.player;

import com.curseclient.CurseClient;
import com.curseclient.client.event.EventBus;
import com.curseclient.client.event.events.render.EventFovUpdate;
import com.curseclient.client.module.modules.client.Cape;
import com.curseclient.client.module.modules.visual.FovModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(AbstractClientPlayer.class)
public class MixinAbstractClientPlayer {

    @Inject(method = "getFovModifier", at = @At("HEAD"))
    private void updateFovModifierHand(CallbackInfoReturnable<Float> cir) {
        Minecraft mc = Minecraft.getMinecraft();
        float fov = mc.gameSettings.fovSetting;
        EventFovUpdate fovEvent = new EventFovUpdate((AbstractClientPlayer)(Object)this, fov);

        EventBus.INSTANCE.post(fovEvent);

        mc.gameSettings.fovSetting = fovEvent.getFovValue();
    }

    @Inject(method = "getFovModifier", at = @At("HEAD"), cancellable = true)
    private void onFovChange(CallbackInfoReturnable<Float> cir){
        ItemStack item = Minecraft.getMinecraft().player.getHeldItemMainhand();
        if(FovModifier.INSTANCE.isEnabled()){
            float mod = 1.0f;
            if(FovModifier.INSTANCE.getAllowSprint() && FovModifier.INSTANCE.getStatic() && Minecraft.getMinecraft().player.isSprinting()) mod *= 1.15f;

            if(FovModifier.INSTANCE.getAllowBow() && Minecraft.getMinecraft().player.isHandActive() && Minecraft.getMinecraft().player.isUser() && item.getItem() == Items.BOW) mod /= (float) FovModifier.INSTANCE.getZoomFactor();
            cir.setReturnValue(mod);
        }
    }

    @Inject(method = "getLocationCape", at = @At("HEAD"), cancellable = true)
    private void getLocationCapeInject(CallbackInfoReturnable<ResourceLocation> cir) {
        final Cape capeValue = (Cape) CurseClient.Companion.getModuleManager().getModuleByName("Cape");
        AbstractClientPlayer instance = (AbstractClientPlayer) (Object) this;
        if (instance != Minecraft.getMinecraft().player) return;
        assert capeValue != null;
        cir.setReturnValue(capeValue.getCapeLocation(String.valueOf(capeValue.getStyleValue())));
    }
}