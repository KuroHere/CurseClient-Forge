package com.curseclient.client.utility.render.font

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.StringUtils
import org.lwjgl.opengl.GL11
import org.newdawn.slick.UnicodeFont
import org.newdawn.slick.font.effects.ColorEffect
import java.awt.Font

class UnicodeFontRenderer private constructor(
    font: Font,
    private val kerning: Float,
    private var antiAliasingFactor: Float
) {

    companion object {
        fun getFontOnPC(name: String, size: Int): UnicodeFontRenderer {
            return getFontOnPC(name, size, Font.PLAIN)
        }

        fun getFontOnPC(name: String, size: Int, fontType: Int): UnicodeFontRenderer {
            return getFontOnPC(name, size, fontType, 0f)
        }

        fun getFontOnPC(name: String, size: Int, fontType: Int, kerning: Float): UnicodeFontRenderer {
            return getFontOnPC(name, size, fontType, kerning, 3.0f)
        }

        fun getFontOnPC(name: String, size: Int, fontType: Int, kerning: Float, antiAliasingFactor: Float): UnicodeFontRenderer {
            return UnicodeFontRenderer(Font(name, fontType, size), kerning, antiAliasingFactor)
        }

        fun getFontFromAssets(name: String, size: Int): UnicodeFontRenderer {
            return getFontOnPC(name, size, Font.PLAIN)
        }

        fun getFontFromAssets(name: String, size: Int, fontType: Int): UnicodeFontRenderer {
            return getFontOnPC(name, fontType, size, 0f)
        }

        fun getFontFromAssets(name: String, size: Int, kerning: Float, fontType: Int): UnicodeFontRenderer {
            return getFontFromAssets(name, size, fontType, kerning, 1.0f)
        }

        fun getFontFromAssets(name: String, size: Int, fontType: Int, kerning: Float, antiAliasingFactor: Float): UnicodeFontRenderer {
            return UnicodeFontRenderer(name, fontType, size, kerning, antiAliasingFactor)
        }
    }

    //private val FONT_HEIGHT = 9
    private val colorCodes = IntArray(32)
    private val cachedStringWidth = HashMap<String, Float>()
    private val unicodeFont: UnicodeFont = UnicodeFont(
        Font(font.name, font.style, (font.size * antiAliasingFactor).toInt())
    )

    init {
        unicodeFont.addAsciiGlyphs()
        unicodeFont.effects.add(ColorEffect(java.awt.Color.WHITE))

        try {
            unicodeFont.loadGlyphs()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        for (i in 0 until 32) {
            val shadow = (i shr 3 and 1) * 85
            var red = (i shr 2 and 1) * 170 + shadow
            var green = (i shr 1 and 1) * 170 + shadow
            var blue = (i and 1) * 170 + shadow

            if (i == 6) {
                red += 85
            }

            if (i >= 16) {
                red /= 4
                green /= 4
                blue /= 4
            }
            colorCodes[i] = (red and 255 shl 16) or (green and 255 shl 8) or (blue and 255)
        }
    }

    constructor(name: String, fontType: Int, size: Int, kerning: Float, antiAliasingFactor: Float) :
        this(Font(name, fontType, size), kerning, antiAliasingFactor)

    fun drawStringScaled(text: String, givenX: Int, givenY: Int, color: Int, givenScale: Double) {
        GL11.glPushMatrix()
        GL11.glTranslated(givenX.toDouble(), givenY.toDouble(), 0.0)
        GL11.glScaled(givenScale, givenScale, givenScale)
        drawString(text, 0.0f, 0.0f, color)
        GL11.glPopMatrix()
    }

    fun drawString(text: String, x: Float, y: Float, color: Int): Int {
        var _x = x
        var _y = y

        _x *= 2.0F
        _y *= 2.0F

        val originalX = _x

        GL11.glPushMatrix()
        GlStateManager.scale(1 / antiAliasingFactor, 1 / antiAliasingFactor, 1 / antiAliasingFactor)
        GL11.glScaled(0.5, 0.5, 0.5)
        _x *= antiAliasingFactor
        _y *= antiAliasingFactor
        val red = (color shr 16 and 255) / 255.0F
        val green = (color shr 8 and 255) / 255.0F
        val blue = (color and 255) / 255.0F
        val alpha = (color shr 24 and 255) / 255.0F
        GlStateManager.color(red, green, blue, alpha)

        val blend = GL11.glIsEnabled(GL11.GL_BLEND)
        val lighting = GL11.glIsEnabled(GL11.GL_LIGHTING)
        val texture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D)
        if (!blend)
            GL11.glEnable(GL11.GL_BLEND)
        if (lighting)
            GL11.glDisable(GL11.GL_LIGHTING)
        if (texture)
            GL11.glDisable(GL11.GL_TEXTURE_2D)

        var currentColor = color
        val characters = text.toCharArray()

        var index = 0
        for (c in characters) {
            if (c == '\r') {
                _x = originalX
            }
            if (c == '\n') {
                _y += getHeight(Character.toString(c)) * 2.0F
            }
            if (c != '\u00a7' && (index == 0 || index == characters.size - 1 || characters[index - 1] != '\u00a7')) {
                unicodeFont.drawString(_x, _y, Character.toString(c), org.newdawn.slick.Color(currentColor))
                _x += (getWidth(Character.toString(c)) * 2.0F * antiAliasingFactor)
            } else if (c == ' ') {
                _x += unicodeFont.spaceWidth
            } else if (c == '\u00a7' && index != characters.size - 1) {
                val codeIndex = "0123456789abcdefg".indexOf(text[index + 1])
                if (codeIndex < 0) continue

                currentColor = colorCodes[codeIndex]
            }

            index++
        }

        GL11.glScaled(2.0, 2.0, 2.0)
        if (texture)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
        if (lighting)
            GL11.glEnable(GL11.GL_LIGHTING)
        if (!blend)
            GL11.glDisable(GL11.GL_BLEND)
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)
        GL11.glPopMatrix()
        return (_x / 2).toInt()
    }

    fun drawStringWithShadow(text: String, x: Float, y: Float, color: Int): Int {
        drawString(StringUtils.stripControlCodes(text), x + 0.5F, y + 0.5F, 0x000000)
        return drawString(text, x, y, color)
    }

    fun drawCenteredString(text: String, x: Float, y: Float, color: Int) {
        drawString(text, x - (getWidth(text) / 2).toInt(), y, color)
    }

    fun drawCenteredTextScaled(text: String, givenX: Int, givenY: Int, color: Int, givenScale: Double) {
        GL11.glPushMatrix()
        GL11.glTranslated(givenX.toDouble(), givenY.toDouble(), 0.0)
        GL11.glScaled(givenScale, givenScale, givenScale)
        drawCenteredString(text, 0.0f, 0.0f, color)
        GL11.glPopMatrix()
    }

    fun drawCenteredStringWithShadow(text: String, x: Float, y: Float, color: Int) {
        drawCenteredString(StringUtils.stripControlCodes(text), x + 0.5F, y + 0.5F, color)
        drawCenteredString(text, x, y, color)
    }

    fun getWidth(s: String): Float {
        if (cachedStringWidth.size > 1000)
            cachedStringWidth.clear()
        return cachedStringWidth.computeIfAbsent(s) {
            var width = 0.0F
            val str = StringUtils.stripControlCodes(s)
            for (c in str.toCharArray()) {
                width += unicodeFont.getWidth(Character.toString(c)) + this.kerning
            }
            width / 2.0F / antiAliasingFactor
        }
    }

    fun getStringWidth(text: String?): Int {
        if (text == null) {
            return 0
        } else {
            var i = 0
            var flag = false
            var j = 0
            while (j < text.length) {
                var c0 = text[j]
                var k = getWidth(c0.toString())
                if (k < 0 && j < text.length - 1) {
                    ++j
                    c0 = text[j]
                    if (c0 != 'l' && c0 != 'L') {
                        if (c0 == 'r' || c0 == 'R') {
                            flag = false
                        }
                    } else {
                        flag = true
                    }
                    k = 0f
                }
                i += k.toInt()
                if (flag && k > 0) {
                    ++i
                }
                ++j
            }
            return i
        }
    }

    fun getCharWidth(c: Char): Float {
        return unicodeFont.getWidth(Character.toString(c)).toFloat()
    }

    fun getHeight(s: String): Float {
        return unicodeFont.getHeight(s) / 2.0F
    }

    fun getFont(): UnicodeFont {
        return unicodeFont
    }

    fun trimStringToWidth(par1Str: String, par2: Int): String {
        val var4 = StringBuilder()
        var var5 = 0.0F
        val var6 = 0
        var var7 = false
        var var8 = false

        for (var10 in var6 until par1Str.length) {
            val var11 = par1Str[var10]
            val var12 = getCharWidth(var11)

            if (var7) {
                var7 = false

                if (var11 != 'l' && var11 != 'L') {
                    if (var11 == 'r' || var11 == 'R') {
                        var8 = false
                    }
                } else {
                    var8 = true
                }
            } else if (var12 < 0.0F) {
                var7 = true
            } else {
                var5 += var12

                if (var8) {
                    ++var5
                }
            }

            if (var5 > par2.toFloat()) {
                break
            } else {
                var4.append(var11)
            }
        }

        return var4.toString()
    }

    fun drawSplitString(lines: ArrayList<String>, x: Float, y: Float, color: Int) {
        drawString(
            lines.joinToString("\n\r"),
            x,
            y,
            color
        )
    }

    fun splitString(text: String, wrapWidth: Int): List<String> {
        val lines = ArrayList<String>()

        val splitText = text.split(" ")
        val currentString = StringBuilder()

        for (word in splitText) {
            val potential = "$currentString $word"

            if (getWidth(potential) >= wrapWidth) {
                lines.add(currentString.toString())
                currentString.setLength(0)
            }
            currentString.append("$word ")
        }
        lines.add(currentString.toString())
        return lines
    }
}