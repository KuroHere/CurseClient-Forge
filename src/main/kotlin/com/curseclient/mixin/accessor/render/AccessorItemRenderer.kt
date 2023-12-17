package com.curseclient.mixin.accessor.render

import net.minecraft.client.renderer.ItemRenderer
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHandSide
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor
import org.spongepowered.asm.mixin.gen.Invoker

@Mixin(ItemRenderer::class)
interface AccessorItemRenderer {
    @get:Accessor("equippedProgressMainHand")
    @set:Accessor("equippedProgressMainHand")
    var equippedProgressMainHand : Float

    @get:Accessor("prevEquippedProgressMainHand")
    @set:Accessor("prevEquippedProgressMainHand")
    var prevEquippedProgressMainHand : Float

    @get:Accessor("equippedProgressOffHand")
    @set:Accessor("equippedProgressOffHand")
    var equippedProgressOffHand : Float

    @get:Accessor("prevEquippedProgressOffHand")
    @set:Accessor("prevEquippedProgressOffHand")
    var prevEquippedProgressOffHand : Float

    @get:Accessor("itemStackMainHand")
    @set:Accessor("itemStackMainHand")
    var itemStackMainHand : ItemStack

    @get:Accessor("itemStackOffHand")
    @set:Accessor("itemStackOffHand")
    var itemStackOffHand : ItemStack

    @Invoker fun invokeRenderArmFirstPerson(value1: Float, value2: Float, side: EnumHandSide?)
    @Invoker fun invokeRenderMapFirstPerson(value1: Float, value2: Float, value3: Float)
    @Invoker fun invokeRenderMapFirstPersonSide(value1: Float, side: EnumHandSide?, value3: Float, stack: ItemStack?)
    @Invoker fun invokeRenderItemSide(entitylivingbaseIn: EntityLivingBase?, heldStack: ItemStack?, transform: TransformType?, leftHanded: Boolean)
}