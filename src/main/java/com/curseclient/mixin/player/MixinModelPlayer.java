package com.curseclient.mixin.player;

import com.curseclient.client.manager.managers.ModuleManager;
import com.curseclient.client.module.modules.visual.CustomModel;
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
            final CustomModel customModel = ModuleManager.INSTANCE.getModuleByClass(CustomModel.class);
            RenderPlayer renderPlayer = Minecraft.getMinecraft().getRenderManager().getSkinMap().get("default");
            layerWings = new LayerWings(renderPlayer);
            assert customModel != null;
            if (customModel.isEnabled()) {
                if (customModel.getWingType() == CustomModel.Type.Crystal)
                    crystalWings.render((AbstractClientPlayer) entityIn, limbSwingAmount, ageInTicks, 0.2f);
                if (customModel.getWingType() == CustomModel.Type.Layer) {
                    layerWings.renderWing((AbstractClientPlayer) entityIn);
                }
                if (customModel.getOxygen())
                    oxygenMask.renderMask((AbstractClientPlayer) entityIn, 0.75);

                if (customModel.getWingType() == CustomModel.Type.Dragon)
                    wingModel.renderWing((AbstractClientPlayer) entityIn, customModel.getScale());

            }
        }
    }
}
