package com.curseclient.client.gui.impl.clickgui

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.gui.impl.clickgui.elements.CategoryPanel
import com.curseclient.client.gui.impl.particles.image.ParticleEngine
import com.curseclient.client.module.Category
import com.curseclient.client.module.modules.client.ClickGui
import com.curseclient.client.utility.render.vector.Vec2d
import org.lwjgl.input.Mouse

class ClickGuiHud : AbstractGui() {
    var currentScale = 1.0; private set
    override fun getScaleFactor() = currentScale

    val panels = ArrayList<CategoryPanel>()
    private var particleEngine: ParticleEngine = ParticleEngine()
    var dWheel = 0.0; private set

    override fun onRegister() =
        Category.values().forEachIndexed { index, category ->
            val panel = CategoryPanel(Vec2d(3.0 + index.toDouble() * (ClickGui.width + 3.0), 5.0), 0.0, 0.0, this, category)
            panel.onRegister()
            panels.add(panel)

            if (particleEngine == null)
                particleEngine = ParticleEngine()
        }


    override fun onGuiOpen() = panels.forEach { it.onGuiOpen() }.also { currentScale = ClickGui.scale }
    override fun onGuiClose() = panels.forEach { it.onGuiClose() }

    override fun onKey(typedChar: Char, key: Int) = super.onKey(typedChar, key).also { panels.forEach { it.onKey(typedChar, key) } }
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
    }

    override fun onTick() = panels.forEach { it.onTick() }

    override fun onRender() {

        if (ClickGui.darkness )
            drawDefaultBackground()
        dWheel = Mouse.getDWheel().toDouble()

        if (ClickGui.newParticles)
            particleEngine.render();
        panels.forEach { it.onRender() }
    }

    fun isPanelFocused(panel: CategoryPanel): Boolean {
        if (panel == panels.lastOrNull { it.panelHovered }) return true

        val index = panels.indexOf(panel)
        val hoveredPanels = panels.filter { it.panelHovered }
        return hoveredPanels.all { panels.indexOf(it) < index }
    }
}
