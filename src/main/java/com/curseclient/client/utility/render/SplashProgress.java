package com.curseclient.client.utility.render;

import com.curseclient.client.module.modules.client.HUD;
import com.curseclient.client.utility.render.font.UnicodeFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class SplashProgress {
    private static final int MAX = 6;
    public static int PROGRESS = 0;
    public static String CURRENT = "";
    private static ResourceLocation splash;
    private static TextureManager ctm;

    public static void update() {
        if(ctm == null || Minecraft.getMinecraft() == null || Minecraft.getMinecraft().getLanguageManager() == null){
            return;
        }
        drawSplash(Minecraft.getMinecraft().getTextureManager());
    }

    public static void setProgress(int givenProgress, String givenText){
        PROGRESS = givenProgress;
        CURRENT = givenText;
        update();
    }

    /**
     * FontRenderer for drawing splash screen
     */
    private static UnicodeFontRenderer sfr;

    public static void drawSplash(TextureManager tm) {
        if (ctm == null)
            ctm = tm;
        ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
        int i = scaledresolution.getScaleFactor();
        Framebuffer framebuffer = new Framebuffer(scaledresolution.getScaledWidth() * i, scaledresolution.getScaledHeight() * i, true);
        framebuffer.bindFramebuffer(false);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, (double) scaledresolution.getScaledWidth(), (double) scaledresolution.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();

        if (splash == null)
            splash = new ResourceLocation("splash.png");
        tm.bindTexture(splash);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawScaledCustomSizeModalRect(0, 0, 0, 0, 1920, 1080, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), 1920, 1080);

        GlStateManager.resetColor();
        GlStateManager.color(1.0f,1.0f,1.0f,1.f);
        Gui.drawScaledCustomSizeModalRect(0,0,0,0,1920,1080, scaledresolution.getScaledWidth(),scaledresolution.getScaledHeight(),1920,1080);
        drawBarProgress();
        framebuffer.unbindFramebuffer();
        framebuffer.framebufferRender(scaledresolution.getScaledWidth() * i, scaledresolution.getScaledHeight() * i);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        Minecraft.getMinecraft().updateDisplay();
    }

    private static void drawBarProgress() {
        if(Minecraft.getMinecraft().gameSettings == null || Minecraft.getMinecraft().getTextureManager() == null){
            return;
        }
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        if(sfr == null){
            sfr = UnicodeFontRenderer.getFontOnPC("Arial", 20);
        }
        double nProgress = (double)PROGRESS;
        double calc = (nProgress/MAX) * sr.getScaledWidth();

        //Gui.drawRect(0, sr.getScaledHeight() -35, sr.getScaledWidth(), sr.getScaledHeight(), new Color(0,0,0,50).getRGB());

        GlStateManager.resetColor();
        resetTextureState();

        sfr.drawString(CURRENT, 20,sr.getScaledHeight() - 35, 0xFFFFFFFF);
        String step = PROGRESS + "/" + MAX;
        sfr.drawString(step,sr.getScaledWidth() - 20 - sfr.getStringWidth(step), sr.getScaledHeight() - 35, 0xe1e1e1FF);

        GlStateManager.resetColor();
        resetTextureState();

        RenderUtils2D.INSTANCE.drawRoundedRect(
            10,
            sr.getScaledHeight() - 10,
            (int) calc - 10,
            sr.getScaledHeight() - 20,
            5, HUD.INSTANCE.getColor(-1, 150).getRGB()
        );

        RenderUtils2D.INSTANCE.drawRoundedRect(
            10,
            sr.getScaledHeight() - 10,
            sr.getScaledWidth() - 10,
            sr.getScaledHeight() - 20,
            5,
            new Color(15,15,15,50).getRGB()
        );

    }

    private static void resetTextureState() {
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(352, 0.1f);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
