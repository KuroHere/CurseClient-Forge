package com.curseclient.client.gui.impl.mainmenu.api

import baritone.api.utils.Helper.mc
import com.curseclient.client.Client
import com.curseclient.client.gui.impl.mainmenu.elements.button.MenuButton
import com.curseclient.client.gui.impl.mainmenu.elements.button.IconButton
import com.curseclient.client.gui.impl.particles.mouse.OsuLightTrail
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.shader.RoundedUtil
import com.curseclient.client.utility.render.shader.RoundedUtil.drawImage
import com.curseclient.client.utility.render.shader.gradient.GradientUtil
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.sound.Song
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color

class GuiManager {
    private val trailMang: OsuLightTrail = OsuLightTrail()

    fun drawScreenComponents(buttons: List<MenuButton>, iconButtons: List<IconButton>, mouseX: Int, mouseY: Int, partialTicks: Float, screen: GuiScreen) {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glPushMatrix()
        musicElement()
        GL11.glPopMatrix()
        GL11.glDisable(GL11.GL_BLEND)

        buttons.forEach { it.onDraw(mouseX, mouseY, partialTicks, screen) }
        iconButtons.forEach { it.onDraw(mouseX, mouseY, partialTicks, screen) }

        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glPushMatrix()
        drawLogo(screen)
        GL11.glPopMatrix()
        GL11.glDisable(GL11.GL_BLEND)

        trailMang.addToTrail(mouseX.toDouble(), mouseY.toDouble())
        trailMang.renderTrail()
    }

    private fun musicElement() {
        val sr = ScaledResolution(mc)
        val fr = Fonts.DEFAULT
        val radius = 5f

        RenderUtils2D.drawBlurredRect(Vec2d(sr.scaledWidth / 1.2, 5.0), Vec2d(sr.scaledWidth / 1.04, 32.0), 5, Color.WHITE)
        RectBuilder(Vec2d(sr.scaledWidth / 1.2, 5.0), Vec2d(sr.scaledWidth / 1.04, 32.0)).apply {
            outlineColor(Color.WHITE)
            width(1.0)
            color(Color(15, 15, 15, 255))
            radius(radius.toDouble())
            draw()
        }
        val albumCovers = mapOf(
            "tiredofproblems" to "tiredofproblems.png",
            "axolotl" to "axolotl.png",
            "cant-slow-me-down" to "can'tslowmedown.jpg",
            "morsmordre" to "morsmordre.jpg",
            "aria-math" to "ariamath.jpg",
            "heathens" to "heathens.jpg"
        )

        val albumCover = albumCovers[Song.song_name]

        GlStateManager.resetColor()

        albumCover?.let {
            val currentAlbumCover = ResourceLocation("sounds/images/$it")
            mc.textureManager.bindTexture(currentAlbumCover)
            GL11.glEnable(GL11.GL_BLEND)
            RoundedUtil.drawRoundTextured(sr.scaledWidth / 1.2F + 10, 10F, 16F, 16F, 6F, 1F)
        }
        fr.drawString((Song.song_name ?: "").uppercase(),
            Vec2d(sr.scaledWidth / 1.16 + 5, 14.0),
            shadow = false,
            color = ColorUtils.pulseColor(Color.WHITE, 255, 1),
            scale = 1.0
        )
    }

    private fun drawLogo(screen: GuiScreen) {
        val sr = ScaledResolution(mc)
        val fr = Fonts.OCR_A
        val x = sr.scaledWidth / 2.0
        val color1 = HUD.getColor(0).setAlpha(255)
        val color2 = HUD.getColor(10).setAlpha(255)
        val client = "Curse Client"
        val version = "Beta ${Client.VERSION} - InDev."

        drawTextWithGradient(fr, x, client, version, color1, color2, screen)
        drawLineAndShadow(fr, x, sr, client, color1, color2)
        drawImage("textures/icons/logo/menu1.png", 4F, -15F, 1000 / 4.6F, 394 / 4.6F, Color.WHITE)
        GradientUtil.applyGradientCornerLR(4F, -15F, 1000 / 5F, 394 / 4.6F, 1f, color1, color2) {
            drawImage("textures/icons/logo/menu2.png", 4F, -15F, 1000 / 4.6F, 394 / 4.6F, Color.WHITE)
        }
        drawImage("textures/icons/logo/menu3.png"
            , 4F, -15F, 1000 / 4.6F, 394 / 4.6F, Color.WHITE
        )
    }

    private fun drawTextWithGradient(fr: Fonts, x: Double, client: String, version: String, color1: Color, color2: Color, screen: GuiScreen) {
        GlStateManager.resetColor()
        GradientUtil.applyGradientHorizontal(x.toFloat(), 30f, fr.getStringWidth(client, 7.9).toFloat(), fr.getHeight(8.1).toFloat(), 1f, color1, color2) {
            RoundedUtil.setAlphaLimit(0f)
            fr.drawString(client, Vec2d(x - 65, 100.0), false, color = Color.WHITE, scale = 9.01)
        }
        Fonts.DEFAULT.drawString(version, Vec2d(fr.getStringWidth(client, 8.25).toInt() + x - 130, 140.0),
            shadow = false, color = Color.WHITE.setAlpha(235), scale = 1.81)
    }

    private fun drawLineAndShadow(fr: Fonts, x: Double, sr: ScaledResolution, logo: String, color1: Color, color2: Color) {
        val lineStart = Vec2d(x - 56, 125.0)
        val lineEnd = Vec2d(x + fr.getStringWidth(logo, 7.8).toInt(), 125.0)

        RenderUtils2D.drawGradientOutline(lineStart, lineEnd, 5f, color1, color2)
        RenderUtils2D.drawGradientOutline(Vec2d(0, sr.scaledHeight), Vec2d(sr.scaledWidth, sr.scaledHeight - 1), 6F, Color.WHITE, Color.WHITE)
    }
}