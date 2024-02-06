package com.curseclient.mixin.gui;

import com.curseclient.client.module.impls.misc.Animations;
import com.curseclient.client.module.impls.client.HUD;
import com.curseclient.client.module.impls.misc.ChestStealer;
import com.curseclient.client.utility.render.animation.ease.EaseUtils;
import com.curseclient.client.utility.render.RenderUtils2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer extends MixinGuiScreen {

    @Shadow
    protected abstract boolean checkHotbarKeys(int keyCode);

    @Shadow private int dragSplittingButton;
    @Shadow private int dragSplittingRemnant;
    private float progress = 0F;

    private long lastMS = 0L;
    private boolean translated = false;


    @Inject(method = "initGui", at = @At("HEAD"), cancellable = true)
    public void injectInitGui(CallbackInfo callbackInfo){
        lastMS = System.currentTimeMillis();
        progress = 0F;
    }

    @Inject(method = "drawScreen", at = @At("HEAD"), cancellable = true)
    private void drawScreenHead(CallbackInfo callbackInfo) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (progress >= 1F) progress = 1F;
        else {
            progress = (float) (System.currentTimeMillis() - lastMS) / (float) Animations.INSTANCE.getAnimTimeValue();
        }

        double trueAnim = EaseUtils.INSTANCE.getEase(progress, Animations.INSTANCE.getGuiEase(), false);
        assert mc.currentScreen != null;

        if (HUD.INSTANCE.getContainerBackground()
            && (!(mc.currentScreen instanceof GuiChest)
            || !ChestStealer.INSTANCE.isEnabled()))
            RenderUtils2D.drawRect(0, 0, this.width, this.height, new Color(0x80101010, true));


        if (Animations.INSTANCE.isEnabled()) {
            GL11.glPushMatrix();
            switch (Animations.INSTANCE.getGuiAnimations().getDisplayName()) {
                case "Zoom":
                    GL11.glTranslated((1 - trueAnim) * (width / 2D), (1 - trueAnim) * (height / 2D), 0D);
                    GL11.glScaled(trueAnim, trueAnim, trueAnim);
                    break;
                case "Slide":
                    switch (Animations.INSTANCE.getHSlideValue().getDisplayName()) {
                        case "Right":
                            GL11.glTranslated((1 - trueAnim) * -width, 0D, 0D);
                            break;
                        case "Left":
                            GL11.glTranslated((1 - trueAnim) * width, 0D, 0D);
                            break;
                    }
                    switch (Animations.INSTANCE.getVSlideValue().getDisplayName()) {
                        case "Upward":

                            GL11.glTranslated(0D, (1 - trueAnim) * height, 0D);
                            break;
                        case "Downward":
                            GL11.glTranslated(0D, (1 - trueAnim) * -height, 0D);
                            break;
                    }
                    break;
                case "Smooth":
                    GL11.glTranslated((1 - trueAnim) * -width, (1 - trueAnim) * -height / 4F, 0D);
                    break;
            }
            translated = true;
        }
    }

    @Override
    protected boolean shouldRenderBackground() {
        return false;
    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    private void drawScreenReturn(CallbackInfo callbackInfo) {
        if (translated) {
            GL11.glPopMatrix();
            translated = false;
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void checkCloseClick(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if (mouseButton - 100 == mc.gameSettings.keyBindInventory.getKeyCode()) {
            mc.player.closeScreen();
            ci.cancel();
        }
    }

    @Inject(method = "mouseClicked", at = @At("TAIL"))
    private void checkHotbarClicks(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        checkHotbarKeys(mouseButton - 100);
    }

    @Inject(method = "updateDragSplitting", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void fixRemnants(CallbackInfo ci) {
        if (this.dragSplittingButton == 2) {
            this.dragSplittingRemnant = mc.player.inventory.getItemStack().getMaxStackSize();
            ci.cancel();
        }
    }
}
