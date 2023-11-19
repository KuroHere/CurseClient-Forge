package com.curseclient.mixin.render;

import com.curseclient.client.module.modules.visual.ItemPhysics;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderEntityItem.class)
public abstract class MixinRenderEntityItem {

    @Inject(method = "transformModelCount", at = @At("HEAD"), cancellable = true)
    private void onItemAnim(EntityItem itemIn, double p_177077_2_, double p_177077_4_, double p_177077_6_, float p_177077_8_, IBakedModel p_177077_9_, CallbackInfoReturnable<Integer> cir){
        if(ItemPhysics.INSTANCE.isEnabled()){

            float f2 = p_177077_9_.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.y;
            GlStateManager.translate((float) p_177077_2_, (float) p_177077_4_ + 0.1F * f2, (float) p_177077_6_);
            GlStateManager.rotate(-90f, 1.0f, 0.0f, 0.0f);

            double size = ItemPhysics.INSTANCE.getSize();
            GlStateManager.scale(size, size, size);


            ItemStack stack = itemIn.getItem();
            int i = 1;

            if (stack.getCount() > 48)
            {
                i = 5;
            }
            else if (stack.getCount() > 32)
            {
                i = 4;
            }
            else if (stack.getCount() > 16)
            {
                i = 3;
            }
            else if (stack.getCount() > 1)
            {
                i = 2;
            }


            cir.setReturnValue(i);
        }
    }
}


