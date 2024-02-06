package com.curseclient.mixin.player;

import com.curseclient.client.event.EventBus;
import com.curseclient.client.event.events.*;
import com.curseclient.client.manager.managers.PacketManager;
import com.curseclient.client.module.impls.player.FreeCam;
import com.curseclient.client.utility.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
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

    // mc
    @Shadow
    protected Minecraft mc;

    @Shadow
    private boolean prevOnGround;

    @Shadow
    private float lastReportedYaw;

    @Shadow
    private float lastReportedPitch;

    @Shadow
    private int positionUpdateTicks;

    @Shadow
    private double lastReportedPosX;

    @Shadow
    private double lastReportedPosY;

    @Shadow
    private double lastReportedPosZ;

    @Shadow
    private boolean serverSprintState;

    @Shadow
    private boolean serverSneakState;

    @Shadow
    protected abstract boolean isCurrentViewEntity();

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    public void onOnUpdateWalkingPlayer(CallbackInfo info) {

        // pre
        RotationUpdateEvent rotationUpdateEvent = new RotationUpdateEvent();
        EventBus.INSTANCE.post(rotationUpdateEvent);

        // prevent client from sending packets
        if (rotationUpdateEvent.isCanceled()) {

            // post
            MotionUpdateEvent motionUpdateEvent = new MotionUpdateEvent();
            EventBus.INSTANCE.post(motionUpdateEvent);

            // prevent vanilla packets from sending
            info.cancel();

            // send custom packets
            if (motionUpdateEvent.isCanceled()) {
                positionUpdateTicks++;

                boolean sprintUpdate = isSprinting();
                if (sprintUpdate != serverSprintState) {
                    if (sprintUpdate) {
                        mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SPRINTING));
                    }

                    else {
                        mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SPRINTING));
                    }

                    serverSprintState = sprintUpdate;
                }

                boolean sneakUpdate = isSneaking();
                if (sneakUpdate != serverSneakState) {
                    if (sneakUpdate) {
                        mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SNEAKING));
                    }

                    else {
                        mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SNEAKING));
                    }

                    serverSneakState = sneakUpdate;
                }

                if (isCurrentViewEntity()) {
                    boolean movementUpdate = StrictMath.pow(motionUpdateEvent.getX() - lastReportedPosX, 2) + StrictMath.pow(motionUpdateEvent.getY() - lastReportedPosY, 2) + StrictMath.pow(motionUpdateEvent.getZ() - lastReportedPosZ, 2) > 9.0E-4D || positionUpdateTicks >= 20;
                    boolean rotationUpdate = motionUpdateEvent.getYaw() - lastReportedYaw != 0.0D || motionUpdateEvent.getPitch() - lastReportedPitch != 0.0D;

                    if (isRiding()) {
                        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(motionX, -999.0D, motionZ, motionUpdateEvent.getYaw(), motionUpdateEvent.getPitch(), motionUpdateEvent.getOnGround()));
                        movementUpdate = false;
                    }

                    else if (movementUpdate && rotationUpdate) {
                        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(motionUpdateEvent.getX(), motionUpdateEvent.getY(), motionUpdateEvent.getZ(), motionUpdateEvent.getYaw(), motionUpdateEvent.getPitch(), motionUpdateEvent.getOnGround()));
                    }

                    else if (movementUpdate) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(motionUpdateEvent.getX(), motionUpdateEvent.getY(), motionUpdateEvent.getZ(), motionUpdateEvent.getOnGround()));
                    }

                    else if (rotationUpdate) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(motionUpdateEvent.getYaw(), motionUpdateEvent.getPitch(), motionUpdateEvent.getOnGround()));
                    }

                    else if (prevOnGround != motionUpdateEvent.getOnGround()) {
                        mc.player.connection.sendPacket(new CPacketPlayer(motionUpdateEvent.getOnGround()));
                    }

                    if (movementUpdate) {
                        lastReportedPosX = motionUpdateEvent.getX();
                        lastReportedPosY = motionUpdateEvent.getY();
                        lastReportedPosZ = motionUpdateEvent.getZ();
                        positionUpdateTicks = 0;
                    }

                    if (rotationUpdate) {
                        lastReportedYaw = motionUpdateEvent.getYaw();
                        lastReportedPitch = motionUpdateEvent.getPitch();
                    }

                    prevOnGround = motionUpdateEvent.getOnGround();
                    autoJumpEnabled = mc.gameSettings.autoJump;
                }
            }
        }
    }

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
