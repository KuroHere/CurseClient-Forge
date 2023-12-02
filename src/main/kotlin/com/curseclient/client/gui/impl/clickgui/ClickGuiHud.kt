package com.curseclient.client.gui.impl.clickgui

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.gui.impl.clickgui.elements.CategoryPanel
import com.curseclient.client.gui.impl.clickgui.elements.settings.misc.DescriptionDisplay
import com.curseclient.client.gui.impl.particles.image.ParticleEngine
import com.curseclient.client.module.Category
import com.curseclient.client.module.modules.client.ClickGui
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.utility.render.ParticleUtils
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse
import java.awt.Color


class ClickGuiHud : AbstractGui() {
    var currentScale = 1.1; private set
    override fun getScaleFactor() = currentScale

    val panels = ArrayList<CategoryPanel>()
    private var particleEngine: ParticleEngine = ParticleEngine()
    private var particle: ParticleUtils = ParticleUtils
    var descriptionDisplay: DescriptionDisplay? = null

    var dWheel = 0.0; private set

    override fun onRegister() =
        Category.values().forEachIndexed { index, category ->
            val panel = CategoryPanel(Vec2d(3.0 + index.toDouble() * (ClickGui.width + 3.0), 5.0), 0.0, 0.0, 0, this, category)
            panel.onRegister()
            panels.add(panel)

            if (particleEngine == null)
                particleEngine = ParticleEngine()
            if (particle == null)
                particle = ParticleUtils
            descriptionDisplay = DescriptionDisplay("", Vec2d(0.0, 0.0), this)
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
        someEffect()

        dWheel = Mouse.getDWheel().toDouble()
        panels.forEach { it.onRender() }

        if (descriptionDisplay?.shouldDraw() == true) {
            descriptionDisplay?.onRender()
        }
    }

    private fun someEffect() {
        if (ClickGui.darkness )
            RenderUtils2D.drawRect(0f, 0f, width.toFloat() * currentScale.toFloat(), height.toFloat() * currentScale.toFloat(), Color(15, 15, 15, 160).rgb)

        val c1 = if (ClickGui.colorMode == ClickGui.ColorMode.Client)
            HUD.getColor(0)
        else ClickGui.buttonColor1

        //if (particles) {
        //    RenderUtils2D.drawGradientRect(0, (height / 1.2).toInt(), width, height, Color(0,0,0,0).rgb, c1.rgb)
        //    particle.drawParticles(Mouse.getX(), height - Mouse.getY() * height)
        //}
        if (ClickGui.newParticles)
            particleEngine.render()

    }

    fun isPanelFocused(panel: CategoryPanel): Boolean {
        if (panel == panels.lastOrNull { it.panelHovered }) return true

        val index = panels.indexOf(panel)
        val hoveredPanels = panels.filter { it.panelHovered }
        return hoveredPanels.all { panels.indexOf(it) < index }
    }
}
