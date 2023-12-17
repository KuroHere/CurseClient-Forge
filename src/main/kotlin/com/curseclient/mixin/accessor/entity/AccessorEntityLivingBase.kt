package com.curseclient.mixin.accessor.entity

import net.minecraft.entity.EntityLivingBase
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(EntityLivingBase::class)
interface AccessorEntityLivingBase {
    @get:Accessor("jumpTicks")
    @set:Accessor("jumpTicks")
    var jumpTicks : Int
}