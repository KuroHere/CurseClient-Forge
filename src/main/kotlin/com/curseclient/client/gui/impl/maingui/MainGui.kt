package com.curseclient.client.gui.impl.maingui

import com.curseclient.CurseClient
import com.curseclient.client.Client
import com.curseclient.client.gui.impl.maingui.elements.GeneralButton
import com.curseclient.client.gui.impl.maingui.elements.MusicButton
import com.curseclient.client.gui.impl.particles.mouse.OsuLightTrail
import com.curseclient.client.manager.managers.SongManager
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.module.modules.client.MenuShader
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.shader.GradientUtil
import com.curseclient.client.utility.render.shader.RoundedUtil
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.misc.Song.Companion.song_name
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color


class MainGui: GuiScreen() {

    private val buttons = ArrayList<GeneralButton>()
    private val musicButton = ArrayList<MusicButton>()
    private val songManager = SongManager
    private var scaledResolution: ScaledResolution? = null
    private val trailMang: OsuLightTrail = OsuLightTrail()
    override fun initGui() {

        this.playMusic()

        updateMusicButtons(ScaledResolution(Minecraft.getMinecraft()))
        updateButtons(ScaledResolution(Minecraft.getMinecraft()))

        MenuShader.initTime = System.currentTimeMillis()

    }

    private fun playMusic() {

        if (!mc.soundHandler.isSoundPlaying(CurseClient.songManager.menuSong)) {
            mc.soundHandler.playSound(CurseClient.songManager.menuSong)
        }
    }

    private fun updateMusicButtons(sr: ScaledResolution) {
        this.scaledResolution = sr
        musicButton.clear()

        val width = 10
        val height = 10
        val x = sr.scaledWidth / 10
        val y = sr.scaledHeight - 20

        val buttonData = listOf(
            MusicAction.Action to x,
            MusicAction.Next to x + 15,
            MusicAction.Previous to x - 15
        )

        musicButton.addAll(buttonData.map { (action, xOffset) ->
            MusicButton(xOffset, y, width, height, action, songManager)
        })
    }

    private fun updateButtons(sr: ScaledResolution) {
        this.scaledResolution = sr
        buttons.clear()

        val width = 220
        val height = 30
        val x = (sr.scaledWidth / 10) + 10
        val y = sr.scaledHeight / 2

        val buttonData = listOf(
            "SinglePlayer" to EnumAction.SinglePlayer,
            "MultiPlayer" to EnumAction.MultiPlayer,
            "Alts Manager" to EnumAction.AltManager,
            "Settings" to EnumAction.Settings,
            "Exit" to EnumAction.Exit
        )

        buttons.addAll(buttonData.mapIndexed { index, (label, action) ->
            GeneralButton(x, y + (index * 35) - 30, width, height, label, action)
        })
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()

        musicElement()

        musicButton.forEach { it.onDraw(mouseX, mouseY, partialTicks, this) }
        buttons.forEach { it.onDraw(mouseX, mouseY, partialTicks, this) }

        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glPushMatrix()

        credit()

        GL11.glPopMatrix()
        GL11.glDisable(GL11.GL_BLEND)

        trailMang.addToTrail(mouseX.toDouble(), mouseY.toDouble())
        trailMang.renderTrail()

        // Text Particle
        //particleMang.addParticle(mouseX.toDouble(), mouseY.toDouble())
        //particleMang.renderParticles(mouseX, mouseY)
    }

    private fun musicElement() {
        val sr = ScaledResolution(mc)
        val rectColor = Color(15, 15, 15, 255)
        val roundedRectColor = rectColor.rgb
        val radius = 5f

        RenderUtils2D.drawBlurredRect(Vec2d(0, sr.scaledHeight - 50), Vec2d(sr.scaledWidth / 6, sr.scaledHeight), 15, rectColor)
        RenderUtils2D.drawRoundedRect(0f, sr.scaledHeight.toFloat() - 50f, sr.scaledWidth.toFloat() / 6, sr.scaledHeight.toFloat(), radius, roundedRectColor, false)

        val albumCovers = mapOf(
            "tiredofproblems" to "tiredofproblems.png",
            "axolotl" to "axolotl.png",
            "cant-slow-me-down" to "can'tslowmedown.jpg",
            "gravity-falls" to "gravityfalls.jpg",
            "aria-math" to "ariamath.jpg"
        )

        val albumCover = albumCovers[song_name]

        resetColor()

        albumCover?.let {
            val currentAlbumCover = ResourceLocation("sounds/images/$it")
            mc.textureManager.bindTexture(currentAlbumCover)
            GL11.glEnable(GL11.GL_BLEND)
            RoundedUtil.drawRoundTextured(10f, sr.scaledHeight.toFloat() - 40, 35f, 35f, 7.5f, 1f)
        }
    }

