package com.curseclient.client.gui.impl.hudeditor

import baritone.api.utils.Helper
import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.gui.impl.hudeditor.elements.HudPanel
import com.curseclient.client.manager.managers.ModuleManager
import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.module.HudModule
import com.curseclient.client.module.modules.client.ClickGui
import com.curseclient.client.utility.render.Screen
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse
import java.awt.Color

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
            val panel = HudPanel(Vec2d(3.0 + index.toDouble() * (ClickGui.width + 3.0), 5.0), 0.0, 0.0, 0, this, category)
            panel.onRegister()
            panels.add(panel)
        }

    override fun onGuiOpen() = panels.forEach { it.onGuiOpen() }.also { currentScale = 1.0 }
    override fun onGuiClose() = panels.forEach { it.onGuiClose() }

    override fun onTick() = panels.forEach { it.onTick() }

    override fun onRender() {
        RectBuilder(Vec2d(0.5 * Screen.width / 2, Screen.height), Vec2d((0.5 * Screen.width / 2 + 0.6), 0.0)).apply {
            color(Color.WHITE)
            draw()
        }
        RectBuilder(Vec2d(Screen.width, 0.5 * Screen.height / 2), Vec2d(0.0, (0.5 * Screen.height / 2 + 0.6))).apply {
            color(Color.WHITE)
            draw()
        }
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