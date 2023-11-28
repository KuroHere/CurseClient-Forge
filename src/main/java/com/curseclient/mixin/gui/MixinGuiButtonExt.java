package com.curseclient.mixin.gui;

import com.curseclient.client.manager.managers.ModuleManager;
import com.curseclient.client.module.modules.client.HUD;
import com.curseclient.client.utility.render.RenderUtils2D;
import com.curseclient.client.utility.render.animation.AnimationUtils;
import com.curseclient.client.utility.render.font.FontRenderer;
import com.curseclient.client.utility.render.font.Fonts;
import com.curseclient.client.utility.DeltaTime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.awt.*;

@Mixin(GuiButtonExt.class)
public abstract class MixinGuiButtonExt extends GuiButton {

    private float moveX = 0F;
    private float cut;
    private float alpha;

    public MixinGuiButtonExt(int p_i1020_1_, int p_i1020_2_, int p_i1020_3_, String p_i1020_4_) {
        super(p_i1020_1_, p_i1020_2_, p_i1020_3_, p_i1020_4_);
    }

    public MixinGuiButtonExt(int p_i46323_1_, int p_i46323_2_, int p_i46323_3_, int p_i46323_4_,
                             int p_i46323_5_, String p_i46323_6_) {
        super(p_i46323_1_, p_i46323_2_, p_i46323_3_, p_i46323_4_, p_i46323_5_, p_i46323_6_);
    }


    /**
     * @author From LiquidBounce++
     * @reason hm hmm..
     */
    @Overwrite
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (visible) {

            hovered = (mouseX >= this.x && mouseY >= this.y &&
                mouseX < this.x + this.width && mouseY < this.y + this.height);

            final int delta = DeltaTime.deltaTime;
            final float speedDelta = 0.01F * delta;

            final HUD hud = (HUD) ModuleManager.INSTANCE.getModuleByName("HUD");

            if (hud == null) return;

            if (enabled && hovered) {
                // CurseClient
                cut += 0.05F * delta;
                if (cut >= 4) cut = 4;
                alpha += 0.3F * delta;
                if (alpha >= 210) alpha = 210;

                // CurseClient+
                moveX = AnimationUtils.INSTANCE.animate(this.width - 2.4F, moveX, speedDelta);
            } else {
                // CurseClient
                cut -= 0.05F * delta;
                if (cut <= 0) cut = 0;
                alpha -= 0.3F * delta;
                if (alpha <= 120) alpha = 120;

                // CurseClient+
                moveX = AnimationUtils.INSTANCE.animate(0F, moveX, speedDelta);
            }

            float roundCorner = (float) Math.max(0F, 2.4F + moveX - (this.width - 2.4F));
            Color c1 = HUD.INSTANCE.getColor(0, 255);

            switch (hud.getGuiButtonStyle().toString().toLowerCase()) {
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

                    if (!this.enabled)
                    {
                        j = 10526880;
                    }
                    else if (this.hovered)
                    {
                        j = 16777120;
                    }

                    this.drawCenteredString(mc.fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
                    break;
                case "curseclient":
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
                case "curseclientplus":
                    RenderUtils2D.INSTANCE.drawRoundedRect(this.x, this.y, this.x + this.width, this.y + this.height, 2.4F, new Color(0, 0, 0, 150).getRGB());
                    RenderUtils2D.INSTANCE.customRounded(this.x, this.y, this.x + 2.4F + moveX, this.y + this.height, 2.4F, roundCorner, roundCorner, 2.4F, (this.enabled ? c1 : new Color(71, 71, 71)).getRGB());
                    break;
            }

            if (hud.getGuiButtonStyle().toString().equalsIgnoreCase("minecraft")) return;

            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            mouseDragged(mc, mouseX, mouseY);

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glPushMatrix();

            if (mc.getLanguageManager().isCurrentLocaleUnicode())
                mc.fontRenderer.drawStringWithShadow(displayString,
                    (float) ((this.x + this.width / 2) -
                        mc.fontRenderer.getStringWidth(displayString) / 2),
                    this.y + (this.height - 5) / 2F - 2, 14737632);
            else
                FontRenderer.INSTANCE.drawString(displayString,
                    (float) ((this.x + this.width / 2) -
                        FontRenderer.INSTANCE.getStringWidth(displayString, Fonts.DEFAULT, 1) / 2),
                    this.y + (this.height - 5) / 2F - 2, true, new Color(14737632), 1, Fonts.DEFAULT);

            GlStateManager.resetColor();
            GL11.glPopMatrix();
            GL11.glDisable(GL11.GL_BLEND);
        }
    }
}
