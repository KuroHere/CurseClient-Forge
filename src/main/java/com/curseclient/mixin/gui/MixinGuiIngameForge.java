package com.curseclient.mixin.gui;

import com.curseclient.client.module.modules.client.Animations;
import com.curseclient.client.module.modules.visual.HungerOverlay;
import com.curseclient.client.utility.render.animation.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.curseclient.client.utility.DeltaTime.deltaTime;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.PLAYER_LIST;

@Mixin(value = GuiIngameForge.class, remap = false)
public abstract class MixinGuiIngameForge extends MixinGuiIngame {

    @Shadow(remap = false)
    protected abstract boolean pre(RenderGameOverlayEvent.ElementType type);

    @Shadow(remap = false)
    protected abstract void post(RenderGameOverlayEvent.ElementType type);

    public float xScale = 0F;

    @Inject(
        method = "renderChat",
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/eventhandler/EventBus;post(Lnet/minecraftforge/fml/common/eventhandler/Event;)Z", ordinal = 0, remap = false)),
        at = @At(value = "RETURN", ordinal = 0),
        remap = false
    )
    private void fixProfilerSectionNotEnding(int width, int height, CallbackInfo ci) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.profiler.getNameOfLastSection().endsWith("chat"))
            mc.profiler.endSection();
    }

    @Inject(method = "renderExperience", at = @At("HEAD"), remap = false)
    private void enableExperienceAlpha(int filled, int top, CallbackInfo ci) {
        GlStateManager.enableAlpha();
    }

    @Inject(method = "renderExperience", at = @At("RETURN"), remap = false)
    private void disableExperienceAlpha(int filled, int top, CallbackInfo ci) {
        GlStateManager.disableAlpha();
    }

    protected MixinGuiIngameForge(GuiPlayerTabOverlay overlayPlayerList) {
        this.overlayPlayerList = overlayPlayerList;
    }

    @Shadow
    public abstract void renderFood(int width, int height);

    @Overwrite(remap = false)
    protected void renderPlayerList(int width, int height) {
        final Minecraft mc = Minecraft.getMinecraft();
        ScoreObjective scoreobjective = mc.world.getScoreboard().getObjectiveInDisplaySlot(0);
        NetHandlerPlayClient handler = mc.player.connection;

        if (!mc.isIntegratedServerRunning() || handler.getPlayerInfoMap().size() > 1 || scoreobjective != null)
        {
            Animations.INSTANCE.getTabAnimations();
            xScale = AnimationUtils.INSTANCE.animate((mc.gameSettings.keyBindPlayerList.isKeyDown() ? 100F : 0F), xScale, 0.0125F * deltaTime);
            float rescaled = xScale / 100F;
            boolean displayable = rescaled > 0F;
            this.overlayPlayerList.updatePlayerList(displayable);
            if (!displayable || pre(PLAYER_LIST)) return;
            GlStateManager.pushMatrix();
            switch (Animations.INSTANCE.getTabAnimations().getDisplayName().toLowerCase()) {
                case "zoom":
                    GlStateManager.translate(width / 2F * (1F - rescaled), 0F, 0F);
                    GlStateManager.scale(rescaled, rescaled, rescaled);
                    break;
                case "slide":
                    GlStateManager.scale(1F, rescaled, 1F);
                    break;
                case "none":
                    break;
            }

            this.overlayPlayerList.renderPlayerlist(width, mc.world.getScoreboard(), scoreobjective);
            GlStateManager.popMatrix();
            post(PLAYER_LIST);
        }
        else
        {
            this.overlayPlayerList.updatePlayerList(false);
        }
    }


    @Inject(method = "renderHealthMount", at = @At("HEAD"))
    private void renderHealthMount(int width, int height, CallbackInfo ci) {
        if (HungerOverlay.INSTANCE.getRenderFoodOnRideable()) {
            this.renderFood(width, height);
        }
    }
}
