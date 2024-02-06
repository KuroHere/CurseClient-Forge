package com.curseclient.client.module.impls.client

import com.curseclient.client.event.events.ConnectionEvent
import com.curseclient.client.event.listener.listener
import com.curseclient.client.gui.GuiUtils
import com.curseclient.client.gui.impl.clickgui.ClickGuiHud
import com.curseclient.client.manager.managers.data.DataManager
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.ColorUtils.toColor
import com.curseclient.client.utility.threads.runAsync
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import java.awt.Color

object ClickGui : Module(
    "ClickGui",
    "sexy",
    Category.CLIENT,
    key = Keyboard.KEY_RSHIFT,
    alwaysListenable = true
) {
    private val page by setting("Page", Page.General)

    // General
    val scale by setting("Scale", 1.0, 0.5, 1.5, 0.01, { page == Page.General })
    val width by setting("Panel Width", 103.0, 100.0, 140.0, 1.0, { page == Page.General })
    val panelRound by setting("Panel Radius", 3.0, 0.0, 10.0, 0.1, { page == Page.General })
    val height by setting("Button Height", 17.5, 12.0, 20.0, 0.5, { page == Page.General })
    val buttonRound by setting("Button Radius", 1.0, 0.0, 10.0, 0.1, { page == Page.General })
    val space by setting("Space", 3.0, 3.0, 8.0, 0.1, { page == Page.General })
    val sorting by setting("Sorting", SortingMode.Alphabetical, { page == Page.General })
    val reverse by setting("Reverse", false, { page == Page.General })

    //detail
    val bind by setting("Bind", false)
    val characters by setting("Characters", false)
    val open by setting("Open", "+", { page == Page.General && characters })
    val close by setting("Close", "-", { page == Page.General && characters })

    //Background
    val darkness by setting("Darkness ", false, { page == Page.BackGround })
    val darkOpacity by setting("DarkOpacity", 180, 10, 255, 1, { darkness && page == Page.BackGround })
    val imageParticle by setting("ImageParticle", false, { page == Page.BackGround })

    // Font
    val fontSize by setting("Font Size", 1.0, 0.5, 2.0, 0.02, { page == Page.Font })
    val settingFontSize by setting("Setting Font Size", 0.9, 0.5, 2.0, 0.02, { page == Page.Font })
    val titleFontSize by setting("Title Font Size", 1.2, 0.5, 2.0, 0.02, { page == Page.Font })

    // Colors
    val colorMode by setting("Color Mode", ColorMode.Client, { page == Page.Colors })
    var buttonColor1 by setting("Color 1", Color(30, 190, 240), { page == Page.Colors && listOf(ColorMode.Static, ColorMode.Vertical, ColorMode.Horizontal).contains(colorMode) })
    var buttonColor2 by setting("Color 2", Color(170, 30, 215), { page == Page.Colors && listOf(ColorMode.Vertical, ColorMode.Horizontal).contains(colorMode) })

    val pulse by setting("Pulse Color", false, { page == Page.Colors && listOf(ColorMode.Static, ColorMode.Vertical, ColorMode.Horizontal).contains(colorMode)})
    val backgroundShader by setting("ShaderBackground", false, { page == Page.Colors })
    val blurRadius by setting("BlurRadius", 20, 5, 50, 1, { page == Page.Colors && backgroundShader })
    val compression by setting("Compression", 2, 1, 5, 1, { page == Page.Colors && backgroundShader })

    val outline by setting("Outline", false, { page == Page.Colors })
    val outlineWidth by setting("Outline Width", 0.0, 0.0, 3.0, 0.1, { outline && page == Page.Colors })
    val outlineColor by setting("OutlineColor", Color(35, 35, 35, 255), { outline && page == Page.Colors })
    val backgroundColor by setting("Background Color", Color(20, 20, 20), { page == Page.Colors })
    val disabledColor by setting("Disabled Color", Color(35, 35, 35, 30), { page == Page.Colors })

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
        Horizontal,
    }

    fun updateColors(newColor1: Int, newColor2: Int) {
        buttonColor1 = newColor1.toColor()
        buttonColor2 = newColor2.toColor()
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