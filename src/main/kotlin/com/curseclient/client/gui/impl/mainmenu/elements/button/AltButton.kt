package com.curseclient.client.gui.impl.mainmenu.elements.button

import com.curseclient.client.gui.impl.mainmenu.MainMenu
import com.curseclient.client.gui.impl.mainmenu.MainMenuElement
import com.curseclient.client.gui.impl.mainmenu.elements.alt.Account
import com.curseclient.client.gui.impl.mainmenu.elements.alt.AltGui
import com.curseclient.client.gui.impl.mainmenu.elements.alt.AltManager
import com.curseclient.client.gui.impl.mainmenu.elements.animation.HoverAnimation
import com.curseclient.client.manager.managers.data.DataManager
import com.curseclient.client.utility.render.font.FontUtils.drawCentreString
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.sound.SoundUtils
import com.curseclient.client.utility.sound.SoundUtils.playSound
import net.minecraft.client.gui.*
import java.awt.Color

class AltButton(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    val text: String,
    val action: MainMenu.AltAction
) : MainMenuElement(x, y, width, height) {

    private var isClicking = false
    private val hoverAnimation = HoverAnimation()
    private val altManager = AltManager()
    var progress = 0.0

    fun update(partialTicks: Float) {
        hoverAnimation.update(isHovered, partialTicks)
        progress = hoverAnimation.getHoverProgress()
    }

    override fun onRender(mouseX: Int, mouseY: Int, partialTicks: Float, mainGui: GuiScreen) {
        super.onRender(mouseX, mouseY, partialTicks, mainGui)
        update(partialTicks)
        val color = if (isHovered) 35 else 55

        RectBuilder(
            Vec2d(getLeftBottom().x - (progress * 5), getLeftBottom().y.toDouble()),
            Vec2d(getRightTop().x + (progress * 5), getRightTop().y.toDouble()))
            .apply {
                color(Color(color, color, color, 100))
                radius(3.0)
                draw()
            }

        Fonts.DEFAULT.drawCentreString(text, Vec2d(x.toDouble(), y.toDouble()), color = Color.WHITE, scale = 1.0 + (progress / 5))
    }

    override fun onMouseClick(mouseX: Int, mouseY: Int, mouseButton: Int, mainGui: GuiScreen) {
        if(isHovered){
            isClicking = true
        }
        if (isClicking) playSound(SoundUtils.Sound.INTERFACE2, 1.0)
    }

    override fun onMouseRelease(mouseX: Int, mouseY: Int, state: Int, mainGui: GuiScreen) {
        if(isClicking){
            isClicking = false
            runAction()
        }
    }

    private fun runAction() {
        when (action) {
            MainMenu.AltAction.Add -> {
                if (altManager.altName.isNotEmpty()) {
                    DataManager.alts.add(Account(altManager.altName))
                    AltGui.saveUserAvatar("https://minotar.net/helm/${altManager.altName}/16.png", altManager.altName)
                    altManager.altName = ""
                    altManager.typing = false
                }
            }
            MainMenu.AltAction.Random -> {
                val name = "CurseUser" + (Math.random() * 10000).toInt()
                DataManager.alts.add(Account(name))
                try {
                    Thread { AltGui.saveUserAvatar("https://minotar.net/helm/$name/16.png", name) }.start()
                } catch (_: Exception) {
                }
            }
            MainMenu.AltAction.Back -> {
                mc.displayGuiScreen(MainMenu())
            }
        }
    }
}