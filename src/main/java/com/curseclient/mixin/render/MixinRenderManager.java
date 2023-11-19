package com.curseclient.mixin.render;

import com.curseclient.client.event.EventBus;
import com.curseclient.client.event.events.render.RenderEntityEvent;
import com.curseclient.client.module.modules.visual.PerspectiveMod;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderManager.class, priority = 114514)
public abstract class MixinRenderManager {
    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    public void renderEntityPre(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci) {
        if (entity == null || !RenderEntityEvent.getRenderingEntities()) return;

        RenderEntityEvent eventAll = new RenderEntityEvent.Pre(entity);
        EventBus.INSTANCE.post(eventAll);
        if (eventAll.getCancelled()) ci.cancel();

        if (!(entity instanceof EntityLivingBase)) {
            RenderEntityEvent eventModel = new RenderEntityEvent.ModelPre(entity);
            EventBus.INSTANCE.post(eventModel);
        }
    }

    @Redirect(method = "cacheActiveRenderInfo", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/RenderManager;playerViewX:F"))
    public void getPlayerViewX(RenderManager renderManager, float value) {
        renderManager.playerViewX = PerspectiveMod.perspectiveToggled ? PerspectiveMod.cameraPitch : value;
    }

    @Redirect(method = "cacheActiveRenderInfo", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/RenderManager;playerViewY:F", ordinal = 0))
    public void getPlayerViewY(RenderManager renderManager, float value) {
        renderManager.playerViewY = PerspectiveMod.perspectiveToggled ? PerspectiveMod.cameraYaw : value;
    }


    @Inject(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/Render;doRender(Lnet/minecraft/entity/Entity;DDDFF)V", shift = At.Shift.AFTER))
    public void renderEntityPeri(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci) {
        if (entity == null || !RenderEntityEvent.getRenderingEntities()) return;

        RenderEntityEvent event = new RenderEntityEvent.Peri(entity);
        EventBus.INSTANCE.post(event);

        if (!(entity instanceof EntityLivingBase)) {
            RenderEntityEvent eventModel = new RenderEntityEvent.ModelPost(entity);
            EventBus.INSTANCE.post(eventModel);
        }
    }

    @Inject(method = "renderEntity", at = @At("RETURN"))
    public void renderEntityPost(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci) {
        if (entity == null || !RenderEntityEvent.getRenderingEntities()) return;

        RenderEntityEvent event = new RenderEntityEvent.Post(entity);
        EventBus.INSTANCE.post(event);
    }
}
