package com.curseclient.client.module.modules.client

import com.curseclient.client.event.events.ConnectionEvent
import com.curseclient.client.event.listener.listener
import com.curseclient.client.gui.GuiUtils
import com.curseclient.client.gui.impl.clickgui.ClickGuiHud
import com.curseclient.client.manager.managers.data.DataManager
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.ColorUtils.r
import com.curseclient.client.utility.threads.runAsync
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Color
import kotlin.random.Random
import kotlin.random.nextInt

object ClickGui : Module(
    "ClickGui",
    "sexy",
    Category.CLIENT,
    alwaysListenable = true
) {
    private val page by setting("Page", Page.General)

    // General
    val scale by setting("Scale", 1.1, 0.5, 1.5, 0.01, { page == Page.General })
    val width by setting("Panel Width", 110.0, 100.0, 140.0, 1.0, { page == Page.General })
    val panelRound by setting("Panel Radius", 4.0, 0.0, 10.0, 0.1, { page == Page.General })
    val height by setting("Button Height", 16.0, 12.0, 20.0, 0.5, { page == Page.General })
    val buttonRound by setting("Button Radius", 2.5, 0.0, 10.0, 0.1, { page == Page.General })
    val outlineWidth by setting("Outline Width", 1.0, 0.0, 3.0, 0.1, { page == Page.General })
    val space by setting("Space", 5.0, 3.0, 8.0, 0.1, { page == Page.General })
    val sorting by setting("Sorting", SortingMode.Alphabetical, { page == Page.General })
    val reverse by setting("Reverse", false, { page == Page.General })

    //detail
    val detailPage by setting("Details", Details.Characters, { page == Page.General })
    val open by setting("Open", "+", { page == Page.General && detailPage == Details.Characters })
    val close by setting("Close", "-", { page == Page.General && detailPage == Details.Characters })

    //Background
    val darkness by setting("Darkness ", false, { page == Page.BackGround })
    val newParticles by setting("Particle", false, { page == Page.BackGround })

    // Font
    val fontSize by setting("Font Size", 1.0, 0.5, 2.0, 0.02, { page == Page.Font })
    val settingFontSize by setting("Setting Font Size", 0.9, 0.5, 2.0, 0.02, { page == Page.Font })
    val titleFontSize by setting("Title Font Size", 1.2, 0.5, 2.0, 0.02, { page == Page.Font })

    // Colors
    val colorMode by setting("Color Mode", ColorMode.Client, { page == Page.Colors })
    val buttonColor1 by setting("Color 1", Color(30, 190, 240), { page == Page.Colors && listOf(ColorMode.Static, ColorMode.Vertical, ColorMode.Horizontal).contains(colorMode) })
    val buttonColor2 by setting("Color 2", Color(170, 30, 215), { page == Page.Colors && listOf(ColorMode.Vertical, ColorMode.Horizontal).contains(colorMode) })

    val pulse by setting("Pulse Color", false, { page == Page.Colors && listOf(ColorMode.Static, ColorMode.Vertical, ColorMode.Horizontal).contains(colorMode)})

    val backgroundColor by setting("Background Color", Color(20, 20, 20), { page == Page.Colors })
    val disabledColor by setting("Disabled Color", Color(255, 255, 255, 30), { page == Page.Colors })

    val buttonAlpha by setting("Button Alpha", 0.7, 0.05, 1.0, 0.01, { page == Page.Colors })
    val settingsBrightness by setting("Settings Brightness", 0.75, 0.0, 1.0, 0.01, { page == Page.Colors })

    // Animations
    val toggleSpeed by setting("Toggle Speed", 1.0, 0.1, 3.0, 0.1, { page == Page.Animations })
    val resizeSpeed by setting("Resize Speed", 2.0, 0.1, 3.0, 0.1, { page == Page.Animations })
    val settingsSpeed by setting("Settings Open Speed", 1.5, 0.1, 3.0, 0.1, { page == Page.Animations })
    val hoverSpeed by setting("Hover Speed", 1.0, 0.1, 3.0, 0.1, { page == Page.Animations })
    val scrollSpeed by setting("Scroll Speed", 2.0, 0.1, 5.0, 0.02, { page == Page.Animations })
    val scrollDecay by setting("Scroll Decay", 0.2, 0.0, 1.0, 0.1, { page == Page.Animations })

    private enum class Page {
        General,
        BackGround,
        Font,
        Colors,
        Animations

    }

    enum class Bind {
        Near,
        far
    }

    enum class Details {
        Characters,
        Bind,
        None
    }

    enum class SortingMode {
        Alphabetical,
        ByWidth
    }

    enum class ColorMode {
        Client,
        Static,
        Vertical,
        Horizontal
    }

    init {
        listener<ConnectionEvent.Connect> {
            setEnabled(false)
        }

        listener<TickEvent.ClientTickEvent> {
            if (mc.currentScreen !is ClickGuiHud) setEnabled(false)

        }
    }

    override fun onEnable() {
        GuiUtils.hideAll()
        GuiUtils.clickGuiHudNew?.let { GuiUtils.showGui(it) }

    }

    override fun onDisable() {
        if (mc.currentScreen is ClickGuiHud) GuiUtils.hideAll()
        runAsync {
            DataManager.saveConfig()
        }
    }
}