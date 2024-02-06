package com.curseclient.mixin.render;

import com.curseclient.client.module.impls.visual.ItemPhysics;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderEntityItem.class)
public abstract class MixinRenderEntityItem extends Render<EntityItem> {

    protected MixinRenderEntityItem(final RenderManager p_i46179_1_) {
        super(p_i46179_1_);
    }

    @Shadow
    protected abstract int func_177078_a(final ItemStack p0);


    @Inject(method = "transformModelCount", at = @At("HEAD"), cancellable = true)
    private void onItemAnim(EntityItem itemIn, double p_177077_2_, double p_177077_4_, double p_177077_6_, float p_177077_8_, IBakedModel p_177077_9_, CallbackInfoReturnable<Integer> cir) {
        if (ItemPhysics.INSTANCE.isEnabled()) {
            ItemStack stack = itemIn.getItem();

            boolean flag = p_177077_9_.isGui3d();
            int i = this.func_177078_a(stack);
            float f1 = MathHelper.sin(((float) itemIn.getAge() + p_177077_8_) / 10.0F + itemIn.hoverStart) * 0.1F + 0.1F;
            if (ItemPhysics.INSTANCE.isEnabled()) {
                f1 = 0.0f;
            }
            float f2 = p_177077_9_.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.y;
            GlStateManager.translate((float) p_177077_2_, (float) p_177077_4_ + f1 + 0.25F * f2, (float) p_177077_6_);

            if (flag || this.renderManager.options != null) {
                float f3 = (((float) itemIn.getAge() + p_177077_8_) / 20.0F + itemIn.hoverStart) * (180F / (float) Math.PI);
                if (ItemPhysics.INSTANCE.isEnabled()) {
                    if (itemIn.onGround) {
                        GL11.glRotatef(itemIn.rotationYaw, 0.0f, 1.0f, 0.0f);
                        GL11.glRotatef(itemIn.rotationPitch + 90.0f, 1.0f, 0.0f, 0.0f);
                    } else {
                        for (int a = 0; a < 10; ++a) {
                            GL11.glRotatef(f3, (float) ItemPhysics.INSTANCE.getWeight(), (float) ItemPhysics.INSTANCE.getWeight(), 0.0f);
                        }
                    }
                } else {
                    GlStateManager.rotate(f3, 0.0F, 1.0F, 0.0F);
                }
            }

            if (!flag) {
                float f6 = -0.0F * (float) (i - 1) * 0.5F;
                float f4 = -0.0F * (float) (i - 1) * 0.5F;
                float f5 = -0.046875F * (float) (i - 1) * 0.5F;
                GlStateManager.translate(f6, f4, f5);
            }
            double size = ItemPhysics.INSTANCE.getSize();
            GlStateManager.scale(size, size, size);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (stack.getCount() > 48) {
                i = 5;
            } else if (stack.getCount() > 32) {
                i = 4;
            } else if (stack.getCount() > 16) {
                i = 3;
            } else if (stack.getCount() > 1) {
                i = 2;
            }

            cir.setReturnValue(i);
        }
    }
}