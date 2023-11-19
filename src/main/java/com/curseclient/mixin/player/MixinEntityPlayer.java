package com.curseclient.mixin.player;

import com.curseclient.client.event.EventBus;
import com.curseclient.client.event.events.PushByEntityEvent;
import com.curseclient.client.event.events.TravelEvent;
import com.curseclient.client.module.modules.movement.KeepSprint;
import com.curseclient.client.module.modules.visual.SmoothCrouch;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase {
    public MixinEntityPlayer(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void travel(float strafe, float vertical, float forward, CallbackInfo info) {
        EntityPlayer instance = (EntityPlayer) (Object) this;
        if (Minecraft.getMinecraft().player != instance) return;

        TravelEvent event = new TravelEvent();
        EventBus.INSTANCE.post(event);
        if (!event.getCancelled()) return;

        move(MoverType.SELF, motionX, motionY, motionZ);
        info.cancel();
    }

    @Inject(method = "getEyeHeight", at = @At("HEAD"), cancellable = true)
    void getEyeHeightInject(CallbackInfoReturnable<Float> cir) {
        if (!SmoothCrouch.INSTANCE.isEnabled()) return;

        EntityPlayer instance = (EntityPlayer) (Object) this;
        if (Minecraft.getMinecraft().player != instance) return;

        if (instance.isPlayerSleeping()) {
            cir.setReturnValue(0.2f);
            return;
        }

        if (instance.isElytraFlying()) {
            cir.setReturnValue(0.4f);
            return;
        }

        cir.setReturnValue(instance.eyeHeight - (SmoothCrouch.getCrouchProgress()));
    }

    @Inject(method = "applyEntityCollision", at = @At("HEAD"), cancellable = true)
    public void onPush(Entity entityIn, CallbackInfo ci){
        PushByEntityEvent event = new PushByEntityEvent();
        EventBus.INSTANCE.post(event);
        if (event.getCancelled()) ci.cancel();
    }

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At("HEAD"))
    public void onAttackPre(Entity targetEntity, CallbackInfo ci) {
        KeepSprint.onHitPre();
    }

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At("RETURN"))
    public void onAttackPost(Entity targetEntity, CallbackInfo ci) {
        KeepSprint.onHitPost();
    }
}
