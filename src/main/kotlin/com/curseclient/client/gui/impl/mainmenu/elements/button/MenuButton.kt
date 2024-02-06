package com.curseclient.client.gui.impl.mainmenu.elements.button

import com.curseclient.client.gui.impl.mainmenu.MainMenu
import com.curseclient.client.gui.impl.mainmenu.MainMenuElement
import com.curseclient.client.gui.impl.mainmenu.elements.alt.AltManager
import com.curseclient.client.gui.impl.mainmenu.elements.animation.HoverAnimation
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.module.impls.client.MenuShader
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.ColorUtils.toColor
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.font.FontUtils.drawCentreString
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.shader.RoundedUtil.endBlend
import com.curseclient.client.utility.render.shader.RoundedUtil.startBlend
import com.curseclient.client.utility.render.shader.blur.GaussianBlur
import com.curseclient.client.utility.render.shader.blur.KawaseBloom
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.sound.SoundUtils
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11.*
import java.awt.Color

class MenuButton(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    val text: String,
    val action: MainMenu.MenuAction
) : MainMenuElement(x, y, width, height) {

    private var isClicking = false
    val c1 = ColorUtils.pulseColor(HUD.getColor(0), 50, 1)

    private val hoverAnimation = HoverAnimation()
    var progress = 0.0

    fun update(partialTicks: Float) {
        hoverAnimation.update(isHovered, partialTicks)
        progress = hoverAnimation.getHoverProgress()
    }

    override fun onRender(mouseX: Int, mouseY: Int, partialTicks: Float, mainGui: GuiScreen) {
        update(partialTicks)
        val fr = Fonts.DEFAULT
        glPushMatrix()
        startBlend()
        drawBackground()
        drawText(fr)
        drawIcon()
        endBlend()
        glPopMatrix()
        //hoverDescription(mouseX + 2, mouseY - 3)
    }

    private fun drawBackground() {
        startBlend()
        glPushMatrix()

        val color = when {
            isHovered -> 35
            !isClicking -> 45
            else -> 55
        }

        GaussianBlur.glDoubleDataBlur({
            RenderUtils2D.drawRect(
                Vec2d(getLeftBottom().x, getLeftBottom().y), Vec2d(getRightTop().x, getRightTop().y), Color(color, color, color, 100)
            )
        }, 15f, 5f)

        MenuShader.glShader({
            RectBuilder(Vec2d(getLeftBottom().x.toDouble(), getLeftBottom().y.toDouble()), Vec2d(getRightTop().x.toDouble(), getRightTop().y.toDouble() / progress)).apply {
                color(Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE)
                draw()
            }
        }, MenuShader.ButtonType.diversity)

        RectBuilder(Vec2d(getLeftBottom().x, getLeftBottom().y), Vec2d(getRightBottom().x, getRightBottom().y - 7 - (progress * 2).toInt())).apply {
            color(Color.WHITE.setAlpha(0), Color.WHITE.setAlpha(0), Color.WHITE, Color.WHITE)
            draw()
        }
        RectBuilder(Vec2d(getLeftBottom().x, getLeftBottom().y), Vec2d(getRightBottom().x, getRightBottom().y - 5)).apply {
            color(Color.WHITE)
            draw()
        }
        endBlend()
        glPopMatrix()
    }

    private fun drawText(fr: Fonts) {
        startBlend()
        glPushMatrix()
        fr.drawCentreString(
            text,
            Vec2d(x.toDouble(), y.toDouble() - 0.1),
            shadow = false,
            scale = 2.68 + (progress / 5),
            color = if (isHovered) ColorUtils.pulseColor(Color.WHITE, 5, 5)
            else Color.WHITE
        )
        endBlend()
        glPopMatrix()
    }

    private fun drawIcon() {
        if (progress > 0.05) {
            GlStateManager.pushMatrix()
            drawIcon(
                x + (width / 2),
                y - (height / 2),
                16 + (progress * 5).toInt(),
                16 + (progress * 5).toInt(),
                progress.toFloat()
            )
            GlStateManager.popMatrix()
        }
    }

    private fun drawIcon(x: Int, y: Int, width: Int, height: Int, alphaIn: Float) {
        val alpha = clamp(alphaIn, 0.0f, 1.0f)

        val targetImage = when (action) {
            MainMenu.MenuAction.SinglePlayer -> ColorUtils.icon_singleplayer
            MainMenu.MenuAction.MultiPlayer -> ColorUtils.icon_multiplayer
            MainMenu.MenuAction.AltManager -> ColorUtils.icon_altmanager
            MainMenu.MenuAction.Exit -> ColorUtils.icon_shutdown
        }

        startBlend()
        glPushMatrix()
        GlStateManager.translate((x - (width / 2)).toDouble(), ((y - (height / 2)).toDouble()), 0.0)
        GlStateManager.rotate((15.0 * progress).toFloat(), 0.0f, 0.0f, 1.0f)
        glColor4f(1f, 1f, 1f, alpha)
        RenderUtils2D.drawImage(targetImage, 0, 0, width, height)
        endBlend()
        glPopMatrix()
    }

    override fun onMouseClick(mouseX: Int, mouseY: Int, mouseButton: Int, mainGui: GuiScreen) {
        if(isHovered){
            isClicking = true
        }
        if (isClicking) SoundUtils.playSound(SoundUtils.Sound.INTERFACE2, 1.0)
    }

    override fun onMouseRelease(mouseX: Int, mouseY: Int, state: Int, mainGui: GuiScreen) {
        if(isClicking){
            isClicking = false
            runAction(mainGui)
        }
    }

    private fun runAction(mainGuiIn: GuiScreen) {
        when (action) {
            MainMenu.MenuAction.SinglePlayer -> displayGui(GuiWorldSelection(mainGuiIn))
            MainMenu.MenuAction.MultiPlayer -> displayGui(GuiMultiplayer(mainGuiIn))
            MainMenu.MenuAction.AltManager -> displayGui(AltManager())
            MainMenu.MenuAction.Exit -> mc.shutdown()

        }
    }

    private fun displayGui(gui: GuiScreen) {
        mc.displayGuiScreen(gui)
    }

    /*private fun hoverDescription(mouseX: Int, mouseY: Int) {
    val color = when {
        isHovered -> 35
        !isClicking -> 45
        else -> 55
    }

    if (isHovered) {
        val description = when (action) {
            MainMenu.MenuAction.SinglePlayer -> "Selecting a world."
            MainMenu.MenuAction.MultiPlayer -> "Selecting a server."
            MainMenu.MenuAction.AltManager -> "On work."
            MainMenu.MenuAction.Settings -> "Configure minecraft."
            MainMenu.MenuAction.Exit -> "Shutting down minecraft."
        }

        RectBuilder(
            Vec2d(mouseX.toDouble(), mouseY - height / 2.0 + 10).minus(1.0, 5.0),
            Vec2d(mouseX.toDouble(), mouseY - height / 2.0 + 10).plus(
                Fonts.DEFAULT.getStringWidth(description) + 1.0,
                Fonts.DEFAULT.getHeight()
            )
        ).apply {
            colorH(
                color.toColor(),
                color.toColor().brighter()
            )
            radius(1.0)
            draw()
        }
        Fonts.DEFAULT.drawString(description, Vec2d(mouseX.toDouble(), mouseY - height / 2.0 + 10), true, Color(255, 255, 255, 255))
    }
}*/
}