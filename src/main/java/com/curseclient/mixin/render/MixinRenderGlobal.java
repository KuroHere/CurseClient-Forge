package com.curseclient.mixin.render;

import com.curseclient.client.event.EventBus;
import com.curseclient.client.event.events.render.RenderEntityEvent;
import com.curseclient.client.event.events.render.ShouldSetupTerrainEvent;
import com.curseclient.client.event.events.world.EventRenderSky;
import com.curseclient.client.module.modules.client.PerformancePlus;
import com.curseclient.client.module.modules.visual.SelectionHighlight;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

    @Inject(method = "renderSky(FI)V", at = @At("HEAD"), cancellable = true)
    private void renderSkyMixin(float partialTicks, int pass, CallbackInfo ci) {
        boolean cancelled = renderSkyHook();

        if (cancelled) {
            ci.cancel();
        }
    }

    private static boolean renderSkyHook() {
        final EventRenderSky event = new EventRenderSky();
        EventBus.INSTANCE.post(event);

        return event.getCancelled();
    }

    @Inject(method = "renderEntities", at = @At("HEAD"))
    public void renderEntitiesHead(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        RenderEntityEvent.setRenderingEntities(true);
    }

    @Inject(method = "renderEntities", at = @At("RETURN"))
    public void renderEntitiesReturn(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        RenderEntityEvent.setRenderingEntities(false);
    }

    @Inject(method = "isOutlineActive", at = @At("HEAD"), cancellable = true)
    void isOutlineActiveInject(Entity entityIn, Entity viewer, ICamera camera, CallbackInfoReturnable<Boolean> cir) {
        if (!PerformancePlus.INSTANCE.isEnabled()) return;
        cir.setReturnValue(false);
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    void onCloudRender(float partialTicks, int pass, double x, double y, double z, CallbackInfo ci) {
        if (!PerformancePlus.INSTANCE.isEnabled()) return;
        ci.cancel();
    }

    @Inject(method = "drawSelectionBox", at = @At("HEAD"), cancellable = true)
    void drawSelectionBoxInject(EntityPlayer player, RayTraceResult traceResult, int execute, float partialTicks, CallbackInfo ci) {
        if (!SelectionHighlight.INSTANCE.isEnabled()) return;
        ci.cancel();

        SelectionHighlight.draw(traceResult);
    }

    @ModifyVariable(method = "setupTerrain", at = @At(value = "HEAD"))
    private boolean onSetupTerrainPre(boolean oldValue) {
        ShouldSetupTerrainEvent event = new ShouldSetupTerrainEvent();
        EventBus.INSTANCE.post(event);
        if (event.getCancelled()) return true;
        return oldValue;
    }
}
