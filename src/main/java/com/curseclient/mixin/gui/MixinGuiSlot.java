package com.curseclient.mixin.gui;

import com.curseclient.client.module.impls.client.HUD;
import com.curseclient.client.utility.render.ColorUtils;
import com.curseclient.client.utility.render.shader.RectBuilder;
import com.curseclient.client.utility.render.shader.RoundedUtil;
import com.curseclient.client.utility.render.vector.Vec2d;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.*;

import static net.minecraft.util.math.MathHelper.clamp;

@Mixin(GuiSlot.class)
public abstract class MixinGuiSlot {

    @Shadow
    protected boolean visible = true;

    @Shadow protected int mouseX;

    @Shadow protected int mouseY;

    @Shadow
    protected abstract int getScrollBarX();

    @Shadow
    protected abstract void bindAmountScrolled();

    @Shadow public int left;

    @Shadow public int width;

    @Shadow
    public abstract int getListWidth();

    @Shadow public int top;

    @Shadow protected float amountScrolled;

    @Shadow protected boolean hasListHeader;

    @Shadow
    protected abstract void drawListHeader(int insideLeft, int insideTop, Tessellator tessellatorIn);

    @Shadow public int bottom;

    @Shadow public int height;

    @Shadow public int right;

    @Shadow
    public abstract int getMaxScroll();

    @Shadow
    protected abstract int getContentHeight();

    @Shadow
    protected abstract void renderDecorations(int mouseXIn, int mouseYIn);

    @Shadow
    protected abstract int getSize();

    @Shadow @Final public int slotHeight;

    @Shadow public int headerPadding;

    @Shadow
    protected abstract void updateItemPos(int entryID, int insideLeft, int yPos, float partialTicks);

    @Shadow protected boolean showSelectionBox;

    @Shadow
    protected abstract boolean isSelected(int slotIndex);

    @Shadow
    protected abstract void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks);

    /**
     * @author Kuro_Here; 09:07 | 17.01.2024
     * @reason Custom box slot
     */
    @Overwrite
    public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
        if (this.visible)
        {
            this.mouseX = mouseXIn;
            this.mouseY = mouseYIn;
            int i = this.getScrollBarX();
            int j = i + 6;
            this.bindAmountScrolled();
            Tessellator tessellator = Tessellator.getInstance();
            int k = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
            int l = this.top + 4 - (int)this.amountScrolled;
            if (this.hasListHeader)
            {
                this.drawListHeader(k, l, tessellator);
            }
            new RectBuilder(new Vec2d(this.left, top), new Vec2d(this.left + this.width, this.bottom))
                .color(
                    new Color(20, 20, 23, 255),
                    new Color(20, 20, 30, 120),
                    new Color(20, 20, 30, 120),
                    new Color(20, 20, 23, 255))
                .draw();
            this.drawSelectionBox(k, l, mouseXIn, mouseYIn, partialTicks);
            GlStateManager.disableDepth();
            RoundedUtil.INSTANCE.startBlend();
            new RectBuilder(new Vec2d(0, 0), new Vec2d(right + top, top))
                .color(new Color(25, 25, 25, 255))
                .draw();
            new RectBuilder(new Vec2d(0,left + bottom), new Vec2d(right + height, right + height))
                .color(new Color(25, 25, 25, 255))
                .draw();
            RoundedUtil.INSTANCE.endBlend();
            int j1 = this.getMaxScroll();

            if (j1 > 0)
            {
                int k1 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
                k1 = clamp(k1, 32, this.bottom - this.top - 8);
                int l1 = (int)this.amountScrolled * (this.bottom - this.top - k1) / j1 + this.top;

                if (l1 < this.top)
                {
                    l1 = this.top;
                }

                RoundedUtil.INSTANCE.startBlend();
                new RectBuilder(new Vec2d(i, this.bottom), new Vec2d(j, this.top))
                    .color(new Color(0, 0, 0, 150))
                    .draw();
                new RectBuilder(new Vec2d(i, l1 + k1), new Vec2d(j, l1))
                    .color(new Color(128, 128, 128, 150))
                    .radius(1.0)
                    .draw();
                new RectBuilder(new Vec2d(i, l1 + k1 - 1), new Vec2d(j - 1, l1))
                    .color(new Color(192, 192, 192, 150))
                    .radius(1.0)
                    .draw();
                RoundedUtil.INSTANCE.endBlend();
            }

            this.renderDecorations(mouseXIn, mouseYIn);
        }
    }

    protected void drawSelectionBox(int insideLeft, int insideTop, int mouseXIn, int mouseYIn, float partialTicks) {
        int i = this.getSize();
        for (int j = 0; j < i; ++j)
        {
            int k = insideTop + j * this.slotHeight + this.headerPadding;
            int l = this.slotHeight - 4;

            if (k > this.bottom || k + l < this.top)
            {
                this.updateItemPos(j, insideLeft, k, partialTicks);
            }

            int i1 = this.left + (this.width / 2 - this.getListWidth() / 2);
            int j1 = this.left + this.width / 2 + this.getListWidth() / 2;
            Color c1 = HUD.INSTANCE.getColor(0, 150);
            Color c2 = HUD.INSTANCE.getColor(15, 1);

            if (this.showSelectionBox && this.isSelected(j)) {
                RoundedUtil.INSTANCE.startBlend();
                GlStateManager.pushMatrix();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                new RectBuilder(new Vec2d(i1, k + l + 2), new Vec2d(j1, k - 2))
                    .outlineColor(Color.WHITE)
                    .width(1)
                    .color(
                        ColorUtils.INSTANCE.pulseAlpha(ColorUtils.INSTANCE.setAlpha(c1, 180), 10, 10),
                        ColorUtils.INSTANCE.pulseAlpha(ColorUtils.INSTANCE.setAlpha(c2, 180), 0, 10),
                        ColorUtils.INSTANCE.pulseAlpha(ColorUtils.INSTANCE.setAlpha(c1, 180), 10, 10),
                        ColorUtils.INSTANCE.pulseAlpha(ColorUtils.INSTANCE.setAlpha(c2, 180), 0, 10))
                    .radius(2.3)
                    .draw();
                RoundedUtil.INSTANCE.endBlend();
                GlStateManager.popMatrix();
            }
            this.drawSlot(j, insideLeft, k, l, mouseXIn, mouseYIn, partialTicks);

            if (this.showSelectionBox && this.isSelected(j)) {
                RoundedUtil.INSTANCE.startBlend();
                GlStateManager.pushMatrix();
                GlStateManager.translate(i1 - 6, k - 5, 0);

                float zoom = (float) (1.0F + Math.sin(System.currentTimeMillis() % 2000 / 1000.0 * Math.PI) * 0.1F);
                GlStateManager.scale(zoom, zoom, 1.0F);
                GL11.glRotatef(-15.0F, 0.0F, 0.0F, 1.0F);
                RoundedUtil.INSTANCE.drawImage(new ResourceLocation("textures/icons/logo/logo.png"),
                    0, 0, 16, 16, Color.WHITE);
                RoundedUtil.INSTANCE.endBlend();
                GlStateManager.popMatrix();
            }
        }
    }
}
