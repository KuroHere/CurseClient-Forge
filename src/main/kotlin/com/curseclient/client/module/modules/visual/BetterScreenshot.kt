package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.events.render.PreScreenshotEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.utility.NotificationType
import com.curseclient.client.utility.NotificationUtils
import com.curseclient.client.utility.getTimestampedPNGFileForDirectory
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.threads.backgroundThread
import com.curseclient.client.utility.threads.mainThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ScreenShotHelper
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.text.event.ClickEvent
import net.minecraftforge.client.ForgeHooksClient
import org.lwjgl.input.Mouse
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object BetterScreenshot : Module(
    "BetterScreenshot",
    "Allows you to select the area you want to screenshot",
    Category.VISUAL
) {

    init {
        safeListener<PreScreenshotEvent> { event ->
            BetterScreenshot.mc.displayGuiScreen(GuiScreenshot)

            event.response = TextComponentString("Taking screenshot...")
            event.cancel()
        }
    }

    private fun screenshot(x: Int, y: Int, width: Int, height: Int): BufferedImage {
        val scaleFac = ScaledResolution(mc).scaleFactor
        return ScreenShotHelper.createScreenshot(
            mc.displayWidth,
            mc.displayHeight,
            mc.framebuffer
        ).getSubimage(x * scaleFac, y * scaleFac, width * scaleFac, height * scaleFac)
    }

    internal object GuiScreenshot : GuiScreen() {

        private var selecting = false

        private var startX = 0
        private var startY = 0

        private var selectionX = 0
        private var selectionY = 0
        private var selectionWidth = 0
        private var selectionHeight = 0

        private val selectionColor = Color(0, 0, 0, 100)

        override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
            mc.ingameGUI.renderGameOverlay(partialTicks) //So you get a preview of what's actually on the ss
            if (!Mouse.isButtonDown(0)) {
                return
            }

            if (selecting) {
                if (mouseX < startX) {
                    selectionX = mouseX
                    selectionWidth = startX - mouseX
                } else {
                    selectionX = startX
                    selectionWidth = mouseX - startX
                }
                if (mouseY < startY) {
                    selectionY = mouseY
                    selectionHeight = startY - mouseY
                } else {
                    selectionY = startY
                    selectionHeight = mouseY - startY
                }

                RenderUtils2D.drawRect(
                    Vec2d(selectionX, selectionY),
                    Vec2d(selectionWidth, selectionHeight),
                    selectionColor
                )
                RenderUtils2D.drawBorder(
                    selectionX.toFloat(),
                    selectionY.toFloat(),
                    selectionWidth.toFloat(),
                    selectionHeight.toFloat(),
                    1F,
                    Color.WHITE
                )
            } else {
                startX = mouseX
                startY = mouseY
                selecting = true
            }
        }

        override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
            if (!selecting) {
                return
            }

            mc.setIngameFocus()
            backgroundThread {
                delay(100) //So the gui is gone when taking the ss
                var screenshot: BufferedImage? = null
                withContext(Dispatchers.IO) {
                    mainThread { //GL context is needed for the method
                        screenshot = screenshot(selectionX, selectionY, selectionWidth, selectionHeight)
                    }.get()
                }
                val event = ForgeHooksClient.onScreenshot(
                    screenshot,
                    getTimestampedPNGFileForDirectory(
                        File(mc.gameDir, "screenshots").also { it.mkdir() }
                    ).canonicalFile
                )
                if (event.isCanceled) {
                    mc.ingameGUI.chatGUI.printChatMessage(event.cancelMessage)
                    return@backgroundThread
                }

                withContext(Dispatchers.IO) {
                    ImageIO.write(screenshot, "png", event.screenshotFile)
                }

                if (event.resultMessage != null) {
                    mc.ingameGUI.chatGUI.printChatMessage(event.resultMessage)
                    return@backgroundThread
                }

                val component = TextComponentString(event.screenshotFile.name)
                component.style.clickEvent = ClickEvent(
                    ClickEvent.Action.OPEN_FILE,
                    event.screenshotFile.absolutePath
                )
                component.style.underlined = true
                mc.ingameGUI.chatGUI.printChatMessage(
                    TextComponentTranslation(
                        "screenshot.success", component
                    )
                )
            }
        }

        override fun onGuiClosed() {
            if (!selecting) {
                NotificationUtils.notify("BetterScreenshot", "No screenshot taken!", NotificationType.INFO, descriptionColor = Color.GREEN)
            }

            selecting = false
        }

    }

}