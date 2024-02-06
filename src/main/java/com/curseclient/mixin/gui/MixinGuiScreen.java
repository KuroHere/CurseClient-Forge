package com.curseclient.mixin.gui;

import com.curseclient.client.manager.managers.ScreenManager;
import com.curseclient.client.module.impls.client.GuiClickCircle;
import com.curseclient.client.module.impls.client.MenuShader;
import com.curseclient.client.utility.render.ClickCircle;
import com.curseclient.client.utility.render.shader.RoundedUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen {

    @Shadow
    public Minecraft mc;

    @Shadow
    public int width;

    @Shadow
    public int height;
    protected boolean shouldRenderBackground() {
        return true;
    }

    @Redirect(method = "handleKeyboardInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKeyState()Z", remap = false))
    private boolean checkCharacter() {
        return Keyboard.getEventKey() == 0 && Keyboard.getEventCharacter() >= ' ' || Keyboard.getEventKeyState();
    }

    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    void drawBackground(CallbackInfo ci) {
        ci.cancel();
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        if (MenuShader.INSTANCE.isDisabled()) {
            RoundedUtil.INSTANCE.drawImage(new ResourceLocation("textures/menu/darkmenu.png"), 0, 0, width, height, Color.WHITE);
        } else
            MenuShader.draw();
    }

    private List<ClickCircle> clickEffects = new ArrayList<>();


    @Inject(method = "drawScreen(IIF)V", at = @At(value = "HEAD"))
    public void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        ScreenManager.INSTANCE.drawCircle(GuiClickCircle.INSTANCE.getColor());
        GL11.glPushMatrix();
        if(!clickEffects.isEmpty()) {
            for (ClickCircle clickEffect : clickEffects) {
                clickEffect.draw(GuiClickCircle.INSTANCE.getColor().getRGB());
            }
        }
        GL11.glPopMatrix();
    }

    @Inject(method = "mouseClicked(III)V", at = @At(value = "HEAD"))
    public void mouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if (GuiClickCircle.INSTANCE.isEnabled())
            ScreenManager.INSTANCE.getClickCircles().add(new ClickCircle((float) mouseX, (float) mouseY, (int) GuiClickCircle.INSTANCE.getSeconds(), (int) GuiClickCircle.INSTANCE.getRadius(), GuiClickCircle.INSTANCE.getEasing().toString()));
        GL11.glPushMatrix();
        if(!clickEffects.isEmpty()) {
            for (ClickCircle clickEffect : clickEffects) {
                clickEffect.draw(GuiClickCircle.INSTANCE.getColor().getRGB());
            }
        }
        GL11.glPopMatrix();
    }

}
