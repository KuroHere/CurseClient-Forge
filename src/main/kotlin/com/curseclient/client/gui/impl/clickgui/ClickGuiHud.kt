package com.curseclient.client.gui.impl.clickgui

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.gui.impl.clickgui.elements.CategoryPanel
import com.curseclient.client.gui.impl.clickgui.elements.settings.misc.DescriptionDisplay

import com.curseclient.client.gui.impl.clickgui.elements.settings.misc.SearchBar
import com.curseclient.client.gui.impl.particles.image.ParticleEngine
import com.curseclient.client.module.Category
import com.curseclient.client.module.impls.client.ClickGui
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color

class ClickGuiHud : AbstractGui() {

    var currentScale = 1.0; private set
    private var particleEngine: ParticleEngine = ParticleEngine()
    private val sr = ScaledResolution(Minecraft.getMinecraft())
    val search: SearchBar = SearchBar(
        Vec2d(sr.scaledWidth_double - 50.0, sr.scaledHeight_double * 2 - 45.0),
        200.0,
        20.0,
        this
    )
    val panels = ArrayList<CategoryPanel>()
    var descriptionDisplay: DescriptionDisplay? = null
    var dWheel = 0.0; private set

    override fun onRegister() {
        Category.values().forEachIndexed { index, category ->
            val panel = CategoryPanel(Vec2d(10.0 + index * (ClickGui.width + 5.0), 20.0), 0.0, 0.0, 0, this, category)
            panel.onRegister()
            panels.add(panel)
            descriptionDisplay = DescriptionDisplay("", Vec2d(0.0, 0.0), this)
            search.updateScreen()
        }
    }

    override fun getScaleFactor() = currentScale

    override fun onGuiOpen() {
        panels.forEach { it.onGuiOpen() }.also { currentScale = ClickGui.scale } }

    override fun onGuiClose() {
        panels.forEach { it.onGuiClose() }
        search.onGuiClose()
    }

    override fun onKey(typedChar: Char, key: Int) = super.onKey(typedChar, key).also {
        panels.forEach { it.onKey(typedChar, key) }
        search.onKey(typedChar, key)
    }
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
        search.onMouseAction(action, button)
    }

    override fun onTick() {
        panels.forEach { it.onTick() }
    }

    override fun onRender() {
        someEffect()
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
            checkMouseWheel()
        dWheel = Mouse.getDWheel().toDouble()
        panels.forEach { it.onRender() }

        if (descriptionDisplay?.shouldDraw() == true) {
            descriptionDisplay?.onRender()
        }
        search.onRender()
    }

    private fun someEffect() {
        if (ClickGui.darkness )
            RenderUtils2D.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Color(15, 15, 15, ClickGui.darkOpacity.toInt()).rgb)
        if (ClickGui.imageParticle)
            particleEngine.render()
    }

    private fun checkMouseWheel() {
        val scrollAmount = if (dWheel < 0) - 10 else if (dWheel > 0) 10 else 0
        panels.forEach { it.setX((it.pos.x + scrollAmount).toInt()) }
    }

    fun isPanelFocused(panel: CategoryPanel): Boolean {
        if (panel == panels.lastOrNull { it.panelHovered }) return true

        val index = panels.indexOf(panel)
        val hoveredPanels = panels.filter { it.panelHovered }
        return hoveredPanels.all { panels.indexOf(it) < index }
    }
}
