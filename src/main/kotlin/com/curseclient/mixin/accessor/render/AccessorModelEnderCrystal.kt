package com.curseclient.mixin.accessor.render

import net.minecraft.client.model.ModelEnderCrystal
import net.minecraft.client.model.ModelRenderer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(ModelEnderCrystal::class)
interface AccessorModelEnderCrystal {
    @get:Accessor("cube")
    val cube : ModelRenderer

    @get:Accessor("glass")
    val glass : ModelRenderer
}