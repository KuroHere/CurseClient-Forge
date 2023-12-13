package com.curseclient.client.gui.impl.maingui.elements

import com.curseclient.client.gui.impl.altmanager.AltGui
import com.curseclient.client.gui.impl.maingui.MainGui
import com.curseclient.client.gui.impl.maingui.MainGuiElement
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.module.modules.hud.compass.Compass
import com.curseclient.client.utility.render.animation.EaseUtils
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.ColorUtils.toColor
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.font.FontRenderer
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.shader.GaussianBlur
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiWorldSelection
import org.lwjgl.opengl.GL11.*
import java.awt.Color

class  GeneralButton(xCenter: Int, yCenter: Int, width: Int, height: Int, val text: String, val action: MainGui.EnumAction) : MainGuiElement(xCenter, yCenter, width, height){
    private var isClicking = false

    val c1 = ColorUtils.pulseColor(HUD.getColor(0), 50, 1)

    override fun onRender(mouseX: Int, mouseY: Int, partialTicks: Float, mainGui: GuiScreen) {
        update(partialTicks)

        val fr = Fonts.DEFAULT

        drawBackground()
        drawText(fr)
        drawIcon()

        hoverDescription(mouseX, mouseY)
    }

    private fun drawBackground() {
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glPushMatrix()

        val color = when {
            isHovered -> 35
            !isClicking -> 45
            else -> 55
        }

        val leftTop = Vec2d(getLeftTop().x.toDouble() - (getHoverProgress() * 2), getLeftTop().y.toDouble())
        val rightBottom = Vec2d(getRightBottom().x.toDouble() + (getHoverProgress() * 2), getRightBottom().y.toDouble())

        GaussianBlur.glBlur({ RenderUtils2D.drawRect(leftTop, rightBottom, Color(color, color, color, 100)) }, 30f)
        RenderUtils2D.drawGradientRect(
            leftTop, rightBottom,
            Color(color, color, color, 255),
            Color(color, color, color, 0),
            Color(color, color, color, 0),
            Color(color, color, color, 255)
        )
        RenderUtils2D.drawLine(leftTop, Vec2d(getRightTop().x.toDouble() + (getHoverProgress() * 2), getRightTop().y.toDouble()), 1f, Color(color, color, color, 255))
        RenderUtils2D.drawLine(Vec2d(getRightBottom().x.toDouble() + (getHoverProgress() * 2), getRightBottom().y.toDouble()), Vec2d(getLeftBottom().x.toDouble() - (getHoverProgress() * 2), getLeftBottom().y.toDouble()), 1f, Color(color, color, color, 255))

        glPopMatrix()
        glDisable(GL_BLEND)
    }

    private fun drawText(fr: Fonts) {
        fr.drawString(
            text,
            Vec2d(xCentered - (width / 2) + 15.0, yCentered.toDouble() - 0.1),
            shadow = false,
            scale = 1.8,
            color = if (action == MainGui.EnumAction.Exit && isHovered) ColorUtils.pulseColor(Color.RED, 1, 5)
            else Color(255, 255, 255, 200)
        )
    }

    private fun drawIcon() {
        if (getHoverProgress() > 0.05) {
            drawIcon(
                xCentered + (width / 2) - (height / 2),
                yCentered + ((height / 2) * (1 - getHoverProgress()) * 0.9).toInt(),
                15 + (getHoverProgress() * 5).toInt(),
                15 + (getHoverProgress() * 5).toInt(),
                getHoverProgress().toFloat()
            )
        }
    }

    private fun hoverDescription(mouseX: Int, mouseY: Int) {
        val color = when {
            isHovered -> 35
            !isClicking -> 45
            else -> 55
        }

        if (isHovered) {
            val description = when (action) {
                MainGui.EnumAction.SinglePlayer -> "Just leave"
                MainGui.EnumAction.MultiPlayer -> "Banned"
                MainGui.EnumAction.AltManager -> "Not work, f*** Kuro!"
                MainGui.EnumAction.Settings -> "Configure your Minecraft"
                MainGui.EnumAction.Exit -> "Don't leave me"
            }

            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            glPushMatrix()

            RectBuilder(
                Vec2d(mouseX.toDouble(), mouseY - height / 2.0 + 10).minus(1.0, 5.0),
                Vec2d(mouseX.toDouble(), mouseY - height / 2.0 + 10).plus(
                    Fonts.DEFAULT.getStringWidth(description) + 1.0,
                    Fonts.DEFAULT.getHeight()
                )
            ).apply {
                color(
                    color.toColor(),
                    color.toColor().brighter(),
                    color.toColor().brighter(),
                    color.toColor()
                )
                radius(1.0)
                draw()
            }

            Fonts.DEFAULT.drawString(description, Vec2d(mouseX.toDouble(), mouseY - height / 2.0 + 10), true, Color(255, 255, 255, 255))

            glPopMatrix()
            glDisable(GL_BLEND)
        }
    }

    private fun drawIcon(centerX: Int, centerY: Int, width: Int, height: Int, alphaIn: Float) {
        val alpha = clamp(alphaIn, 0.0f, 1.0f)

        val targetImage = when (action) {
            MainGui.EnumAction.SinglePlayer -> ColorUtils.icon_singleplayer
            MainGui.EnumAction.MultiPlayer -> ColorUtils.icon_multiplayer
            MainGui.EnumAction.AltManager -> ColorUtils.icon_altmanager
            MainGui.EnumAction.Settings -> ColorUtils.icon_settings
            MainGui.EnumAction.Exit -> ColorUtils.icon_shutdown
        }

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glPushMatrix()
        glColor4f(1f, 1f, 1f, alpha)
        RenderUtils2D.drawImage(targetImage, centerX - (width / 2), centerY - (height / 2), width, height)
        glPopMatrix()
        glDisable(GL_BLEND)
    }

    override fun onMouseClick(mouseX: Int, mouseY: Int, mouseButton: Int, mainGui: GuiScreen) {
        if(isHovered){
            isClicking = true
        }
    }

    override fun onMouseRelease(mouseX: Int, mouseY: Int, state: Int, mainGui: GuiScreen) {
        if(isClicking){
            isClicking = false
            runAction(mainGui)
        }
    }

    private fun runAction(mainGuiIn: GuiScreen) {
        when (action) {
            MainGui.EnumAction.SinglePlayer -> displayGui(GuiWorldSelection(mainGuiIn))
            MainGui.EnumAction.MultiPlayer -> displayGui(GuiMultiplayer(mainGuiIn))
            MainGui.EnumAction.AltManager -> displayGui(AltGui)
            MainGui.EnumAction.Settings -> displayGui(GuiOptions(mainGuiIn, mc.gameSettings))
            MainGui.EnumAction.Exit -> mc.shutdown()
        }
    }

    private fun displayGui(gui: GuiScreen) {
        mc.displayGuiScreen(gui)
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
        return clamp(EaseUtils.getEase(hoverProgress, EaseUtils.EaseType.InQuad), 0.0, 1.0)
    }

}