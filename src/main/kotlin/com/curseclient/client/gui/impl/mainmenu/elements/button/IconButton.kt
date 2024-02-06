package com.curseclient.client.gui.impl.mainmenu.elements.button

import com.curseclient.CurseClient
import com.curseclient.client.gui.impl.mainmenu.MainMenu
import com.curseclient.client.gui.impl.mainmenu.MainMenuElement
import com.curseclient.client.gui.impl.mainmenu.elements.animation.HoverAnimation
import com.curseclient.client.manager.managers.SongManager
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.font.impl.BonIcon
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.shader.RoundedUtil
import com.curseclient.client.utility.render.shader.RoundedUtil.drawImage
import com.curseclient.client.utility.render.shader.blur.KawaseBloom
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.sound.SoundUtils
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glBlendFunc
import java.awt.Color


class IconButton(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    val action: MainMenu.IconAction,
    private val songManager: SongManager
) : MainMenuElement(x, y, width, height) {

    private var isClicking = false
    private val hoverAnimation = HoverAnimation()
    var progress = 0.0

    fun update(partialTicks: Float) {
        hoverAnimation.update(isHovered, partialTicks)
        progress = hoverAnimation.getHoverProgress()
    }

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
            MainMenu.IconAction.Action -> sr.scaledWidth / 1.119 - 8
            MainMenu.IconAction.Next -> sr.scaledWidth / 1.119 + 8
            MainMenu.IconAction.Previous -> sr.scaledWidth / 1.119 - 23
            MainMenu.IconAction.Settings -> sr.scaledWidth - 15.0
        }

        val y = when (action) {
            MainMenu.IconAction.Action, MainMenu.IconAction.Next, MainMenu.IconAction.Previous -> 25.5
            MainMenu.IconAction.Settings -> 18.0
        }

        val iconInfo: IconInfo = when (action) {
            MainMenu.IconAction.Action ->
                if (CurseClient.songManager.isPaused) {
                    IconInfo(BonIcon.PAUSE, null)
                } else {
                    IconInfo(BonIcon.PLAY, null)
                }

            MainMenu.IconAction.Next -> IconInfo(BonIcon.NEXT, null)
            MainMenu.IconAction.Previous -> IconInfo(BonIcon.PREVIOUS, null)
            MainMenu.IconAction.Settings -> IconInfo("", ColorUtils.icon_settings)
        }

        val name = iconInfo.name
        val icon = iconInfo.icon

        val hoverScale = 2.0 + (0.2 * (progress * 2).toInt())
        val c1 = HUD.getColor((1 * progress).toInt())
        val c2 = HUD.getColor((10 * progress).toInt())

        if (icon != null) {
            RenderUtils2D.drawBlurredShadow((x - 14F).toFloat(), (y - 14F).toFloat(), 30F, 30F, 10, ColorUtils.interpolateColor(c1, c2, progress.toFloat()))
            RectBuilder(Vec2d(x - 14, y - 14), Vec2d(x + 14, y + 15)).apply {
                outlineColor(c1, c2, c1, c2)
                width(1.0)
                color(Color(0, 0, 15, 250))
                radius(3.0)
                draw()
            }
            KawaseBloom.glBloom({
                drawIcon(icon, x.toFloat(), y.toFloat(), 32F, 32F)
            }, (1 + (progress * 2)).toInt(), 2)
            drawIcon(icon, x.toFloat(), y.toFloat(), 32F, 32F)
        } else
            Fonts.BonIcon20.drawString(
                name,
                Vec2d(if (hoverScale > 2.0) x - (progress * 2).toInt() else x, y),
                shadow = false,
                color = if (isHovered) Color.WHITE else Color.WHITE.setAlpha(230 + (progress * 2).toInt()),
                scale = hoverScale
            )
    }

    data class IconInfo(val name: String, val icon: ResourceLocation?)

    private var rotationAngle = 0F
    private fun drawIcon(icon: ResourceLocation, x: Float, y: Float, width: Float, height: Float) {
        RoundedUtil.startBlend()
        GL11.glPushMatrix()
        GlStateManager.translate((x + width / 2 - 16).toDouble(), (y + height / 2 - 15).toDouble(), 0.0)
        if (isHovered)
            rotationAngle += 1F
        GlStateManager.rotate(MathUtils.calculateRotation(rotationAngle), 0.0f, 0.0f, 1.0f)
        GlStateManager.translate((-(width / 2)).toDouble(), (-(height / 2)).toDouble(), 0.0)
        GL11.glColor4f(1f, 1f, 1f, 1F)
        drawImage(icon, 0F, 0F, width, height, Color.WHITE)

        RoundedUtil.endBlend()
        GL11.glPopMatrix()
    }

    override fun onMouseClick(mouseX: Int, mouseY: Int, mouseButton: Int, mainGui: GuiScreen) {
        if(isHovered){
            isClicking = true
        }
        if (isClicking) SoundUtils.playSound(SoundUtils.Sound.INTERFACE2, 1.0)
    }

    override fun onMouseRelease(mouseX: Int, mouseY: Int, state: Int, mainGui: GuiScreen) {
        if (isClicking) {
            isClicking = false
            runAction(mainGui)
        }
    }

    private fun runAction(mainGuiIn: GuiScreen) {
        when (action) {
            MainMenu.IconAction.Next -> {
                songManager.skip()
            }
            MainMenu.IconAction.Previous -> {
                songManager.skip()
            }
            MainMenu.IconAction.Action -> {
                songManager.playOrPause()
            }
            MainMenu.IconAction.Settings -> {
                mc.displayGuiScreen(GuiOptions(mainGuiIn, mc.gameSettings))
            }
        }
    }

}