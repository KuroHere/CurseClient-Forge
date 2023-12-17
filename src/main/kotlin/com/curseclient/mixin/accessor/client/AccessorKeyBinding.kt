package com.curseclient.mixin.accessor.client

import net.minecraft.client.settings.KeyBinding
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(KeyBinding::class)
interface AccessorKeyBinding {
    @get:Accessor("pressed")
    @set:Accessor("pressed")
    var pressed : Boolean
}