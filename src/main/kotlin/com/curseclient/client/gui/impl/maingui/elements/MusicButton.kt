package com.curseclient.client.gui.impl.maingui.elements

import com.curseclient.CurseClient
import com.curseclient.client.gui.impl.maingui.MainGui
import com.curseclient.client.gui.impl.maingui.MainGuiElement
import com.curseclient.client.manager.managers.SongManager
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.animation.EaseUtils
import com.curseclient.client.utility.render.font.BonIcon
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glBlendFunc
import java.awt.Color

class MusicButton(
    xCenter: Int,
    yCenter: Int,
    width: Int,
    height: Int,
    val action: MainGui.MusicAction,
    val songManager: SongManager // Pass SongManager instance to MusicButton
) : MainGuiElement(xCenter, yCenter, width, height) {

    private var isClicking = false

    override fun onRender(mouseX: Int, mouseY: Int, partialTicks: Float, mainGui: GuiScreen) {
        update(partialTicks)

        GL11.glEnable(GL11.GL_BLEND)
        glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glPushMatrix()

        action()

        GL11.glPopMatrix()
        GL11.glDisable(GL11.GL_BLEND)
    }

    private fun action() {
        val sr = ScaledResolution(mc)

        val x = when (action) {
            MainGui.MusicAction.Action -> sr.scaledWidth / 10 - 8
            MainGui.MusicAction.Next -> sr.scaledWidth / 10 + 8
            MainGui.MusicAction.Previous -> sr.scaledWidth / 10 - 23
        }

        val y = sr.scaledHeight - 18
        val name = when (action) {
            MainGui.MusicAction.Action ->
                if (CurseClient.songManager.isPaused){
                BonIcon.PAUSE
            } else {
                BonIcon.PLAY
            }
            MainGui.MusicAction.Next -> BonIcon.NEXT
            MainGui.MusicAction.Previous -> BonIcon.PREVIOUS
        }

        val hoverScale = 2.0 + (0.2 * (getHoverProgress() * 2).toInt())

        Fonts.BonIcon20.drawString(
            name,
            Vec2d(
                if (hoverScale > 2.0) x - (getHoverProgress() * 2).toInt() else x,
                y
            ),
            true,
            if (isHovered) Color(30, 213, 95).setAlpha((150 + (0.2 * (getHoverProgress() * 2))).toInt()) else Color.WHITE,
            scale = hoverScale
        )
    }

    override fun onMouseClick(mouseX: Int, mouseY: Int, mouseButton: Int, mainGui: GuiScreen) {
        if(isHovered){
            isClicking = true
        }
    }

    override fun onMouseRelease(mouseX: Int, mouseY: Int, state: Int, mainGui: GuiScreen) {
        if (isClicking) {
            isClicking = false
            runAction()
        }
    }

    private fun runAction() {
        when (action) {
            MainGui.MusicAction.Next -> {
                songManager.skip()
            }
            MainGui.MusicAction.Previous -> {
                songManager.skip()
            }
            MainGui.MusicAction.Action -> {
                songManager.playOrPause()
            }
        }
    }

    private fun update(partialTicks: Float){
        if(isHovered) {
            hoverProgress += (0.4 * partialTicks)

        }else{
            hoverProgress -= (0.10 * partialTicks)
        }

        if(hoverProgress > 1.0) hoverProgress = 1.0
        if(hoverProgress < 0.0) hoverProgress = 0.0
    }

    private var hoverProgress = 0.0
    private fun getHoverProgress():Double{
        return MathUtils.clamp(EaseUtils.getEase(hoverProgress, EaseUtils.EaseType.InQuad), 0.0, 1.0)
    }

}