package com.curseclient.client.gui

import com.curseclient.client.event.EventBus
import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.impl.clickgui.ClickGuiHud
import com.curseclient.client.gui.impl.hudeditor.HudEditorGui
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen

object GuiUtils {
    private val mc = Minecraft.getMinecraft()

    var clickGuiHudNew: ClickGuiHud? = null
    var hudEditorGui: HudEditorGui? = null

    fun bootstrap() {
        clickGuiHudNew = ClickGuiHud()
        clickGuiHudNew?.onRegister()

        hudEditorGui = HudEditorGui()
        hudEditorGui?.onRegister()
    }

    fun showGui(gui: GuiScreen) {
        mc.displayGuiScreen(gui)
        if (gui is AbstractGui) {
            gui.onGuiOpen()
            gui.isActive = true
            EventBus.subscribe(gui)
        }
    }

    fun hideAll() {
        if (mc.currentScreen is AbstractGui){
            (mc.currentScreen as AbstractGui).onGuiCloseAttempt()
            return
        }

        mc.displayGuiScreen(null)
    }
}