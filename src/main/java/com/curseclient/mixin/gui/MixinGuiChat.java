package com.curseclient.mixin.gui;

import com.curseclient.client.manager.managers.ModuleManager;
import com.curseclient.client.module.modules.misc.ChatMod;
import com.curseclient.client.utility.render.graphic.GLUtils;
import com.curseclient.client.utility.render.animation.SimpleAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiChat;

@Mixin(GuiChat.class)
public class MixinGuiChat {

    @Unique
    private final SimpleAnimation curseClient_1_12_2$animation = new SimpleAnimation(0.0F);

    @Inject(method = "drawScreen", at = @At("HEAD"))
    public void drawScreenPre(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {

        final ChatMod chatMod = ModuleManager.INSTANCE.getModuleByClass(ChatMod.class);
        assert chatMod != null;
        if(chatMod.isEnabled() && chatMod.getBarAnimation()) {
            curseClient_1_12_2$animation.setAnimation(30, 20);
            GLUtils.INSTANCE.startTranslate(0, 29 - (int) curseClient_1_12_2$animation.getValue());
        }
    }

    @Inject(method = "drawScreen", at = @At("TAIL"))
    public void drawScreenPost(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {

        final ChatMod chatMod = ModuleManager.INSTANCE.getModuleByClass(ChatMod.class);
        assert chatMod != null;
        if(chatMod.isEnabled() && chatMod.getBarAnimation()) {
            GLUtils.INSTANCE.stopTranslate();
        }
    }
}
