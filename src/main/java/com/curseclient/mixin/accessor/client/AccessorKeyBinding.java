package com.curseclient.mixin.accessor.client;

import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface AccessorKeyBinding {

    @Accessor("pressed")
    boolean getPressed();

    @Accessor("pressed")
    void setPressed(boolean value);
}
