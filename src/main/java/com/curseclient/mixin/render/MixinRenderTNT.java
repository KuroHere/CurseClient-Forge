package com.curseclient.mixin.render;

import com.curseclient.client.module.impls.visual.TNTESP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderTNTPrimed;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityTNTPrimed;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.text.DecimalFormat;

@Mixin(RenderTNTPrimed.class)
public class MixinRenderTNT {

    private static DecimalFormat timeFormatter = new DecimalFormat("0.00");

    @Inject(method = "doRender*", at = @At("HEAD"))
    public void doRender(EntityTNTPrimed entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if(TNTESP.INSTANCE.isEnabled() && TNTESP.INSTANCE.getTimeCount()) {
            doTNTRender((RenderTNTPrimed) (Object) this, entity, x, y, z, partialTicks);
        }
    }

    private static void doTNTRender(RenderTNTPrimed tntRenderer, EntityTNTPrimed tntPrimed, double x, double y, double z, float partialTicks) {
        int fuseTimer = tntPrimed.getFuse();

        if (fuseTimer >= 1) {
            double distance = tntPrimed.getDistanceSq(tntRenderer.getRenderManager().renderViewEntity);

            if (distance <= 4096.0D) {
                float number = ((float) fuseTimer - partialTicks) / 20.0F;
                String time = timeFormatter.format(number);
                FontRenderer fontrenderer = tntRenderer.getFontRendererFromRenderManager();

                GlStateManager.pushMatrix();
                GlStateManager.translate((float) x + 0.0F, (float) y + tntPrimed.height + 0.5F, (float) z);
                GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(-tntRenderer.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
                byte xMultiplier = 1;

                if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2) {
                    xMultiplier = -1;
                }

                float scale = 0.02666667F;

                GlStateManager.rotate(tntRenderer.getRenderManager().playerViewX * (float) xMultiplier, 1.0F, 0.0F, 0.0F);
                GlStateManager.scale(-scale, -scale, scale);
                GlStateManager.disableLighting();
                GlStateManager.depthMask(false);
                GlStateManager.disableDepth();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                int stringWidth = fontrenderer.getStringWidth(time) >> 1;
                float green = Math.min((float) fuseTimer / 80.0F, 1.0F);
                Color color = new Color(1.0F - green, green, 0.0F);

                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                GlStateManager.disableTexture2D();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
                bufferbuilder.pos(-stringWidth - 1, -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                bufferbuilder.pos(-stringWidth - 1, 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                bufferbuilder.pos(stringWidth + 1, 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                bufferbuilder.pos(stringWidth + 1, -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                tessellator.draw();
                GlStateManager.enableTexture2D();
                fontrenderer.drawString(time, -fontrenderer.getStringWidth(time) >> 1, 0, color.getRGB());
                GlStateManager.enableLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.popMatrix();
            }

        }
    }
}
