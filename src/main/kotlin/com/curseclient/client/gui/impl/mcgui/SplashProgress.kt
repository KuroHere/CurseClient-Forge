package com.curseclient.client.gui.impl.mcgui

import baritone.api.utils.Helper.mc
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.font.UnicodeFontRenderer
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.shader.RoundedUtil.endBlend
import com.curseclient.client.utility.render.shader.RoundedUtil.startBlend
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.shader.Framebuffer
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.nio.channels.Channels

object SplashProgress {
    private var splash: ResourceLocation? = null
    private var ctm: TextureManager? = null

    private const val MaxProgress = 6
    var Progress = 0
    var Current = ""

    private val tips = listOf(
        "Tip: The default click gui bind is Right Shift.",
        "Tip: Default prefix is \".\" and you can't change that for now.",
        "Tip: Client not work with future client, will be fix in the future.",
        "Tip: In crystal pvp match, focus on your movement ability.",
        "Tip: Not recommend this client for low end pc.",
        "Tip: Delete first_launch.txt in client cfg folder to watch full intro again."
    )

    private val randomTip = tips.random()

    fun update() {
        if (ctm == null) {
            return
        }
        drawSplash(mc.textureManager)
    }

    fun setProgress(givenProgress: Int, givenText: String) {
        Progress = givenProgress
        Current = givenText
        update()
    }

    private var sfr: UnicodeFontRenderer? = null

    fun drawSplash(tm: TextureManager) {
        if (ctm == null) {
            ctm = tm
        }
        val scaledResolution = ScaledResolution(mc)
        val i = scaledResolution.scaleFactor
        val framebuffer = Framebuffer(scaledResolution.scaledWidth * i, scaledResolution.scaledHeight * i, true)
        framebuffer.bindFramebuffer(false)
        GlStateManager.matrixMode(5889)
        GlStateManager.loadIdentity()
        GlStateManager.ortho(0.0, scaledResolution.scaledWidth.toDouble(), scaledResolution.scaledHeight.toDouble(), 0.0, 1000.0, 3000.0)
        GlStateManager.matrixMode(5888)
        GlStateManager.loadIdentity()
        GlStateManager.translate(0.0f, 0.0f, -2000.0f)
        GlStateManager.disableLighting()
        GlStateManager.disableFog()
        GlStateManager.disableDepth()
        GlStateManager.enableTexture2D()

        splash = ResourceLocation("splash.png")
        splash?.let { tm.bindTexture(it) }

        GlStateManager.resetColor()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        Gui.drawScaledCustomSizeModalRect(0, 0, 0F, 0F, 1920, 1080, scaledResolution.scaledWidth, scaledResolution.scaledHeight, 1920F, 1080F)
        drawBarProgress()
        framebuffer.unbindFramebuffer()
        framebuffer.framebufferRender(scaledResolution.scaledWidth * i, scaledResolution.scaledHeight * i)
        GlStateManager.enableAlpha()
        GlStateManager.alphaFunc(516, 0.1f)
        mc.updateDisplay()
    }

    private fun drawBarProgress() {
        if (mc.gameSettings == null) {
            return
        }
        val sr = ScaledResolution(Minecraft.getMinecraft())
        if (sfr == null) {
            sfr = UnicodeFontRenderer.getFontOnPC("SansSerif", 20)
        }
        val nProgress = Progress.toDouble()
        val calc = nProgress / MaxProgress * sr.scaledWidth - 20

        GlStateManager.resetColor()
        resetTextureState()

        sfr?.drawString(Current, 20F, sr.scaledHeight - 35F, Color(-1).rgb)
        val step = "$Progress/$MaxProgress"
        sfr?.drawString(step, (sr.scaledWidth - 20 - sfr!!.getStringWidth(step)).toFloat(), sr.scaledHeight - 35F, Color(-1).rgb)
        sfr?.drawCenteredTextScaled(randomTip, (sr.scaledWidth / 2), sr.scaledHeight - 8, Color(-1).rgb, 0.7)

        startBlend()
        RectBuilder(Vec2d(20, sr.scaledHeight - 70), Vec2d(calc, sr.scaledHeight.toDouble() - 85)).apply {
            color(Color.WHITE)
            radius(5.0)
            draw()
        }

        RectBuilder(Vec2d(20, sr.scaledHeight - 70), Vec2d(sr.scaledWidth.toDouble() - 20, sr.scaledHeight.toDouble() - 85)).apply {
            outlineColor(Color.WHITE.setAlpha(90))
            width(1.0)
            color(Color.WHITE.setAlpha(80))
            radius(5.0)
            draw()
        }
        endBlend()
    }

    private fun resetTextureState() {
        GlStateManager.enableTexture2D()
        GlStateManager.enableAlpha()
        GlStateManager.alphaFunc(352, 0.1f)
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(770, 771)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }
}