package com.curseclient.mixin.entity;

import com.curseclient.client.event.EventBus;
import com.curseclient.client.event.events.EntityAttackedEvent;
import com.curseclient.client.event.events.EntityDeathEvent;
import com.curseclient.client.event.events.JumpEvent;
import com.curseclient.client.event.events.JumpMotionEvent;
import com.curseclient.client.module.impls.visual.ViewModel;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static java.lang.Math.max;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase {

    @Inject(method = "attackEntityFrom", at = @At("HEAD"))
    public void hookAttackEntityFrom(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        EntityAttackedEvent event = new EntityAttackedEvent((Entity) (Object) this);
        EventBus.INSTANCE.post(event);
    }

    @Inject(method = "getArmSwingAnimationEnd", at = @At("HEAD"), cancellable = true)
    private void onGetArmSwingAnimationEnd(CallbackInfoReturnable<Integer> cir) {
        if (!ViewModel.INSTANCE.isEnabled()) return;
        cir.setReturnValue((int)(6 * max(ViewModel.getSwingSpeed(), 0.2f)));
    }

    @Inject(method = "getJumpUpwardsMotion", at = @At("HEAD"), cancellable = true)
    private void onJumpMotion(CallbackInfoReturnable<Float> cir) {
        if (!this.getClass().isInstance(Minecraft.getMinecraft().player)) return;

        JumpMotionEvent event = new JumpMotionEvent(0.42f);
        EventBus.INSTANCE.post(event);
        if (event.getCancelled()) cir.setReturnValue(0f); else cir.setReturnValue(event.getMotion());
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        if (!this.getClass().isInstance(Minecraft.getMinecraft().player)) return;

        JumpEvent event = new JumpEvent();
        EventBus.INSTANCE.post(event);
        if (event.getCancelled()) ci.cancel();
    }

    @Inject(method = "setHealth", at = @At("HEAD"))
    private void onSetHealth(float healthTo, CallbackInfo ci) {
        EntityLivingBase instance = (EntityLivingBase) (Object) this;

        float healthFrom = instance.getHealth();

        if (healthFrom <= 0.0) return;
        if (healthTo > 0.0) return;

        EventBus.INSTANCE.post(new EntityDeathEvent(instance));
    }


}
