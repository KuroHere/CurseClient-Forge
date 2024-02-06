package com.curseclient.mixin.gui;

import com.curseclient.client.gui.impl.mcgui.DateTimeDisplay;
import com.curseclient.client.utility.render.font.FontRenderer;
import com.curseclient.client.utility.render.font.Fonts;
import com.curseclient.client.utility.render.shader.RoundedUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiKeyBindingList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;

@Mixin(GuiControls.class)
public class MixinGuiControls extends GuiScreen {

    final long date = System.currentTimeMillis();
    @Shadow private GuiKeyBindingList keyBindingList;

    @Shadow protected String screenTitle;

    @Shadow @Final private GameSettings options;

    @Shadow private GuiButton buttonReset;

    /**
     * @author Kuro_Here; 09:15 | 17.01.2024
     * @reason Uh, all of this only for fck font?
     */
    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        final FontRenderer fonts = FontRenderer.INSTANCE;
        final boolean shadow = false;
        final Color color = Color.WHITE;

        this.drawDefaultBackground();
        this.keyBindingList.drawScreen(mouseX, mouseY, partialTicks);
        RoundedUtil.INSTANCE.drawImage(new ResourceLocation("textures/icons/logo/curseg.png"), 10, 0, 30, 30, Color.WHITE);
        fonts.drawString(
            I18n.format(this.screenTitle),
            (float) 40,
            10,
            shadow,
            color,
            2F,
            Fonts.PROTOTYPE
        );

        DateTimeDisplay dateTimeDisplay = new DateTimeDisplay();
        dateTimeDisplay.drawDateTime(fonts, width, shadow, color);

        boolean flag = false;

        for (KeyBinding keybinding : this.options.keyBindings)
        {
            if (!keybinding.isSetToDefaultValue())
            {
                flag = true;
                break;
            }
        }

        this.buttonReset.enabled = flag;
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
