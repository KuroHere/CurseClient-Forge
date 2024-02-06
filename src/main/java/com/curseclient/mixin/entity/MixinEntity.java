package com.curseclient.mixin.entity;

import com.curseclient.client.event.EventBus;
import com.curseclient.client.event.events.StepEvent;
import com.curseclient.client.module.impls.client.PerformancePlus;
import com.curseclient.client.module.impls.movement.SafeWalk;
import com.curseclient.client.module.impls.player.FreeCam;
import com.curseclient.client.module.impls.player.ViewLock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Entity.class, priority = Integer.MAX_VALUE)
public class MixinEntity {
    @Shadow private int entityId;
    private boolean modifiedSneaking = false;

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z", ordinal = 0, shift = At.Shift.BEFORE))
    public void moveInvokeIsSneakingPre(MoverType type, double x, double y, double z, CallbackInfo ci) {
        if (SafeWalk.shouldSafewalk(this.entityId)) {
            modifiedSneaking = true;
            SafeWalk.setSneaking(true);
        }
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z", ordinal = 0, shift = At.Shift.AFTER))
    public void moveInvokeIsSneakingPost(MoverType type, double x, double y, double z, CallbackInfo ci) {
        if (modifiedSneaking) {
            modifiedSneaking = false;
            SafeWalk.setSneaking(false);
        }
    }

    @Inject(method = "move", at = @At(value = "FIELD", target = "net/minecraft/entity/Entity.stepHeight:F", ordinal = 3, shift = At.Shift.BEFORE))
    private void preStep(MoverType type, double x, double y, double z, CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;

        if (player == null) return;
        if (entityId != player.getEntityId()) return;

        StepEvent.Pre event = new StepEvent.Pre(0.6f);
        EventBus.INSTANCE.post(event);

        player.stepHeight = (float) event.getHeight();
        if (event.getCancelled()) player.stepHeight = 0f;
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "net/minecraft/entity/Entity.resetPositionToBB ()V", ordinal = 1, shift = At.Shift.BEFORE))
    private void postStep(MoverType type, double x, double y, double z, CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;

        if (player == null) return;
        if (entityId != player.getEntityId()) return;

        EventBus.INSTANCE.post(new StepEvent.Post());
        player.stepHeight = 0.6f;
    }

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    public void turn(float yaw, float pitch, CallbackInfo ci) {
        Entity casted = (Entity) (Object) this;

        ViewLock.handleTurn(casted, yaw, pitch, ci);
        FreeCam.handleTurn(casted, yaw, pitch, ci);
    }

    @Inject(method = "isInRangeToRender3d", at = @At("HEAD"), cancellable = true)
    void isInRangeToRender3dInject(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;

        Double range = PerformancePlus.getEntityRenderRange(entity);
        if (range == null) return;

        if (range < 1.0) {
            cir.setReturnValue(false);
            return;
        }

        double d0 = entity.posX - x;
        double d1 = entity.posY - y;
        double d2 = entity.posZ - z;
        double distanceToCamera = d0 * d0 + d1 * d1 + d2 * d2;

        cir.setReturnValue(distanceToCamera < range * range);
    }
}