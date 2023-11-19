package com.curseclient.mixin.accessor.render;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemRenderer.class)
public interface AccessorItemRenderer {
    @Accessor("equippedProgressMainHand") float getEquippedProgressMainHand();
    @Accessor("equippedProgressMainHand") void setEquippedProgressMainHand(float value);

    @Accessor("prevEquippedProgressMainHand") float getPrevEquippedProgressMainHand();
    @Accessor("prevEquippedProgressMainHand") void setPrevEquippedProgressMainHand(float value);


    @Accessor("equippedProgressOffHand") float getEquippedProgressOffHand();
    @Accessor("equippedProgressOffHand") void setEquippedProgressOffHand(float value);

    @Accessor("prevEquippedProgressMainHand") float getPrevEquippedProgressOffHand();
    @Accessor("prevEquippedProgressMainHand") void setPrevEquippedProgressOffHand(float value);

    @Accessor("itemStackMainHand") ItemStack getItemStackMainHand();
    @Accessor("itemStackMainHand") void setItemStackMainHand(ItemStack item);

    @Accessor("itemStackOffHand") ItemStack getItemStackOffHand();
    @Accessor("itemStackOffHand") void setItemStackOffHand(ItemStack item);

    @Invoker void invokeRenderArmFirstPerson(float value1, float value2, EnumHandSide side);
    @Invoker void invokeRenderMapFirstPerson(float value1, float value2, float value3);
    @Invoker void invokeRenderMapFirstPersonSide(float value1, EnumHandSide side, float value3, ItemStack stack);
    @Invoker void invokeRenderItemSide(EntityLivingBase entitylivingbaseIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform, boolean leftHanded);
}
