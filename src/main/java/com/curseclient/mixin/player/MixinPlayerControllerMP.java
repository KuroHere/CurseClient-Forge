package com.curseclient.mixin.player;

import com.curseclient.client.event.EventBus;
import com.curseclient.client.event.events.AttackEvent;
import com.curseclient.client.event.events.block.BlockResetEvent;
import com.curseclient.client.manager.managers.HotbarManager;
import com.curseclient.client.module.impls.player.AutoEat;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Inject(method = "resetBlockRemoving", at = @At(value = "HEAD"), cancellable = true)
    private void onResetBlockRemoving(CallbackInfo info) {
        BlockResetEvent blockResetEvent = new BlockResetEvent();
        EventBus.INSTANCE.post(blockResetEvent);

        if (blockResetEvent.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    public void attackEntityPre(EntityPlayer playerIn, Entity targetEntity, CallbackInfo ci) {
        if (targetEntity == null) return;
        AttackEvent event = new AttackEvent.Pre(targetEntity);
        EventBus.INSTANCE.post(event);
        if (event.getCancelled()) ci.cancel();
    }

    @Inject(method = "attackEntity", at = @At("RETURN"))
    public void attackEntityPost(EntityPlayer playerIn, Entity targetEntity, CallbackInfo ci) {
        if (targetEntity == null) return;
        AttackEvent event = new AttackEvent.Post(targetEntity);
        EventBus.INSTANCE.post(event);
    }

    @Inject(method = "onStoppedUsingItem", at = @At("HEAD"), cancellable = true)
    public void onStoppedUsingItemMixin(EntityPlayer player, CallbackInfo ci) {
        if (AutoEat.INSTANCE.getAntiReset() && AutoEat.INSTANCE.isEnabled() && AutoEat.INSTANCE.getActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "syncCurrentPlayItem", at = @At("HEAD"), cancellable = true)
    public void onSlotUpdate(CallbackInfo ci) {
        ci.cancel();
        HotbarManager.INSTANCE.handleSlotUpdate();
    }
}
