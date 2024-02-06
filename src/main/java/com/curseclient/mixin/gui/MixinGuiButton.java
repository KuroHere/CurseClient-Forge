package com.curseclient.mixin.gui;

import com.curseclient.client.gui.impl.clickgui.elements.ModuleButton;
import com.curseclient.client.module.impls.client.HUD;
import com.curseclient.client.module.impls.client.SoundManager;
import com.curseclient.client.utility.DeltaTime;
import com.curseclient.client.utility.render.RenderUtils2D;
import com.curseclient.client.utility.render.StencilUtil;
import com.curseclient.client.utility.render.animation.animaions.simple.SimpleUtil;
import com.curseclient.client.utility.render.font.FontRenderer;
import com.curseclient.client.utility.render.font.Fonts;
import com.curseclient.client.utility.render.shader.RectBuilder;
import com.curseclient.client.utility.render.shader.blur.GaussianBlur;
import com.curseclient.client.utility.render.vector.Vec2d;
import com.curseclient.client.utility.sound.SoundUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.*;

@Mixin(GuiButton.class)
public abstract class MixinGuiButton extends Gui {

    @Shadow
    public boolean visible;

    @Shadow public int x;

    @Shadow public int y;

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Shadow
    protected boolean hovered;

    @Shadow
    public boolean enabled;

    public ModuleButton baseButton;

    @Shadow
    protected abstract void mouseDragged(Minecraft mc, int mouseX, int mouseY);

    @Shadow
    protected abstract int getHoverState(boolean mouseOver);

    @Shadow
    public String displayString;

    @Shadow
    @Final
    protected static ResourceLocation BUTTON_TEXTURES;

    private float moveX = 0F;
    private float progress = 0f;
    private float cut;
    private float alpha;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            hovered = (mouseX >= this.x && mouseY >= this.y &&
                mouseX < this.x + this.width && mouseY < this.y + this.height);

            final int delta = DeltaTime.deltaTime;
            final float speedDelta = 0.01F * delta;

            if (enabled && hovered) {
                cut += 0.05F * delta;
                if (cut >= 4) cut = 4;
                alpha += 0.3F * delta;
                if (alpha >= 210) alpha = 210;

                moveX = SimpleUtil.INSTANCE.animate(this.width - 2.4F, moveX, speedDelta);
            } else {
                cut -= 0.05F * delta;
                if (cut <= 0) cut = 0;
                alpha -= 0.3F * delta;
                if (alpha <= 120) alpha = 120;

                moveX = SimpleUtil.INSTANCE.animate(0F, moveX, speedDelta);
            }

            float roundCorner = Math.max(0F, 2.4F + moveX - (this.width - 2.4F));

            Color c1 = HUD.INSTANCE.getColor(0, 255);
            Color c2 = HUD.INSTANCE.getColor(10, 255);

            switch (HUD.INSTANCE.getGuiButtonStyle().toString().toLowerCase()) {
                case "minecraft":
                    mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
                    int i = this.getHoverState(this.hovered);
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    GlStateManager.blendFunc(770, 771);
                    this.drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
                    this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
                    this.mouseDragged(mc, mouseX, mouseY);
                    int j = 14737632;

                    if (!this.enabled) {
                        j = 10526880;
                    } else if (this.hovered) {
                        j = 16777120;
                    }

                    this.drawCenteredString(mc.fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
                    break;
                case "liquidbounce":
                    Gui.drawRect(this.x + (int) this.cut, this.y,
                        this.x + this.width - (int) this.cut, this.y + this.height,
                        this.enabled ? new Color(0F, 0F, 0F, this.alpha / 255F).getRGB() :
                            new Color(0.5F, 0.5F, 0.5F, 0.5F).getRGB());
                    break;
                case "rounded":
                    RenderUtils2D.INSTANCE.originalRoundedRect(this.x, this.y,
                        this.x + this.width, this.y + this.height, 2F,
                        this.enabled ? new Color(0F, 0F, 0F, this.alpha / 255F).getRGB() :
                            new Color(0.5F, 0.5F, 0.5F, 0.5F).getRGB());
                    break;
                case "liquidbounceplus":
                    RenderUtils2D.INSTANCE.drawRoundedRect(this.x, this.y, this.x + this.width, this.y + this.height, 2.4F, new Color(0, 0, 0, 150).getRGB());
                    RenderUtils2D.INSTANCE.customRounded(this.x, this.y, this.x + 2.4F + moveX, this.y + this.height, 2.4F, roundCorner, roundCorner, 2.4F, (this.enabled ? c1 : new Color(71, 71, 71)).getRGB());
                    break;
                case "curseclient":
                    Runnable rectTop = () -> RenderUtils2D.INSTANCE.originalRoundedRect(
                        this.x, this.y,
                        this.x + this.width,
                        this.y + this.height,
                        2F, Color.WHITE.getRGB());
                    GaussianBlur.INSTANCE.glBlur(rectTop, 30, 2);
                    StencilUtil.INSTANCE.glStencil(rectTop);

                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GL11.glPushMatrix();
                    progress = SimpleUtil.INSTANCE.animate(alpha, progress, speedDelta);

                    new RectBuilder(new Vec2d(x, y), new Vec2d(x + width, y + height / 1.4))
                        .color(new Color(45, 45, 45, 80))
                        .radius(0.0)
                        .draw();

                    StencilUtil.INSTANCE.uninitStencilBuffer();
                    new RectBuilder(new Vec2d(x, y + height / 1.25), new Vec2d(x + width, y + height))
                        .color(enabled ? c1 : Color.DARK_GRAY, enabled ? c2 : Color.DARK_GRAY, enabled ? c1 : Color.DARK_GRAY, enabled ? c2 : Color.DARK_GRAY)
                        .radius(0.0)
                        .draw();
                    GlStateManager.resetColor();
                    GL11.glPopMatrix();
                    GL11.glDisable(GL11.GL_BLEND);
                    break;
            }

            if (HUD.INSTANCE.getGuiButtonStyle().toString().equalsIgnoreCase("minecraft")) return;

            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            mouseDragged(mc, mouseX, mouseY);

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glPushMatrix();

            //FontGlyphs.Companion.setAssumeNonVolatile(true);

            if (mc.getLanguageManager().isCurrentLocaleUnicode())
                mc.fontRenderer.drawStringWithShadow(displayString,
                    (float) ((this.x + this.width / 2) -
                        mc.fontRenderer.getStringWidth(displayString) / 2),
                    this.y + (this.height - 5) / 2F - 2, Color.WHITE.getRGB());
            else
                FontRenderer.INSTANCE.drawString(displayString,
                        (this.x + (float) this.width / 2) -
                            FontRenderer.INSTANCE.getStringWidth(displayString, Fonts.DEFAULT_BOLD, 1) / 2,
                    this.y + (this.height - 5) / 2F - 2, true, Color.WHITE, 1, Fonts.DEFAULT_BOLD);

            //FontGlyphs.Companion.setAssumeNonVolatile(false);

            GlStateManager.resetColor();
            GL11.glPopMatrix();
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    /**
     * @author Kuro_Here
     * @reason Custom mc ui button sound
     */
    @Overwrite
    public void playPressSound(SoundHandler soundHandlerIn)
    {
        if (SoundManager.INSTANCE.getMcButtonSound()) SoundManager.INSTANCE.playButton();
        else soundHandlerIn.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
