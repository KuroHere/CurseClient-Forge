package com.curseclient.mixin.player;

import com.curseclient.client.module.modules.visual.CustomModel;
import com.curseclient.client.utility.render.model.WingModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;


@Mixin(ModelPlayer.class)
public class MixinModelPlayer extends ModelBiped {

    private WingModel wingModel = new WingModel();
    @Inject(method = "render", at = @At("RETURN"))
    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale, CallbackInfo ci) {
        if (entityIn.equals(Minecraft.getMinecraft().player)) {

            wingModel.renderWing((AbstractClientPlayer) entityIn, CustomModel.INSTANCE.getScale());
        }
    }

}
