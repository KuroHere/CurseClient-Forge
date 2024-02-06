package com.curseclient.client.gui.impl.mainmenu

import com.curseclient.CurseClient
import com.curseclient.client.gui.impl.mainmenu.api.MenuButtonManager
import com.curseclient.client.gui.impl.mainmenu.api.GuiManager
import com.curseclient.client.gui.impl.mainmenu.api.IconButtonManager
import com.curseclient.client.gui.impl.mainmenu.elements.button.MenuButton
import com.curseclient.client.gui.impl.mainmenu.elements.button.IconButton
import com.curseclient.client.module.impls.client.MenuShader
import com.curseclient.client.utility.render.shader.RoundedUtil
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import java.awt.Color

class MainMenu: GuiScreen() {

    private val menuButtonManager = MenuButtonManager()
    private val iconButtonManager = IconButtonManager()
    private val guiManager = GuiManager()

    private val buttons = mutableListOf<MenuButton>()
    private val iconButton = mutableListOf<IconButton>()

    override fun initGui() {
        playMusic()
        iconButtonManager.updateMusicButtons(ScaledResolution(mc))
        menuButtonManager.updateButtons(ScaledResolution(mc))
        MenuShader.initTime = System.currentTimeMillis()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        menuButtonManager.drawButtons(mouseX, mouseY, partialTicks, this)
        guiManager.drawScreenComponents(buttons, iconButton, mouseX, mouseY, partialTicks, this)
        iconButtonManager.drawButtons(mouseX, mouseY, partialTicks, this)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        menuButtonManager.handleMouseClick(mouseX, mouseY, mouseButton, this)
        iconButtonManager.handleMouseClick(mouseX, mouseY, mouseButton, this)

    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        menuButtonManager.handleMouseRelease(mouseX, mouseY, state, this)
        iconButtonManager.handleMouseRelease(mouseX, mouseY, state, this)
    }

    private fun playMusic() {
        if (!mc.soundHandler.isSoundPlaying(CurseClient.songManager.menuSong)) {
            mc.soundHandler.playSound(CurseClient.songManager.menuSong)
        }
    }

    override fun doesGuiPauseGame(): Boolean = false

    enum class AltAction {
        Random,
        Back,
        Add
    }

    enum class IconAction {
        Next,
        Previous,
        Action,
        Settings,
    }

    enum class MenuAction {
        SinglePlayer,
        MultiPlayer,
        AltManager,
        Exit
    }
}