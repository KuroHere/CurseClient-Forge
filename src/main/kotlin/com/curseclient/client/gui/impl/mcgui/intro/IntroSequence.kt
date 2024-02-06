package com.curseclient.client.gui.impl.mcgui.intro

import com.curseclient.CurseClient
import com.curseclient.client.gui.impl.mainmenu.MainMenu
import com.curseclient.client.utility.math.Timer
import com.curseclient.client.utility.render.animation.animaions.Rise6Animation
import com.curseclient.client.utility.render.animation.ease.EaseUtils
import com.curseclient.client.utility.render.shader.RoundedUtil.drawImage
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.io.File

class IntroSequence : GuiScreen() {
    private val logoAnimation = Rise6Animation(EaseUtils.EaseType.InOutCubic, 3000)
    private val timeTracker = Timer()

    override fun initGui() {
        logoAnimation.reset()
        timeTracker.reset()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)
        Gui.drawRect(0, 0, sr.scaledWidth, sr.scaledHeight, Color.BLACK.rgb)

        logoAnimation.run(if (timeTracker.getElapsedTime() > 2800) 0.0 else 255.0)
        resetColor()
        drawImage(ResourceLocation("textures/icons/logo/splash.png"), ((sr.scaledWidth / 2.0 - 75).toFloat()),
            (sr.scaledHeight / 2.0 - 25).toInt().toFloat(), 150F, 50F, Color.WHITE, logoAnimation.value.toFloat())

        if (timeTracker.finished(5000)) {
            // First Launch detect
            val firstLaunchFile = File(CurseClient.DIR, "first_launch.txt")
            if (!firstLaunchFile.exists()) {
                firstLaunchFile.createNewFile()
                mc.displayGuiScreen(PrereleaseDisclaimer())
            } else {
                mc.displayGuiScreen(MainMenu())
            }
        }
    }
}