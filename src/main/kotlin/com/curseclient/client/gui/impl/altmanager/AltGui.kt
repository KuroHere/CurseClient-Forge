package com.curseclient.client.gui.impl.altmanager

import baritone.api.utils.Helper.mc
import com.curseclient.client.gui.impl.maingui.MainGui
import com.curseclient.client.utility.extension.Timer
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.Font
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.function.Consumer

object AltGui: GuiScreen() {
    private val typingIconTimer: Timer = Timer()
    var altButtons: ArrayList<AltButton> = ArrayList()
    private var email = ""
    private var password:String = ""
    private var e = false 
    private var p:Boolean = false
    private var microsoft:Boolean = false

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val scaledResolution = ScaledResolution(mc)
        val width = scaledResolution.scaledWidth.toFloat()
        val height = scaledResolution.scaledHeight.toFloat()
        val x = width - 150.0f
        val center = x + 75.0f

        // Currently logged in as
        val text = ("Currently logged in as " + mc.session.username) + "."
        Fonts.DEFAULT.drawString(text, Vec2d(width / 2.0f - Fonts.DEFAULT.getStringWidth(text) / 2.0f, height.toDouble() / 2.0f + 20.0f), color = Color.WHITE, scale = 1.0)

        // Background
        Gui.drawRect(x.toInt(), 0, width.toInt(), height.toInt(), Color(14, 14, 14).rgb)

        // Top Text
        Gui.drawRect(x.toInt(), 0, width.toInt(), 20, Color(12, 12, 12).rgb)
        Gui.drawRect(x.toInt(), 19, width.toInt(), 20, Color(20, 20, 20).rgb)
        Fonts.DEFAULT.drawString("Alt Manager", Vec2d(center - Fonts.DEFAULT.getStringWidth("Alt Manager") / 2.0f, 10 - Fonts.DEFAULT.getHeight() / 2.0f), color = Color.WHITE)


        // Buttons background
        Gui.drawRect(x.toInt(), height.toInt() - 85, width.toInt(), height.toInt(), Color(12, 12, 12).rgb)
        Gui.drawRect(x.toInt(), height.toInt() - 85, width.toInt(), height.toInt() - 84, Color(20, 20, 20).rgb)
        val canAdd = password != "" && email != ""

        // Add
        Gui.drawRect(x.toInt() + 5, height.toInt() - 30, x.toInt() + 72, height.toInt() - 10, Color(20, 20, 20).rgb)
        Fonts.DEFAULT.drawString("Add Alt", Vec2d(x + 36.25f - Fonts.DEFAULT.getStringWidth("Add Alt") / 2.0f, height - 20 - Fonts.DEFAULT.getHeight() / 2.0f), color = if (canAdd) Color.WHITE else Color.GRAY)
        if (mouseX > x.toInt() + 5.0f && mouseX < x + 72.5f && mouseY > height.toInt() - 30.0f && mouseY < height - 10.0f) {
            Gui.drawRect(x.toInt() + 5, height.toInt() - 30, x.toInt() + 72, height.toInt() - 10, Color(0, 0, 0, 50).rgb)
        }

        // Microsoft/Cracked
        Gui.drawRect(x.toInt() + 77, height.toInt() - 30, width.toInt() - 5, height.toInt() - 10, if (microsoft) Color.CYAN.rgb else Color.RED.rgb)
        if (mouseX > x + 77.5f && mouseX < width - 5.0f && mouseY > height.toInt() - 30.0f && mouseY < height - 10.0f) {
            Gui.drawRect(x.toInt() + 77, height.toInt() - 30, width.toInt() - 5, height.toInt() - 10, Color(0, 0, 0, 150).rgb)
        }
        Fonts.DEFAULT.drawString(if (microsoft) "Microsoft" else "Cracked", Vec2d(x + 112.5f - Fonts.DEFAULT.getStringWidth(if (microsoft) "Microsoft" else "Cracked") / 2.0f, height - 20 - Fonts.DEFAULT.getHeight() / 2.0f), color =  Color.WHITE)

