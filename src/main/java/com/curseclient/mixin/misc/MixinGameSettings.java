package com.curseclient.mixin.misc;

import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameSettings.class)
public class MixinGameSettings {

    @Inject(method = "setOptionValue", at = @At("HEAD"), cancellable = true)
    void onSettingChange(GameSettings.Options settingsOption, int value, CallbackInfo ci) {
        if (settingsOption != GameSettings.Options.NARRATOR) return;
        ci.cancel();

        GameSettings settings = (GameSettings) (Object) (this);

        if (settings.narrator != 0) {
            settings.narrator = 0;
            NarratorChatListener.INSTANCE.announceMode(0);
        }
    }
}
