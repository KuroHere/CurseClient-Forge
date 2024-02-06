package com.curseclient.client.gui.impl.mainmenu.elements.alt

import com.curseclient.client.gui.impl.mainmenu.MainMenu
import com.curseclient.client.gui.impl.mainmenu.elements.button.AltButton
import com.curseclient.client.manager.managers.data.DataManager
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.math.Timer
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.HoverUtils
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.RenderUtils2D.glScissor
import com.curseclient.client.utility.render.StencilUtil
import com.curseclient.client.utility.render.font.FontUtils.drawCentreString
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.graphic.GLUtils
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.shader.RoundedUtil.endBlend
import com.curseclient.client.utility.render.shader.RoundedUtil.startBlend
import com.curseclient.client.utility.render.shader.blur.GaussianBlur
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color

// Lazy to fix (～￣▽￣)～
class AltManager : GuiScreen() {
    private val altsComponents = mutableListOf<AltGui>()
    private val buttons = mutableListOf<AltButton>()
    val sr: ScaledResolution = ScaledResolution(Minecraft.getMinecraft())
    private var accounts: ArrayList<Account> = ArrayList()

    private var isHovered = false
    var clickTimer: Timer = Timer()
    private var isClicking = false
    private var dWheel = 0
    var typing = false
    var altName = ""

    override fun initGui() {
        buttons.clear()

        val width = 90
        val height = 28
        val x = 210
        val y = sr.scaledHeight / 1.73

        buttons.add(
            AltButton(55, 20, width, height, "Back to menu", MainMenu.AltAction.Back),
        )

        val buttonData = listOf(
            "Random nickname" to MainMenu.AltAction.Random,
            "Add" to MainMenu.AltAction.Add,
        )

        buttons.addAll(buttonData.mapIndexed { index, (label, action) ->
            AltButton(x, (y + (index * 35.0)).toInt(), width, height, label, action)
        })
    }


