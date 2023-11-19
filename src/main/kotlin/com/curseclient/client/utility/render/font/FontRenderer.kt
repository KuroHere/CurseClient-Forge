package com.curseclient.client.utility.render.font

import com.curseclient.client.event.listener.tryGetOrNull
import com.curseclient.client.module.modules.client.FontSettings
import com.curseclient.client.utility.render.ColorUtils.glColor
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.math.MathHelper
import net.minecraft.util.text.TextFormatting
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.awt.Font


object FontRenderer {

    private var fontGlyphs = HashMap<Fonts, FontGlyphs>()
    private var currentColor = Color.WHITE

    private val fallbackFonts = arrayOf(
        "Noto Sans JP", "Noto Sans CJK JP", "Noto Sans CJK JP", "Noto Sans CJK KR", "Noto Sans CJK SC", "Noto Sans CJK TC",
        "Source Han Sans", "Source Han Sans HC", "Source Han Sans SC", "Source Han Sans TC", "Source Han Sans K",
        "MS Gothic", "Meiryo", "Yu Gothic",
        "Hiragino Sans GB W3", "Hiragino Kaku Gothic Pro W3", "Hiragino Kaku Gothic ProN W3", "Osaka",
        "TakaoPGothic", "IPAPGothic"
    )

    fun reloadFonts() {
        FontSettings.updateSystemFonts()

        fontGlyphs.apply {
            Fonts.values().forEach { font ->
                this[font]?.destroy()
                this[font] = loadFont(font)
            }
        }
    }

    private fun loadFont(font: Fonts): FontGlyphs {
        val mainFont = tryGetOrNull {
            val path = "/assets/curseclient/fonts/" + font.path + ".ttf"
            val inputStream = javaClass.getResourceAsStream(path)!!
            Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(64.0f)
        } ?: Font("SansSerif", Font.PLAIN, 64)

        val fallbackFont = tryGetOrNull {
            Font(fallbackFonts.firstOrNull { FontSettings.availableFonts.contains(it) }, Font.PLAIN, 64)
        } ?: Font("SansSerif", Font.PLAIN, 64)

        return FontGlyphs(mainFont, fallbackFont)
    }

    fun drawString(text: String, posXIn: Float, posYIn: Float, shadow: Boolean, colorIn: Color, scale: Float, style: Fonts) {
        var posX = 0.0
        var posY = 0.0

        GlStateManager.disableOutlineMode()
        GlStateManager.enableTexture2D()
        GlStateManager.disableAlpha()
        glDisable(GL_ALPHA_TEST)
        GlStateManager.enableBlend()
        glPushMatrix()
        glTranslatef(posXIn, posYIn, 0.0f)
        glScalef(FontSettings.size * scale, FontSettings.size * scale, 1.0f)
        glTranslated(0.0, FontSettings.baselineOffset, 0.0)
        GlStateManager.resetColor()

        val currentFontGlyphs = fontGlyphs[style]!!
        var lastChunk: GlyphChunk? = null
        var lastColor: Color? = null

        text.toCharArray().forEachIndexed { index, char ->
            if (checkColorCode(text, index)) return@forEachIndexed

            if (char == '\n') {
                posY += currentFontGlyphs.fontHeight * FontSettings.lineSpace
                posX = 0.0
                return@forEachIndexed
            }

            val chunk = currentFontGlyphs.getChunk(char)
            val charInfo = currentFontGlyphs.getCharInfo(char)
            val color = if (currentColor == Color.WHITE) colorIn else currentColor

            if (chunk != lastChunk) {
                chunk.use(FontSettings.lodBias.toFloat())
                lastChunk = chunk
            }

            if (lastColor != color) {
                color.glColor()
                lastColor = color
            }

            if (shadow) {
                getShadowColor(color).glColor()

                val shift = FontSettings.shadowShift
                drawQuad(posX + shift, posY + shift, charInfo)

                color.glColor()
            }

            drawQuad(posX, posY, charInfo)
            posX += charInfo.width + FontSettings.gap
        }

        glPopMatrix()
        GlStateManager.enableAlpha()
        GlStateManager.resetColor()
    }

    private fun getShadowColor(color: Color) = Color(
        (color.red * 0.2f).toInt(),
        (color.green * 0.2f).toInt(),
        (color.blue * 0.2f).toInt(),
        (color.alpha * 0.9f).toInt()
    )

    private fun drawQuad(posX: Double, posY: Double, charInfo: CharInfo) {
        glBegin(GL_QUADS)
        glTexCoord2d(charInfo.u1, charInfo.v1)
        glVertex3d(posX, posY, 0.0)

        glTexCoord2d(charInfo.u1, charInfo.v2)
        glVertex3d(posX, posY + charInfo.height, 0.0)

        glTexCoord2d(charInfo.u2, charInfo.v2)
        glVertex3d(posX + charInfo.width, posY + charInfo.height, 0.0)

        glTexCoord2d(charInfo.u2, charInfo.v1)
        glVertex3d(posX + charInfo.width, posY, 0.0)
        glEnd()
    }

    fun getFontHeight(style: Fonts, scale: Float = 1f) =
        (fontGlyphs[style]!!.fontHeight * FontSettings.lineSpace * scale).toFloat()

    fun getStringWidth(text: String, style: Fonts, scale: Float = 1f): Float {
        val font = fontGlyphs[style]!!

        val width = text.toCharArray().mapIndexed { index, char ->
            if (checkColorCode(text, index, false)) return@mapIndexed null
            font.getCharInfo(char).width + FontSettings.gap
        }.filterNotNull().sum()

        return (width * FontSettings.size * scale).toFloat()
    }

    private fun checkColorCode(text: String, index: Int, changeColor: Boolean = true): Boolean {
        if (text.getOrNull(index - 1) == '§') return true

        if (text.getOrNull(index) == '§') {
            val nextChar = text.getOrNull(index + 1)
            if (changeColor) updateColor(nextChar)?.let { currentColor = it }
            return true
        }

        return false
    }

    private fun updateColor(char: Char?): Color? {
        return when (char) {
            TextFormatting.BLACK.toString()[1] -> Color(0, 0, 0)
            TextFormatting.DARK_BLUE.toString()[1] -> Color(0, 0, 170)
            TextFormatting.DARK_GREEN.toString()[1] -> Color(0, 170, 0)
            TextFormatting.DARK_AQUA.toString()[1] -> Color(0, 170, 170)
            TextFormatting.DARK_RED.toString()[1] -> Color(170, 0, 0)
            TextFormatting.DARK_PURPLE.toString()[1] -> Color(170, 0, 170)
            TextFormatting.GOLD.toString()[1] -> Color(250, 170, 0)
            TextFormatting.GRAY.toString()[1] -> Color(170, 170, 170)
            TextFormatting.DARK_GRAY.toString()[1] -> Color(85, 85, 85)
            TextFormatting.BLUE.toString()[1] -> Color(85, 85, 255)
            TextFormatting.GREEN.toString()[1] -> Color(85, 255, 85)
            TextFormatting.AQUA.toString()[1] -> Color(85, 255, 255)
            TextFormatting.RED.toString()[1] -> Color(255, 85, 85)
            TextFormatting.LIGHT_PURPLE.toString()[1] -> Color(255, 85, 255)
            TextFormatting.YELLOW.toString()[1] -> Color(255, 255, 85)
            TextFormatting.WHITE.toString()[1], TextFormatting.RESET.toString()[1] -> Color.WHITE
            else -> null
        }
    }
}
