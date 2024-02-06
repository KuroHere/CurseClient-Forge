package com.curseclient.mixin.gui;

import com.curseclient.client.gui.impl.mcgui.DateTimeDisplay;
import com.curseclient.client.utility.render.font.FontRenderer;
import com.curseclient.client.utility.render.font.Fonts;
import com.curseclient.client.utility.render.shader.RectBuilder;
import com.curseclient.client.utility.render.shader.RoundedUtil;
import com.curseclient.client.utility.render.vector.Vec2d;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumDifficulty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Mixin(GuiOptions.class)
public abstract class MixinGuiOptions extends GuiScreen {

    @Shadow protected String title;

    /**
     * @author Kuro_Here; 09:07 | 17.01.2024
     * @reason Uh, all of this only for fck font?
     */
    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        final FontRenderer fonts = FontRenderer.INSTANCE;
        final boolean shadow = false;
        final Color color = Color.WHITE;

        this.drawDefaultBackground();

        RoundedUtil.INSTANCE.drawImage(new ResourceLocation("textures/icons/logo/curseg.png"), 10, 0, 30, 30, Color.WHITE);
        fonts.drawString(
            this.title,
            (float) 40, 10,
            shadow,
            color,
            2F,
            Fonts.PROTOTYPE
        );

        DateTimeDisplay dateTimeDisplay = new DateTimeDisplay();
        dateTimeDisplay.drawDateTime(fonts, width, shadow, color);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
