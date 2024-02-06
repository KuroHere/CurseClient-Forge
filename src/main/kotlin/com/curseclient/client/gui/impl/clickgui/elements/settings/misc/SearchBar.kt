package com.curseclient.client.gui.impl.clickgui.elements.settings.misc

import baritone.api.utils.Helper.mc
import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.elements.InteractiveElement
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.module.impls.client.ClickGui
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.ColorUtils.toColor
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.animation.animaions.AstolfoAnimation
import com.curseclient.client.utility.render.font.FontUtils.drawCentreString
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.graphic.GLUtils
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.shader.RoundedUtil.drawImage
import com.curseclient.client.utility.render.shader.RoundedUtil.drawRound
import com.curseclient.client.utility.render.shader.RoundedUtil.endBlend
import com.curseclient.client.utility.render.shader.RoundedUtil.startBlend
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.lang.reflect.Field
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class SearchBar(pos: Vec2d,
                width: Double,
                height: Double,
                gui: AbstractGui
) : InteractiveElement(pos, width, height, gui) {

    var field: GuiTextField = GuiTextField(0, mc.fontRenderer, pos.x.toInt() , pos.y.toInt(), width.toInt(), height.toInt())
    var click = false

    init {
        field.apply {
            enableBackgroundDrawing = true
            setMaxStringLength(32)
        }
    }

    fun updateScreen() {
        field.updateCursorCounter()
    }

    override fun onRender() {
        field.x = pos.x.toInt()
        field.y = pos.y.toInt()

        val c1 = if (ClickGui.colorMode == ClickGui.ColorMode.Client)
            HUD.getColor(0)
        else if
            (ClickGui.pulse) ColorUtils.pulseColor(ClickGui.buttonColor1, 0, 1)
        else
            ClickGui.buttonColor1
        val c2 = when (ClickGui.colorMode) {
            ClickGui.ColorMode.Client -> HUD.getColor(5)
            ClickGui.ColorMode.Static -> if (ClickGui.pulse) ColorUtils.pulseColor(ClickGui.buttonColor1, 0, 1) else ClickGui.buttonColor1
            else -> ClickGui.buttonColor2
        }

        GlStateManager.pushMatrix()
        startBlend()
        RenderUtils2D.drawBlurredShadow(field.x.toFloat(), field.y.toFloat(), width.toFloat(), height.toFloat(), 10, c1.darker())
        RectBuilder(Vec2d(field.x, field.y), Vec2d(field.x + width, field.y + height)).apply {
            outlineColor(Color(-1))
            width(1.0)
            if (ClickGui.colorMode == ClickGui.ColorMode.Horizontal)
                colorH(c1, c2)
            else
                colorV(c1, c2)
            radius(2.0)
            draw()
        }

        val flag1 = field.isFocused && count() / 6 % 2 == 0
        if(click && flag1 && field.isFocused) {
            fr.drawString(field.text, Vec2d(field.x.toDouble() + 23, field.y + height - fr.getHeight(1.0) - 2.5), scale = 1.0)
            fr.drawString("_", Vec2d(field.x + 21 + fr.getStringWidth(field.text) + 1.0, field.y + height - fr.getHeight(1.0) - 2.5), color = Color(-1))
        } else {
            fr.drawCentreString("Search for a module...", Vec2d(field.x.toDouble() + width / 2, field.y + height - fr.getHeight(1.0) - 2.5), false, scale = 1.0)
        }
        drawImage(ResourceLocation("textures/icons/search.png"),
            field.x.toFloat(), field.y.toFloat() + 2, 16F, 16F, Color.WHITE)
        RectBuilder(Vec2d(field.x + 17, field.y + 2), Vec2d(field.x + 18.0, field.y + height - 3.0)).apply {
            color(Color.WHITE)
            radius(1.0)
            draw()
        }
        
        endBlend()
        GlStateManager.popMatrix()
    }

    private fun count(): Int {
        return try {
            val f: Field = GuiTextField::class.java.getDeclaredFields()[8]
            f.isAccessible = true
            f.getInt(field)
        } catch (ex: Exception) {
            ex.printStackTrace()
            0
        }
    }

    override fun onMouseAction(action: MouseAction, button: Int) {
        if (action != MouseAction.CLICK || button != 0 || !hovered) return
        if (action == MouseAction.CLICK)
            click = true
        try {
            field.mouseClicked(pos.x.toInt(), pos.y.toInt(), 1)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onGuiOpen() {
        field.text = ""
        field.isFocused = false
        click = false
    }
    override fun onGuiClose() {
        field.text = ""
        field.isFocused = false
        click = false
    }

    override fun onRegister() {}
    override fun onGuiCloseAttempt() {}
    override fun onTick() {}

    override fun onKey(typedChar: Char, key: Int) {
        field.textboxKeyTyped(typedChar, key)
        if (Keyboard.getKeyName(0) == null) return

    }
}