package com.curseclient.client.gui.impl.mainmenu.elements.radio

import com.curseclient.client.manager.managers.RadioManager
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.resources.I18n
import org.lwjgl.input.Keyboard
import java.io.IOException

// it's work only in ./gradlew runclient
// which is having ssl access
// IDK why my Gradle not download it when ./gradlew build,
// so this is why I won't work on it anymore
class RadioGUI(private val previous: GuiScreen) : GuiScreen() {
    private var nameOrId: GuiTextField? = null

    override fun initGui() {
        Keyboard.enableRepeatEvents(true)
        buttonList.clear()
        buttonList.add(GuiButton(12, width / 2, height / 5 + 60, 136, 22, "Stop"))
        buttonList.add(GuiButton(11, width / 2 - 138, height / 5 + 60, 136, 22, "Play"))

        buttonList.add(GuiButton(0, width - 136, 0, 136, 22, "MONSTERCAT"))
        buttonList.add(GuiButton(1, width - 136, 21, 136, 22, "CHILLHOP"))
        buttonList.add(GuiButton(8, width - 136, 42, 136, 22, "HIP HOP"))
        buttonList.add(GuiButton(3, width - 136, 63, 136, 22, "DANCE"))
        buttonList.add(GuiButton(4, width - 136, 84, 136, 22, "CHILL"))
        buttonList.add(GuiButton(5, width - 136, 105, 136, 22, "XMAS"))
        buttonList.add(GuiButton(6, width - 136, 126, 136, 22, "CLUB"))
        buttonList.add(GuiButton(7, width - 136, 147, 136, 22, "RAP"))

        buttonList.add(GuiButton(2, width / 2 - 150 / 2, height / 6 + 100, 146, 22, I18n.format("gui.done")))
        nameOrId = GuiTextField(0, fontRenderer, width / 2 - 100, height / 5 + 30, 200, 20).apply {
            text = ""
            maxStringLength = Int.MAX_VALUE
        }
    }

    override fun updateScreen() {
        nameOrId?.updateCursorCounter()
    }

    @Throws(IOException::class)
    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            11 -> {
                val URL = nameOrId?.text ?: ""
                if (URL.isNotEmpty() && URL.contains("streams.ilovemusic.de")) {
                    RadioManager.setStream(java.net.URL(URL).openStream())
                    RadioManager.start()
                }
            }

            0 -> {
                RadioManager.stop()
                RadioManager.setStream(java.net.URL("https://streams.ilovemusic.de/iloveradio24.mp3").openStream())
                RadioManager.start()
            }

            1 -> {
                RadioManager.stop()
                RadioManager.setStream(java.net.URL("https://streams.ilovemusic.de/iloveradio17.mp3").openStream())
                RadioManager.start()
            }

            8 -> {
                RadioManager.stop()
                RadioManager.setStream(java.net.URL("https://streams.ilovemusic.de/iloveradio3.mp3").openStream())
                RadioManager.start()
            }

            3 -> {
                RadioManager.stop()
                RadioManager.setStream(java.net.URL("https://streams.ilovemusic.de/iloveradio2.mp3").openStream())
                RadioManager.start()
            }

            4 -> {
                RadioManager.stop()
                RadioManager.setStream(java.net.URL("https://streams.ilovemusic.de/iloveradio1.mp3").openStream())
                RadioManager.start()
            }

            5 -> {
                RadioManager.stop()
                RadioManager.setStream(java.net.URL("https://streams.ilovemusic.de/iloveradio8.mp3").openStream())
                RadioManager.start()
            }

            6 -> {
                RadioManager.stop()
                RadioManager.setStream(java.net.URL("https://streams.ilovemusic.de/iloveradio20.mp3").openStream())
                RadioManager.start()
            }

            7 -> {
                RadioManager.stop()
                RadioManager.setStream(java.net.URL("https://streams.ilovemusic.de/iloveradio13.mp3").openStream())
                RadioManager.start()
            }

            12 -> RadioManager.stop()
            2 -> mc.displayGuiScreen(previous)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        nameOrId?.textboxKeyTyped(typedChar, keyCode)
        super.keyTyped(typedChar, keyCode)
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        nameOrId?.mouseClicked(mouseX, mouseY, mouseButton)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        nameOrId?.drawTextBox()

        mc.fontRenderer.drawString("URL", width / 2, nameOrId?.y?.minus(20) ?: 0, -1)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    companion object {
        fun withColors(identifier: String, input: String): String {
            var output = input
            var index = output.indexOf(identifier)
            while (output.indexOf(identifier) != -1) {
                output = output.replace(identifier, "\u00A7")
                index = output.indexOf(identifier)
            }
            return output
        }
    }
}