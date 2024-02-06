package com.curseclient.mixin.gui;

import com.curseclient.client.module.impls.misc.ChatMod;
import com.curseclient.client.utility.render.animation.animaions.simple.SimpleAnimation;
import com.curseclient.client.utility.render.graphic.GLUtils;
import com.curseclient.client.utility.render.shader.RectBuilder;
import com.curseclient.client.utility.render.shader.blur.KawaseBloom;
import com.curseclient.client.utility.render.vector.Vec2d;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiChat;

import java.awt.*;

/**
 * @author KuroHere 08.01.2024
 */
@Mixin(GuiChat.class)
public class MixinGuiChat extends GuiScreen {

    @Shadow
    protected GuiTextField inputField;
    private final SimpleAnimation animation = new SimpleAnimation(0.0F);
    @Inject(method = "keyTyped", at = @At("RETURN"))
    private void updateLength(CallbackInfo callbackInfo) {
        if (!inputField.getText().startsWith("."))
            return;

        if (!inputField.getText().startsWith("." + "lc"))
            inputField.setMaxStringLength(10000);
        else
            inputField.setMaxStringLength(100);
    }


    /**
     * @author: Kuro_Here
     * @reason: Custom chat bar
     */
    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        animation.setAnimation(32, 10);

        if (ChatMod.INSTANCE.isEnabled() && ChatMod.INSTANCE.getBarAnimation()) {
            Runnable bloom = () ->
                new RectBuilder(new Vec2d(2, this.height - animation.getValue() + 28), new Vec2d(25 + fontRenderer.getStringWidth(inputField.getText()), this.height - animation.getValue() + 14))
                    .color(Color.BLACK)
                    .radius(1.0)
                    .draw();
            KawaseBloom.INSTANCE.glBloom(bloom, 1, 1);

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glPushMatrix();
            if (!inputField.getText().isEmpty() && inputField.getText().startsWith(".")) {
                new RectBuilder(new Vec2d(2, this.height - animation.getValue() + 28), new Vec2d(25 + fontRenderer.getStringWidth(inputField.getText()), this.height - animation.getValue() + 14))
                    .color(new Color(Integer.MIN_VALUE))
                    .radius(1.0)
                    .draw();
            } else
                new RectBuilder(new Vec2d(2, this.height - animation.getValue() + 28), new Vec2d(25 + fontRenderer.getStringWidth(inputField.getText()) , this.height - animation.getValue() + 14))
                    .color(new Color(15, 15, 15, 180))
                    .radius(1.0)
                    .draw();

            new RectBuilder(new Vec2d(3, this.height - animation.getValue() + 27), new Vec2d(5, this.height - animation.getValue() + 15))
                .color(Color.WHITE)
                .radius(1.0)
                .draw();
            new RectBuilder(new Vec2d(25 + fontRenderer.getStringWidth(inputField.getText()) - 1, this.height - animation.getValue() + 27), new Vec2d(25 + fontRenderer.getStringWidth(inputField.getText()) - 3, this.height - animation.getValue() + 15))
                .color(Color.WHITE)
                .radius(1.0)
                .draw();
            GlStateManager.resetColor();
            GL11.glPopMatrix();
            GL11.glDisable(GL11.GL_BLEND);

            GLUtils.INSTANCE.startTranslate((float) 0, 29 -  animation.getValue());
            this.inputField.drawTextBox();
            this.inputField.x = (int) 8.5;

            TextComponentBase iChatComponent =
                (TextComponentBase) this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

            if (iChatComponent != null)
                this.handleComponentHover(iChatComponent, mouseX, mouseY);
        } else {
            drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);
            this.inputField.drawTextBox();
            ITextComponent itextcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

            if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null)
            {
                this.handleComponentHover(itextcomponent, mouseX, mouseY);
            }

            super.drawScreen(mouseX, mouseY, partialTicks);
        }
    }
}
