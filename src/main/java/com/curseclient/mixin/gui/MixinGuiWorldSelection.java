package com.curseclient.mixin.gui;

import com.curseclient.client.gui.impl.mcgui.DateTimeDisplay;
import com.curseclient.client.utility.render.font.FontRenderer;
import com.curseclient.client.utility.render.font.Fonts;
import com.curseclient.client.utility.render.shader.RoundedUtil;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiListWorldSelection;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.*;
import java.time.ZoneId;

@Mixin(GuiWorldSelection.class)
public class MixinGuiWorldSelection extends GuiScreen {

    @Shadow private String worldVersTooltip;

    @Shadow private GuiListWorldSelection selectionList;

    @Shadow protected String title = "Select world";

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        final FontRenderer fonts = FontRenderer.INSTANCE;
        final boolean shadow = false;
        final Color color = Color.WHITE;

        this.worldVersTooltip = null;
        drawDefaultBackground();
        this.selectionList.drawScreen(mouseX, mouseY, partialTicks);
        RoundedUtil.INSTANCE.drawImage(new ResourceLocation("textures/icons/logo/curseg.png"), 10, 0, 30, 30, Color.WHITE);

        fonts.drawString(
            this.title,
            (float) 40,
            10,
            shadow,
            color,
            2F,
            Fonts.PROTOTYPE
        );

        DateTimeDisplay dateTimeDisplay = new DateTimeDisplay();
        dateTimeDisplay.drawDateTime(fonts, width, shadow, color);

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.worldVersTooltip != null)
        {
            this.drawHoveringText(Lists.newArrayList(Splitter.on("\n").split(this.worldVersTooltip)), mouseX, mouseY);
        }
    }

}
