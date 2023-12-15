package com.curseclient.mixin.gui;

import com.curseclient.client.manager.managers.ModuleManager;
import com.curseclient.client.manager.managers.ScreenManager;
import com.curseclient.client.module.modules.client.GuiClickCircle;
import com.curseclient.client.module.modules.client.HUD;
import com.curseclient.client.module.modules.client.MenuShader;
import com.curseclient.client.utility.render.ClickCircle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen {
    @Shadow
    protected List<GuiButton> buttonList;

    @Shadow
    public Minecraft mc;

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Redirect(method = "handleKeyboardInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKeyState()Z", remap = false))
    private boolean checkCharacter() {
        return Keyboard.getEventKey() == 0 && Keyboard.getEventCharacter() >= ' ' || Keyboard.getEventKeyState();
    }

    /**
     * @author CCBlueX (superblaubeere27)
     * @reason Making it possible for other mixins to receive actions
     */
    @Inject(method = "actionPerformed", at = @At("RETURN"))
    protected void injectActionPerformed(GuiButton button, CallbackInfo callbackInfo) {
        this.injectedActionPerformed(button);
    }

    protected boolean shouldRenderBackground() {
        return true;
    }


    protected void injectedActionPerformed(GuiButton button) {

    }
    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    void drawBackground(CallbackInfo ci) {
        if (!MenuShader.INSTANCE.isEnabled()) return;

        ci.cancel();
        MenuShader.draw();
    }
    private List<ClickCircle> clickEffects = new ArrayList<>();

    @Inject(method = "drawScreen(IIF)V", at = @At(value = "HEAD"))
    public void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        GuiClickCircle module = (GuiClickCircle) ModuleManager.INSTANCE.getModuleByName("GuiClickCircle");
        assert module != null;
        ScreenManager.INSTANCE.drawCircle(module.getColor());

        if(!clickEffects.isEmpty()) {
            Iterator<ClickCircle> clickEffectIterator= clickEffects.iterator();
            while(clickEffectIterator.hasNext()){
                ClickCircle clickEffect = clickEffectIterator.next();
                clickEffect.draw(module.getColor().getRGB());
                if (clickEffect.canRemove())
                    clickEffectIterator.remove();
            }
        }
    }

    @Inject(method = "mouseClicked(III)V", at = @At(value = "HEAD"))
    public void mouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        GuiClickCircle module = (GuiClickCircle) ModuleManager.INSTANCE.getModuleByName("GuiClickCircle");
        assert module != null;
        if (module.isEnabled())
            ScreenManager.INSTANCE.getClickCircles().add(new ClickCircle((float) mouseX, (float) mouseY, (int) module.getSeconds(), (int) module.getRadius(), module.getEasing().toString()));
        if(!clickEffects.isEmpty()) {
            Iterator<ClickCircle> clickEffectIterator= clickEffects.iterator();
            while(clickEffectIterator.hasNext()){
                ClickCircle clickEffect = clickEffectIterator.next();
                clickEffect.draw(module.getColor().getRGB());
                if (clickEffect.canRemove())
                    clickEffectIterator.remove();
            }
        }
    }
}
