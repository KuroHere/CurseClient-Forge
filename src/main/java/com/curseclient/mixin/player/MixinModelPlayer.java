package com.curseclient.mixin.player;

import com.curseclient.client.module.impls.visual.Cosmetic;
import com.curseclient.client.utility.render.model.wing.CrystalWings;
import com.curseclient.client.utility.render.model.wing.DragonWing;
import com.curseclient.client.utility.render.model.wing.LayerWings;
import com.curseclient.client.utility.render.model.diver.OxygenMask;
import net.minecraft.client.renderer.entity.RenderPlayer;
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

    private DragonWing wingModel = new DragonWing();
    private OxygenMask oxygenMask = new OxygenMask();
    private LayerWings layerWings;
    private CrystalWings crystalWings = new CrystalWings();

    @Inject(method = "render", at = @At("RETURN"))
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {
        if (entityIn.equals(Minecraft.getMinecraft().player)) {
            RenderPlayer renderPlayer = Minecraft.getMinecraft().getRenderManager().getSkinMap().get("default");
            layerWings = new LayerWings(renderPlayer);
            if (Cosmetic.INSTANCE.isEnabled()) {
                if (Cosmetic.INSTANCE.getWingType() == Cosmetic.Type.Crystal)
                    crystalWings.render((AbstractClientPlayer) entityIn, limbSwingAmount, ageInTicks, 0.2f);
                if (Cosmetic.INSTANCE.getWingType() == Cosmetic.Type.Layer) {
                    layerWings.renderWing((AbstractClientPlayer) entityIn);
                }
                if (Cosmetic.INSTANCE.getOxygen())
                    oxygenMask.renderMask((AbstractClientPlayer) entityIn, 0.75);

                if (Cosmetic.INSTANCE.getWingType() == Cosmetic.Type.Dragon)
                    wingModel.renderWing((AbstractClientPlayer) entityIn, Cosmetic.INSTANCE.getScale());

            }
        }
    }
}
