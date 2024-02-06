package com.curseclient.mixin.gui;

import com.curseclient.client.gui.impl.mcgui.DateTimeDisplay;
import com.curseclient.client.utility.render.font.FontRenderer;
import com.curseclient.client.utility.render.font.Fonts;
import com.curseclient.client.utility.render.shader.RoundedUtil;
import net.minecraft.client.gui.GuiResourcePackAvailable;
import net.minecraft.client.gui.GuiResourcePackSelected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenResourcePacks;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.*;

@Mixin(GuiScreenResourcePacks.class)
public class MixinGuiScreenResourcePacks extends GuiScreen {

    @Shadow private GuiResourcePackAvailable availableResourcePacksList;

    @Shadow private GuiResourcePackSelected selectedResourcePacksList;

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        final FontRenderer fonts = FontRenderer.INSTANCE;
        final boolean shadow = false;
        final Color color = Color.WHITE;

        this.drawBackground(0);
        this.availableResourcePacksList.drawScreen(mouseX, mouseY, partialTicks);
        this.selectedResourcePacksList.drawScreen(mouseX, mouseY, partialTicks);
        RoundedUtil.INSTANCE.drawImage(new ResourceLocation("textures/icons/logo/curseg.png"), 10, 0, 30, 30, Color.WHITE);
        fonts.drawString(
            I18n.format("resourcePack.title"),
            (float) 40, 10,
            shadow,
            color,
            2F,
            Fonts.PROTOTYPE
        );
        this.drawCenteredString(this.fontRenderer, I18n.format("resourcePack.folderInfo"), this.width / 2 - 77, this.height - 26, 8421504);

        DateTimeDisplay dateTimeDisplay = new DateTimeDisplay();
        dateTimeDisplay.drawDateTime(fonts, width, shadow, color);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

}
