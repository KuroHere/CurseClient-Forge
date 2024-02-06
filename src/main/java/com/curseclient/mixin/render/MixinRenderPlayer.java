package com.curseclient.mixin.render;

import com.curseclient.client.event.EventBus;
import com.curseclient.client.event.events.RenderRotationsEvent;
import com.curseclient.client.module.impls.player.FreeCam;
import com.curseclient.client.module.impls.visual.PlayerModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static baritone.api.utils.Helper.mc;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer extends RenderLivingBase<AbstractClientPlayer> {
    public MixinRenderPlayer(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
    }
    private float renderPitch;
    private float renderYaw;
    private float renderHeadYaw;
    private float prevRenderHeadYaw;
    private float prevRenderPitch;
    private float prevRenderYawOffset;
    private float prevPrevRenderYawOffset;

    @Inject(method = "doRender", at = @At("HEAD"))
    private void doRenderPre(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (mc.player.equals(entity)) {
            prevRenderHeadYaw = entity.prevRotationYawHead;
            prevRenderPitch = entity.prevRotationPitch;
            renderPitch = entity.rotationPitch;
            renderYaw = entity.rotationYaw;
            renderHeadYaw = entity.rotationYawHead;
            prevPrevRenderYawOffset = entity.prevRenderYawOffset;
            prevRenderYawOffset = entity.renderYawOffset;

            RenderRotationsEvent renderRotationsEvent = new RenderRotationsEvent();
            EventBus.INSTANCE.post(renderRotationsEvent);

            if (renderRotationsEvent.isCanceled()) {

                entity.rotationYaw = renderRotationsEvent.getYaw();
                entity.rotationYawHead = renderRotationsEvent.getYaw();
                entity.prevRotationYawHead = renderRotationsEvent.getYaw();
                entity.prevRenderYawOffset = renderRotationsEvent.getYaw();
                entity.renderYawOffset = renderRotationsEvent.getYaw();
                entity.rotationPitch = renderRotationsEvent.getPitch();
                entity.prevRotationPitch = renderRotationsEvent.getPitch();
            }
        }
    }

    @Inject(method = "doRender*", at = @At("RETURN"))
    private void doRenderPost(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (mc.player.equals(entity)) {
            entity.rotationPitch = renderPitch;
            entity.rotationYaw = renderYaw;
            entity.rotationYawHead = renderHeadYaw;
            entity.prevRotationYawHead = prevRenderHeadYaw;
            entity.prevRotationPitch = prevRenderPitch;
            entity.renderYawOffset = prevRenderYawOffset;
            entity.prevRenderYawOffset = prevPrevRenderYawOffset;
        }
    }

    @Inject(method={"renderLivingAt"}, at={@At(value="HEAD")})
    protected void renderLivingAt(AbstractClientPlayer entityLivingBaseIn, double x, double y, double z, CallbackInfo callbackInfo) {
        if (PlayerModel.INSTANCE.isEnabled() && PlayerModel.INSTANCE.getCustomSize() && entityLivingBaseIn.equals(Minecraft.getMinecraft().player)) {
            GlStateManager.scale(PlayerModel.INSTANCE.getSize(), PlayerModel.INSTANCE.getSize(), PlayerModel.INSTANCE.getSize());
        }
    }

    @Inject(method = "doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderViewEntity:Lnet/minecraft/entity/Entity;"))
    public void doRenderGetRenderViewEntity(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (FreeCam.INSTANCE.isEnabled() && Minecraft.getMinecraft().getRenderViewEntity() != entity) {
            double renderY = y;

            if (entity.isSneaking()) {
                renderY = y - 0.125D;
            }

            this.setModelVisibilities(entity);
            GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
            super.doRender(entity, x, renderY, z, entityYaw, partialTicks);
            GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
        }
    }

    @Shadow
    protected abstract void setModelVisibilities(AbstractClientPlayer clientPlayer);
}