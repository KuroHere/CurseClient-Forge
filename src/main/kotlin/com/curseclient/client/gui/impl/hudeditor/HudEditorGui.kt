package com.curseclient.client.gui.impl.hudeditor

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.gui.impl.hudeditor.elements.HudPanel
import com.curseclient.client.manager.managers.ModuleManager
import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.module.HudModule
import com.curseclient.client.module.modules.client.ClickGui
import com.curseclient.client.utility.render.vector.Vec2d
import org.lwjgl.input.Mouse

class HudEditorGui : AbstractGui() {
    var currentScale = 1.0; private set
    override fun getScaleFactor() = currentScale

    val panels = ArrayList<HudPanel>()
    val hud: HudModule? = null
    var dWheel = 0.0; private set

    override fun onMouseAction(action: MouseAction, button: Int) {
        if (action == MouseAction.CLICK) {
            panels.lastOrNull { it.panelHovered }?.let { panel ->
                panels.remove(panel)
                panels.add(panel)
            }
        }

        panels.forEach { panel ->
            if (panel == panels.lastOrNull { it.panelHovered } || action == MouseAction.RELEASE)
                panel.onMouseAction(action, button)
        }

        ModuleManager.getHudModules().filterIsInstance<DraggableHudModule>().forEach {
            it.handleMouseAction(mouse, action, button)
        }
    }

    override fun onRegister() =
        HudCategory.values().forEachIndexed { index, category ->
            val panel = HudPanel(Vec2d(3.0 + index.toDouble() * (ClickGui.width + 3.0), 5.0), 0.0, 0.0, this, category)
            panel.onRegister()
            panels.add(panel)
        }

    override fun onGuiOpen() = panels.forEach { it.onGuiOpen() }.also { currentScale = ClickGui.scale }
    override fun onGuiClose() = panels.forEach { it.onGuiClose() }

    override fun onTick() = panels.forEach { it.onTick() }

    override fun onRender() {
        dWheel = Mouse.getDWheel().toDouble()
        panels.forEach { it.onRender() }

    }

    fun isPanelFocused(panel: HudPanel): Boolean {
        if (panel == panels.lastOrNull { it.panelHovered }) return true

        val index = panels.indexOf(panel)
        val hoveredPanels = panels.filter { it.panelHovered }
        return hoveredPanels.all { panels.indexOf(it) < index }
    }
}