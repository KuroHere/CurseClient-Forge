package com.curseclient.client.module.modules.client

import com.curseclient.client.event.events.ConnectionEvent
import com.curseclient.client.event.listener.listener
import com.curseclient.client.gui.GuiUtils
import com.curseclient.client.gui.impl.hudeditor.HudEditorGui
import com.curseclient.client.manager.managers.ModuleManager
import com.curseclient.client.module.Category
import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.Module
import net.minecraftforge.fml.common.gameevent.TickEvent

object HudEditor : Module(
    "HudEditor",
    "Edit your HUD element.",
    Category.CLIENT,
    alwaysListenable = true
){
    init {
        listener<ConnectionEvent.Connect> {
            setEnabled(false)
        }

        listener<TickEvent.ClientTickEvent> {
            if(mc.currentScreen !is HudEditorGui) setEnabled(false)
        }
    }

    override fun onEnable() {
        GuiUtils.hideAll()
        GuiUtils.hudEditorGui?.let { GuiUtils.showGui(it) }
    }

    override fun onDisable() {
        if (mc.currentScreen is HudEditorGui) GuiUtils.hideAll()
        ModuleManager.getModules().filterIsInstance<DraggableHudModule>().forEach { it.isDragging = false }
    }
}