    private var rotationAngle = 0F
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        GlStateManager.disableCull()
        checkMouseWheel()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)

        val color = Color(0x86000000.toInt(), true)
        val color2 = Color(0xE4000000.toInt(), true)
        val color3 = Color(0xE4313131.toInt(), true)

        val halfW = sr.scaledWidth / 1.8f
        val halfH = sr.scaledHeight / 2f

        GaussianBlur.glDoubleDataBlur({
            RectBuilder(Vec2d(40.0, 130.0), Vec2d(sr.scaledWidth / 2.5, sr.scaledHeight / 1.4)).apply {
                color(color)
                outlineColor(Color.DARK_GRAY)
                width(0.8)
                radius(8.0)
                draw()
            }
        }, 7.0F, 2.0F)
        GaussianBlur.glDoubleDataBlur({
            RectBuilder(Vec2d(halfW - 120.0, 120.0), Vec2d(halfH * 3.4, sr.scaledHeight / 1.38)).apply {
                color(color)
                outlineColor(Color.DARK_GRAY)
                width(0.8)
                radius(8.0)
                draw()
            }
        }, 7.0F, 2.0F)

        var altsX = 0
        for (alt in DataManager.alts) {
            altsComponents.add(AltGui((halfW - 115 + altsX + dWheel).toInt(), 225, alt.accountName))
            altsX += 225
        }

        glScissor(halfW - 120, 150F, halfH * 3.4F, sr.scaledHeight / 1.45F, sr)
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        altsComponents.forEach { it.render(mouseX, mouseY) }
        GL11.glDisable(GL11.GL_SCISSOR_TEST)

        Fonts.DEFAULT.drawCentreString("You've successfully logged in with a nickname " + mc.session.username,
            Vec2d(210.0, sr.scaledHeight / 1.37), false, color = Color.WHITE)
        Fonts.DEFAULT.drawCentreString("Accounts manager",
            Vec2d(210.0, 150.0), false, Color.WHITE, 2.0)
        Fonts.DEFAULT.drawCentreString("List of accounts",
            Vec2d(halfW + 140.0, 135.0), false, Color.WHITE, 2.0)

        if (HoverUtils.isHovered(Vec2d(mouseX, mouseY), Vec2d(80, 170), Vec2d(280, 30))) {
            if (isHovered) {
                isHovered = false
            }
        } else {
            if (!isHovered) {
                isHovered = true
            }
        }

        if (isClicking) {
            typing = !typing
        }

        val centerX = 80 + (330 - 79) / 2.0
        val centerY = 189 + (226 - 189) / 2.0

        GlStateManager.pushMatrix()
        startBlend()
        StencilUtil.initStencilToWrite()
        RenderUtils2D.drawRoundedRect(79F, 189F, 331F, 226F, 4F, Color.WHITE.rgb)
        StencilUtil.readStencilBuffer(1)
        endBlend()
        GlStateManager.pushMatrix()
        GlStateManager.translate(centerX, centerY, 0.0)

        if (isHovered)
            rotationAngle += 1F
        GlStateManager.rotate(MathUtils.calculateRotation(rotationAngle), 0.0f, 0.0f, 1.0f)
        GlStateManager.translate(-centerX, -centerY, 0.0)
        RectBuilder(Vec2d(75, 189), Vec2d(335, 226)).apply {
            colorV(HUD.getColor(0), HUD.getColor(5))
            radius(4.0)
            draw()
        }
        GlStateManager.popMatrix()
        StencilUtil.uninitStencilBuffer()
        GlStateManager.popMatrix()
        GaussianBlur.glDoubleDataBlur({
            RectBuilder(Vec2d(80, 190), Vec2d(330, 225)).apply {
                color(Color(35, 35, 35, 200))
                radius(4.0)
                draw()
            }
        }, 20F, 5F)
        Fonts.NUNITO_BOLD.drawString("User name",
            Vec2d(85, 180), false, Color.WHITE.setAlpha(210), 1.5)
        Fonts.NUNITO_LIGHT.drawString(altName + (if (typing) (if (System.currentTimeMillis() % 3000 > 1000) "_" else "") else ""),
            Vec2d(100, 210), false, Color.WHITE, 2.0)

        RenderUtils2D.drawGradientOutline(Vec2d(60.0, 165.0), Vec2d(370.0, 165.0), 1F, HUD.getColor(0), HUD.getColor(5))
        RenderUtils2D.drawGradientOutline(Vec2d(450.0, 150.0), Vec2d(900.0, 150.0), 1F, HUD.getColor(5), HUD.getColor(0))

        drawButtons(mouseX, mouseY, partialTicks, this)
        buttons.forEach { it.onDraw(mouseX, mouseY, partialTicks, this) }
        altsComponents.clear()
    }

    override fun keyTyped(chr: Char, keyCode: Int) {
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
            mc.displayGuiScreen(MainMenu())
        if (typing) {
            when (keyCode) {
                Keyboard.KEY_ESCAPE -> return
                Keyboard.KEY_RETURN -> {
                    if (altName.isNotEmpty()) {
                        accounts.add(Account(altName))
                        AltGui.saveUserAvatar("https://minotar.net/helm/$altName/16.png", altName)
                        altName = ""
                        typing = false
                    }
                }
                Keyboard.KEY_BACK -> {
                    if (altName.isNotEmpty()) {
                        altName = altName.substring(0, altName.length - 1)
                    }
                }
            }
            if (ChatAllowedCharacters.isAllowedCharacter(chr)) {
                altName += chr
            }
        }
    }

    private fun drawButtons(mouseX: Int, mouseY: Int, partialTicks: Float, screen: GuiScreen) {
        buttons.forEach { it.onDraw(mouseX, mouseY, partialTicks, screen) }
    }

    override fun mouseClicked(x: Int, y: Int, mouseButton: Int) {
        buttons.forEach { it.onMouseClick(x, y, mouseButton, this) }
        if (isHovered)
            isClicking = true
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        buttons.forEach { it.onMouseRelease(mouseX, mouseY, state, this) }
        if (isClicking)
            isClicking = false
    }

    private fun checkMouseWheel() {
        val dWheel = Mouse.getDWheel()
        val time = GLUtils.deltaTimeInt()
        when {
            dWheel < 0 -> this.dWheel -= 10 - time
            dWheel > 0 -> this.dWheel += 10 + time
        }
    }
}