        // Password
        Gui.drawRect(x.toInt() + 5, height.toInt() - 55, width.toInt() - 5, height.toInt() - 35, Color(20, 20, 20).rgb)
        if (password == "") {
            Fonts.DEFAULT.drawString("Password" + if (p) typingIcon() else "", Vec2d((x + 7.5f).toDouble(), height - 45 - Fonts.DEFAULT.getHeight() / 2.0f), color =  Color(100, 100, 100, 50))
        } else {
            Fonts.DEFAULT.drawString(password + if (p) typingIcon() else "", Vec2d((x + 7.5f).toDouble(), height - 45 - Fonts.DEFAULT.getHeight() / 2.0f), color =  Color.WHITE)
        }

        // Email
        Gui.drawRect(x.toInt() + 5, height.toInt() - 80, width.toInt() - 5, height.toInt() - 60, Color(20, 20, 20).rgb)
        if (email == "") {
            Fonts.DEFAULT.drawString((if (microsoft) "Email" else "Username") + if (e) typingIcon() else "", Vec2d((x + 7.5f).toDouble(), height - 70 - Fonts.DEFAULT.getHeight() / 2.0f), color =  Color(100, 100, 100, 50))
        } else {
            Fonts.DEFAULT.drawString(email + if (e) typingIcon() else "", Vec2d((x + 7.5f).toDouble(), height - 70 - Fonts.DEFAULT.getHeight() / 2.0f), color =  Color.WHITE)
        }

        // Render each alt button
        altButtons.forEach(Consumer { altButton: AltButton ->
            altButton.x = x + 5.0f
            altButton.drawScreen(mouseX, mouseY)
        })
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        when (keyCode) {
            Keyboard.KEY_ESCAPE -> mc.displayGuiScreen(MainGui())
        }
        if (e) {
            email = type(typedChar, keyCode, email)
        } else if (p) {
            password = type(typedChar, keyCode, password)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val scaledResolution = ScaledResolution(mc)
        val width = scaledResolution.scaledWidth.toFloat()
        val height = scaledResolution.scaledHeight.toFloat()
        val x = width - 150.0f
        if (mouseButton == 0) {
            if (password != "" && email != "" && mouseX > x + 5.0f && mouseX < x + 72.5f && mouseY > height - 30.0f && mouseY < height - 10.0f) {
                altButtons.add(AltButton(email, password, if (microsoft) Alt.AltType.MICROSOFT else Alt.AltType.CRACKED, x + 5.0f, 25 + altButtons.size * 50.0f, 140.0f, 45.0f))
                email = ""
                password = ""
            }
            if (mouseX > x + 77.5f && mouseX < width - 5.0f && mouseY > height - 30.0f && mouseY < height - 10.0f) {
                microsoft = !microsoft
                email = ""
                password = ""
            }
            if (mouseX > x + 5.0f && mouseX < width - 5.0f) {
                e = mouseY > height - 80.0f && mouseY < height - 60.0f
                p = mouseY > height - 55.0f && mouseY < height - 35.0f
            }
        }
        ArrayList<AltButton>(altButtons).forEach(Consumer{ altButton: AltButton -> altButton.mouseClicked(mouseX, mouseY, mouseButton) }) //e
    }

    fun updateButtons() {
        val scaledResolution = ScaledResolution(mc)
        val width = scaledResolution.scaledWidth.toFloat()
        val x = width - 150.0f
        val altButtons1: ArrayList<AltButton> = ArrayList<AltButton>()
        for (altButton in altButtons) {
            altButtons1.add(AltButton(altButton.email, altButton.password, altButton.altType, x + 5.0f, 25 + altButtons1.size * 50.0f, 140.0f, 45.0f))
        }
        altButtons = altButtons1
    }

    private fun typingIcon(): String {
        if (typingIconTimer.passedDMs(1000)) {
            typingIconTimer.sync()
        }
        return if (typingIconTimer.passedDMs(500)) {
            ""
        } else "_"
    }

    private fun type(typedChar: Char, keyCode: Int, string: String): String {
        var newString = string
        when (keyCode) {
            14 -> {
                if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                    newString = ""
                }
                if (newString.isNotEmpty()) {
                    newString = newString.substring(0, newString.length - 1)
                }
            }

            27, 28 -> {
                e = false
                p = false
            }

            else -> if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                newString += typedChar

            }
        }
        return newString
    }
}