    private fun credit() {
        val sr = ScaledResolution(mc)
        val fr = Fonts.OCR_A
        val frd = Fonts.DEFAULT
        val songCredits = mapOf(
            "tiredofproblems" to  "NUEKI, TOLCHONOV, glichery",
            "axolotl" to "C418",
            "gravity-falls" to "Gravity Falls Series",
            "aria-math" to "C418",
            "cant-slow-me-down" to "MIRANI, IIIBOI, GROOVYROOM"
        )

        val sn = song_name ?: ""
        val sc = songCredits[song_name] ?: ""

        // Draw song name and credits
        drawSongAndCredits(frd, sr, sn, sc)

        val x = sr.scaledWidth / 11
        val fontColor1 = HUD.getColor(0).setAlpha(255)
        val fontColor2 = HUD.getColor(10).setAlpha(255)
        val logo = "Curse Client"
        val cheat = "Best 1.12.2 cheat"
        val version = "Version ${Client.VERSION} Beta"

        // Dont know why I'm doing that ?
        val love = "Made by KuroHere with an abundance of love ♥."
        val firstPartOfLove = love.substring(0, 43) // "Made by KuroHere with an abundance of love"
        val secondPartOfLove = love.substring(43)   // "♥"

        // Draw gradient, logo, cheat, version, line, and shadow
        drawTextWithGradient(fr, x, logo, cheat, version, firstPartOfLove, secondPartOfLove, fontColor1, fontColor2)
        drawLineAndShadow(fr, x, logo, fontColor1, fontColor2)

        val creditInfo = "Copyright Mojang AB. Do not distribute!"
        val creditPos = Vec2d(width - 3.0 - Fonts.OCR_A.getStringWidth(creditInfo, 1.4), (height - 12.0))

        fr.drawString(creditInfo, creditPos, scale = 1.4)
    }

    private fun drawSongAndCredits(fr: Fonts, sr: ScaledResolution, sn: String, sc: String) {
        fr.drawString(sn.uppercase(), Vec2d((sr.scaledWidth / 16), sr.scaledHeight - 45),
            color = ColorUtils.pulseColor(Color.WHITE, 255, 1), scale = 1.0)
        fr.drawString(sc, Vec2d((sr.scaledWidth / 16), sr.scaledHeight - 38),
            color = Color.WHITE, scale = 0.6)
    }

    private fun drawTextWithGradient(fr: Fonts, x: Int, logo: String, cheat: String, version: String, love1: String, love2: String, fontColor1: Color, fontColor2: Color) {
        resetColor()
        GradientUtil.applyGradientHorizontal(x.toFloat(), 30f, x + fr.getStringWidth(logo, 8.1).toFloat() + fr.getStringWidth(version, 1.1).toFloat(), 30f + fr.getHeight(8.1).toFloat(), 1f, fontColor1, fontColor2) {
            RoundedUtil.setAlphaLimit(0f)
            fr.drawString(logo, Vec2d(x - 67, 67), false, color = Color.WHITE, scale = 8.1)
            fr.drawString(version, Vec2d(fr.getStringWidth(logo, 8.1).toInt() + x - 72, 37),
                shadow = false, color = Color.WHITE, scale = 1.1)
        }
        fr.drawString(love1, Vec2d((width.toFloat() / 2.0f - fr.getStringWidth(love1, 0.7) / 2), height.toDouble() - fr.getHeight(0.7)), color =  Color(255, 255, 255, 100), scale = 0.7)
        resetColor()
        // I Hate x y z width height, so it just a two gradient color mix together and make pink color...
        GradientUtil.applyGradientCornerLR((width.toFloat() / 2.0f + fr.getStringWidth(love1, 0.7) / 2).toFloat(), 0f, (width.toFloat() / 2.0f + fr.getStringWidth(love1, 0.7)).toFloat(), (height - fr.getHeight(0.7)).toFloat(), 1f, Color.RED, Color.white) {
            RoundedUtil.setAlphaLimit(0f)
            fr.drawString(love2, Vec2d((width.toFloat() / 2.0f + fr.getStringWidth(love1, 0.7) / 2), height.toDouble() - fr.getHeight(0.7)), color = Color(255, 255, 255, 100), scale = 0.7)
        }
        fr.drawString(cheat, Vec2d(fr.getStringWidth(logo, 7.6).toInt() + x - 153, 107),
            shadow = false, color = Color.WHITE, scale = 1.6)
    }

    private fun drawLineAndShadow(fr: Fonts, x: Int, logo: String, fontColor1: Color, fontColor2: Color) {
        val lineStart = Vec2d(x - 70, 92)
        val lineEnd = Vec2d(x + fr.getStringWidth(logo, 7.2).toInt(), 92)

        RenderUtils2D.drawGradientRect(lineStart, lineEnd, fontColor1, fontColor2, fontColor1, fontColor2)
        RenderUtils2D.drawLine(lineStart, lineEnd, 3f, fontColor1)
        RenderUtils2D.drawBlurredShadow(x - 73f, 91f, x + fr.getStringWidth(logo, 7.0).toFloat(), 3f, 15, fontColor1)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for(button in buttons){
            button.onMouseClick(mouseX, mouseY, mouseButton, this)
        }
        for(musicButton in musicButton)
            musicButton.onMouseClick(mouseX, mouseY, mouseButton, this)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        for(button in buttons){
            button.onMouseRelease(mouseX, mouseY, state, this)
        }
        for(musicButton in musicButton){
            musicButton.onMouseRelease(mouseX, mouseY, state, this)
        }
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    enum class MusicAction{
        Next,
        Previous,
        Action
    }

    enum class EnumAction{
        SinglePlayer,
        MultiPlayer,
        AltManager,
        Settings,
        Exit
    }
}