package com.curseclient.client.gui.impl.mainmenu.api

import com.curseclient.client.gui.impl.mainmenu.MainMenu
import com.curseclient.client.gui.impl.mainmenu.elements.button.MenuButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution

class MenuButtonManager {
    private val buttons = mutableListOf<MenuButton>()
    private var scaledResolution: ScaledResolution? = null

    fun updateButtons(sr: ScaledResolution) {
        this.scaledResolution = sr
        buttons.clear()

        val width = sr.scaledWidth / 4.09
        val height = 95
        val x = sr.scaledWidth / 7.97
        val y = sr.scaledHeight / 1.09

        val buttonData = listOf(
            "SinglePlayer" to MainMenu.MenuAction.SinglePlayer,
            "MultiPlayer" to MainMenu.MenuAction.MultiPlayer,
            "AltsManager" to MainMenu.MenuAction.AltManager,
            "Exit" to MainMenu.MenuAction.Exit
        )

        buttons.addAll(buttonData.mapIndexed { index, (label, action) ->
            MenuButton((x + (index * 240) - 5).toInt(), y.toInt(), width.toInt(), height, label, action)
        })
    }

    fun handleMouseClick(mouseX: Int, mouseY: Int, mouseButton: Int, screen: GuiScreen) {
        buttons.forEach { it.onMouseClick(mouseX, mouseY, mouseButton, screen) }
    }

    fun handleMouseRelease(mouseX: Int, mouseY: Int, state: Int, screen: GuiScreen) {
        buttons.forEach { it.onMouseRelease(mouseX, mouseY, state, screen) }
    }

    fun drawButtons(mouseX: Int, mouseY: Int, partialTicks: Float, screen: GuiScreen) {
        buttons.forEach { it.onDraw(mouseX, mouseY, partialTicks, screen) }
    }
}