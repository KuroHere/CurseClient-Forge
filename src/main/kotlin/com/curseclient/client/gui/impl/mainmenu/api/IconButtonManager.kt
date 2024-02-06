package com.curseclient.client.gui.impl.mainmenu.api

import com.curseclient.CurseClient.Companion.songManager
import com.curseclient.client.gui.impl.mainmenu.MainMenu
import com.curseclient.client.gui.impl.mainmenu.elements.button.IconButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution

class IconButtonManager {
    private val iconButtons = mutableListOf<IconButton>()
    private var scaledResolution: ScaledResolution? = null

    fun updateMusicButtons(sr: ScaledResolution) {
        this.scaledResolution = sr
        iconButtons.clear()

        val buttonData = listOf(
            Pair(MainMenu.IconAction.Action, sr.scaledWidth / 1.119) to 25.5,
            Pair(MainMenu.IconAction.Next, sr.scaledWidth / 1.119 + 15) to 25.5,
            Pair(MainMenu.IconAction.Previous, sr.scaledWidth / 1.119 - 15) to 25.5,
            Pair(MainMenu.IconAction.Settings, sr.scaledWidth - 15.0) to 15.0,
        )

        iconButtons.addAll(buttonData.map { (pair, yOffset) ->
            val (action, xOffset) = pair
            IconButton(xOffset.toInt(), yOffset.toInt(), 15, 15, action, songManager)
        })
    }

    fun handleMouseClick(mouseX: Int, mouseY: Int, mouseButton: Int, screen: GuiScreen) {
        iconButtons.forEach { it.onMouseClick(mouseX, mouseY, mouseButton, screen) }
    }

    fun handleMouseRelease(mouseX: Int, mouseY: Int, state: Int, screen: GuiScreen) {
        iconButtons.forEach { it.onMouseRelease(mouseX, mouseY, state, screen) }
    }

    fun drawButtons(mouseX: Int, mouseY: Int, partialTicks: Float, screen: GuiScreen) {
        iconButtons.forEach { it.onDraw(mouseX, mouseY, partialTicks, screen) }
    }
}