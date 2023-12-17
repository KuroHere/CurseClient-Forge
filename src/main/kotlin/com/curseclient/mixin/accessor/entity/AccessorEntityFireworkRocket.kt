package com.curseclient.mixin.accessor.entity

import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityFireworkRocket
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(EntityFireworkRocket::class)
interface AccessorEntityFireworkRocket {
    @get:Accessor("boostedEntity")
    val boostedEntity : EntityLivingBase
}