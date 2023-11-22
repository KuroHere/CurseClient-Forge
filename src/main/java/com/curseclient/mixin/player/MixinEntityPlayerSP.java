package com.curseclient.mixin.player;

import com.curseclient.client.event.EventBus;
import com.curseclient.client.event.events.EventUpdate;
import com.curseclient.client.event.events.MoveEvent;
import com.curseclient.client.event.events.PushOutOfBlocksEvent;
import com.curseclient.client.manager.managers.PacketManager;
import com.curseclient.client.module.modules.player.FreeCam;
import com.curseclient.client.utility.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityPlayerSP.class, priority = Integer.MAX_VALUE)
public abstract class MixinEntityPlayerSP extends MixinEntityPlayer {
    @Shadow private boolean autoJumpEnabled;

    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void onUpdate(CallbackInfo ci) {
        EventUpdate event = new EventUpdate();
        EventBus.INSTANCE.post(event);
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    private void onUpdateWalkingPlayer(final CallbackInfo ci) {
        ci.cancel();
        PacketManager.handlePacketUpdate();

        autoJumpEnabled = Minecraft.getMinecraft().gameSettings.autoJump;

    }

    @Shadow
    protected abstract boolean pushOutOfBlocks(double x, double y, double z);

    @Shadow
    protected abstract void updateAutoJump(float p_189810_1_, float p_189810_2_);

    public MixinEntityPlayerSP(World worldIn) {
        super(worldIn);
    }


    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void onPushOutOfBlocks(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        PushOutOfBlocksEvent event = new PushOutOfBlocksEvent();
        EventBus.INSTANCE.post(event);
        if (event.getCancelled()) callbackInfoReturnable.setReturnValue(false);
    }


    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void moveHead(MoverType type, double x, double y, double z, CallbackInfo ci) {
        EntityPlayerSP player = Wrapper.getPlayer();
        if (player == null) return;

        MoveEvent event = new MoveEvent(player);
        EventBus.INSTANCE.post(event);

        if (event.isModified()) {
            double prevX = this.posX;
            double prevZ = this.posZ;

            super.move(type, event.getX(), event.getY(), event.getZ());
            this.updateAutoJump((float) (this.posX - prevX), (float) (this.posZ - prevZ));

            ci.cancel();
        }
    }

    @Inject(method = "isCurrentViewEntity", at = @At("RETURN"), cancellable = true)
    protected void mixinIsCurrentViewEntity(CallbackInfoReturnable<Boolean> cir) {
        if (FreeCam.INSTANCE.isEnabled() && FreeCam.INSTANCE.getCamera() != null) {
            cir.setReturnValue(Minecraft.getMinecraft().getRenderViewEntity() == FreeCam.INSTANCE.getCamera());
        }
    }

}
