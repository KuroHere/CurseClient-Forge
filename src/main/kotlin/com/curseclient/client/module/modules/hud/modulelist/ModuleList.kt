package com.curseclient.client.module.modules.hud.modulelist

import com.curseclient.client.manager.managers.ModuleManager
import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.module.HudModule
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts

// TODO: need to make this can move pos
object ModuleList : DraggableHudModule(
    "ModuleList",
    "Shows active modules",
    HudCategory.HUD,
    alwaysListenable = true
) {
    private val sizeSetting by setting("Size", 1.0, 0.5, 2.0, 0.05)
    val animationMode by setting("Animation Mode", ModuleListAnimationMode.Scale)
    val animationSpeed by setting("Animation Speed", 2.0, 0.5, 3.0, 0.1)
    val textShadow by setting("Text Shadow", true)
    val bgAlpha by setting("Background Alpha", 0.0, 0.0, 1.0, 0.05)
    val bgBlur by setting("Blur", false)
    val radius by setting("BlurRadius", 20, 5, 50, 1, { bgBlur })
    val compression by setting("Compression", 2, 1, 5, 1, { bgBlur })
    val line by setting("Line", false)

    val size get() = sizeSetting
    val font get() = Fonts.DEFAULT

    enum class ModuleListAnimationMode {
        Slide,
        Scale
    }

    private fun getModules(): List<Module> {
        return ModuleManager.getModules()
            .filter { it.isEnabled() && it.isVisible && it !is HudModule }
            .sortedBy { font.getStringWidth(it.name + if(it.getHudInfo() != "") " " + it.getHudInfo() else "", size) }
            .reversed()
    }

    private val elements = ArrayList<ModuleListElement>()

    private fun updatePositions(){
        elements.reverse()
        elements.sortBy { font.getStringWidth(it.module.name + if(it.module.getHudInfo() != "") " " + it.module.getHudInfo() else "", size) }
        elements.reverse()

        var y = pos.y
        for((i, e) in elements.withIndex()){
            e.y = y

            e.pos = i

            y += e.getHeight()
        }
    }

    private fun Module.isAdded(): Boolean {
        return elements.any { it.module.name == this.name }
    }

    override fun onRender() {
        getModules().filter { !it.isAdded() && isEnabled() }.forEach { elements.add(ModuleListElement(it, 0.0, pos.y, 0)) }

        elements.forEach { it.update() }
        updatePositions()
        elements.removeIf { it.shouldRemoved }
        elements.forEach { it.render() }
    }
}