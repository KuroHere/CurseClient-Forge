package com.curseclient.client.utility.render

import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.math.MathUtils.interpolateFloat
import com.curseclient.client.utility.math.MathUtils.interpolateInt
import com.curseclient.client.utility.math.MathUtils.lerp
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11.glColor4f
import java.awt.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


object ColorUtils {

    const val ONE_THIRD = 1.0f / 3.0f
    const val TWO_THIRD = 2.0f / 3.0f

    fun blendColors(fractions: FloatArray?, colors: Array<Color>?, progress: Float): Color {
        requireNotNull(fractions) { "Fractions can't be null" }
        requireNotNull(colors) { "Colours can't be null" }

        if (fractions.size == colors.size) {
            val fractionBlack = getFraction(fractions, progress)
            val range = floatArrayOf(fractions[fractionBlack[0]], fractions[fractionBlack[1]])
            val colorRange = arrayOf(colors[fractionBlack[0]], colors[fractionBlack[1]])
            val max = range[1] - range[0]
            val value = progress - range[0]
            val weight = value / max
            return blend(colorRange[0], colorRange[1], 1.0f - weight)
        } else {
            throw IllegalArgumentException("Fractions and colours must have an equal number of elements")
        }
    }

    private fun blend(c1: Color, c2: Color, ratio: Float): Color {
        val r = (c1.red * ratio + c2.red * (1 - ratio)).toInt()
        val g = (c1.green * ratio + c2.green * (1 - ratio)).toInt()
        val b = (c1.blue * ratio + c2.blue * (1 - ratio)).toInt()
        val a = (c1.alpha * ratio + c2.alpha * (1 - ratio)).toInt()
        return Color(r, g, b, a)
    }

    private fun getFraction(fractions: FloatArray, progress: Float): IntArray {
        var start = 0
        var end = 1
        while (end < fractions.size && progress > fractions[end]) {
            start = end
            end++
        }
        return intArrayOf(start, end)
    }

    fun injectAlpha(color: Color, alpha: Int): Color {
        return Color(color.red, color.green, color.blue, MathHelper.clamp(alpha, 0, 255))
    }

    fun changeAlpha(origColor: Int, userInputedAlpha: Int): Int {
        var origColor = origColor
        origColor = origColor and 0x00FFFFFF
        return userInputedAlpha shl 24 or origColor
    }

    //Opacity value ranges from 0-1
    fun applyOpacity(color: Color, opacity: Float): Color {
        var opacity = opacity
        opacity = min(1, max(0, opacity.toInt())).toFloat()
        return Color(color.red, color.green, color.blue, (color.alpha * opacity).toInt())
    }

    fun interpolateColorsBackAndForth(speed: Int, index: Int, start: Color, end: Color, trueColor: Boolean): Color {
        var angle = ((System.currentTimeMillis() / speed + index) % 360).toInt()
        angle = (if (angle >= 180) 360 - angle else angle) * 2
        return if (trueColor) interpolateColorHue(start, end, angle / 360f) else interpolateColorC(start, end, angle / 360f)
    }

    //The next few methods are for interpolating colors
    fun interpolateColor(color1: Color, color2: Color, amount: Float): Int {
        val amount = min(1f, max(0f, amount))
        return interpolateColorC(color1, color2, amount).rgb
    }

    fun interpolateColor(color1: Int, color2: Int, amount: Float): Int {
        val amount = min(1f, max(0f, amount))
        val cColor1 = Color(color1)
        val cColor2 = Color(color2)
        return interpolateColorC(cColor1, cColor2, amount).rgb
    }

    fun interpolateColorC(color1: Color, color2: Color, amount: Float): Color {
        val amount = min(1f, max(0f, amount))
        return Color(interpolateInt(color1.red, color2.red, amount.toDouble()),
            interpolateInt(color1.green, color2.green, amount.toDouble()),
            interpolateInt(color1.blue, color2.blue, amount.toDouble()),
            interpolateInt(color1.alpha, color2.alpha, amount.toDouble()))
    }

    private fun interpolateColorHue(color1: Color, color2: Color, amount: Float): Color {
        val amount = min(1f, max(0f, amount))
        val color1HSB = Color.RGBtoHSB(color1.red, color1.green, color1.blue, null)
        val color2HSB = Color.RGBtoHSB(color2.red, color2.green, color2.blue, null)
        val resultColor = Color.getHSBColor(interpolateFloat(color1HSB[0], color2HSB[0], amount.toDouble()),
            interpolateFloat(color1HSB[1], color2HSB[1], amount.toDouble()), interpolateFloat(color1HSB[2], color2HSB[2], amount.toDouble()))
        return applyOpacity(resultColor, interpolateInt(color1.alpha, color2.alpha, amount.toDouble()) / 255f)
    }

    private fun gradientColor(color1: Color, color2: Color, offset: Double): Color {
        var offset = offset
        if (offset > 1) {
            val left = offset % 1
            val off = offset.toInt()
            offset = if (off % 2 == 0) left else 1 - left
        }
        val inverse_percent = 1 - offset
        val redPart = (color1.red * inverse_percent + color2.red * offset).toInt()
        val greenPart = (color1.green * inverse_percent + color2.green * offset).toInt()
        val bluePart = (color1.blue * inverse_percent + color2.blue * offset).toInt()
        return Color(redPart, greenPart, bluePart)
    }

    //Fade a color in and out with a specified alpha value ranging from 0-1
    fun fade(speed: Int, index: Int, color: Color, alpha: Float): Color {
        val hsb = Color.RGBtoHSB(color.red, color.green, color.blue, null)
        var angle = ((System.currentTimeMillis() / speed + index) % 360).toInt()
        angle = (if (angle > 180) 360 - angle else angle) + 180
        val colorHSB = Color(Color.HSBtoRGB(hsb[0], hsb[1], angle / 360f))
        return Color(colorHSB.red, colorHSB.green, colorHSB.blue, max(0, min(255, (alpha * 255).toInt())))
    }

    fun darker(color: Color, v: Float): Color {
        return Color(max((color.red * v).toInt(), 0), max((color.green * v).toInt(), 0), max((color.blue * v).toInt(), 0),
            color.alpha)
    }

    fun toRGBA(r: Int, g: Int, b: Int, a: Int): Int {
        return (r shl 16) + (g shl 8) + b + (a shl 24)
    }

    fun hsvToRgb(h: Float, s: Float, v: Float): Int {
        val i = (h * 6).toInt()
        val f = h * 6 - i
        val p = v * (1 - s)
        val q = v * (1 - f * s)
        val t = v * (1 - (1 - f) * s)

        return when (i % 6) {
            0 -> rgbToInt(v, t, p)
            1 -> rgbToInt(q, v, p)
            2 -> rgbToInt(p, v, t)
            3 -> rgbToInt(p, q, v)
            4 -> rgbToInt(t, p, v)
            else -> rgbToInt(v, p, q)
        }
    }

    private fun rgbToInt(r: Float, g: Float, b: Float): Int {
        val red = (r * 255).toInt()
        val green = (g * 255).toInt()
        val blue = (b * 255).toInt()

        return (red shl 16) or (green shl 8) or blue
    }

    fun hsbToRGB(h: Float, s: Float, b: Float): Color {
        return hsbToRGB(h, s, b, 1.0f)
    }

    fun hsbToRGB(h: Float, s: Float, b: Float, a: Float): Color {
        val hue6 = (h % 1.0f) * 6.0f
        val intHue6 = hue6.toInt()
        val f = hue6 - intHue6
        val p = b * (1.0f - s)
        val q = b * (1.0f - f * s)
        val t = b * (1.0f - (1.0f - f) * s)

        return when (intHue6) {
            0 -> Color(b, t, p, a)
            1 -> Color(q, b, p, a)
            2 -> Color(p, b, t, a)
            3 -> Color(p, q, b, a)
            4 -> Color(t, p, b, a)
            5 -> Color(b, p, q, a)
            else -> Color(255, 255, 255)
        }
    }

    fun rgbToHue(r: Int, g: Int, b: Int): Float {
        val cMax = maxOf(r, g, b)
        if (cMax == 0) return 0.0f

        val cMin = minOf(r, g, b)
        if (cMax == cMin) return 0.0f

        val diff = (cMax - cMin) * 6.0f

        val hue = when (cMax) {
            r -> {
                (g - b) / diff + 1.0f
            }
            g -> {
                (b - r) / diff + ONE_THIRD
            }
            else -> {
                (r - g) / diff + TWO_THIRD
            }
        }

        return hue % 1.0f
    }

    fun rgbToSaturation(r: Int, g: Int, b: Int): Float {
        val cMax = maxOf(r, g, b)
        if (cMax == 0) return 0.0f

        val cMin = minOf(r, g, b)
        val diff = cMax - cMin

        return diff / cMax.toFloat()
    }

    fun rgbToBrightness(r: Int, g: Int, b: Int): Float {
        return maxOf(r, g, b) / 255.0f
    }

    fun rgbToLightness(r: Int, g: Int, b: Int): Float {
        return (maxOf(r, g, b) + minOf(r, g, b)) / 510.0f
    }

    fun argbToRgba(argb: Int) =
        (argb and 0xFFFFFF shl 8) or
            (argb shr 24 and 255)

    fun rgbaToArgb(rgba: Int) =
        (rgba shr 8 and 0xFFFFFF) or
            (rgba and 255 shl 24)

    fun rgbToHSB(r: Int, g: Int, b: Int, a: Int): Color {
        val cMax = maxOf(r, g, b)
        if (cMax == 0) return Color(0.0f, 0.0f, 0.0f, a / 255.0f)

        val cMin = minOf(r, g, b)
        val diff = cMax - cMin

        val diff6 = diff * 6.0f

        var hue = when (cMax) {
            cMin -> {
                0.0f
            }
            r -> {
                (g - b) / diff6 + 1.0f
            }
            g -> {
                (b - r) / diff6 + ONE_THIRD
            }
            else -> {
                (r - g) / diff6 + TWO_THIRD
            }
        }

        hue %= 1.0f

        val saturation = diff / cMax.toFloat()
        val brightness = cMax / 255.0f

        return Color(hue, saturation, brightness, a / 255.0f)
    }

    val Color.r get() = red.toFloat() / 255f
    val Color.g get() = green.toFloat() / 255f
    val Color.b get() = blue.toFloat() / 255f
    val Color.a get() = alpha.toFloat() / 255f

    fun Int.toColor(): Color {
        return Color(this)
    }

    fun Color.glColor() {
        glColor4f(this.red / 255f, this.green / 255f, this.blue / 255f, this.alpha / 255f)
    }

    fun Color.setAlphaD(amount: Double): Color {
        return this.setAlpha((clamp(amount, 0.0, 1.0) * 255.0).toInt())
    }

    fun Color.multAlpha(amount: Double): Color {
        return this.setAlpha(clamp(amount * this.alpha.toDouble(), 0.0, 255.0).toInt())
    }

    fun Color.setAlpha(amount: Int): Color {
        return Color(this.red, this.green, this.blue, amount)
    }

    fun Color.setDarkness(amount: Int): Color {
        val darkerRed = (this.red * (1 - amount / 255.0)).toInt()
        val darkerGreen = (this.green * (1 - amount / 255.0)).toInt()
        val darkerBlue = (this.blue * (1 - amount / 255.0)).toInt()

        return Color(darkerRed, darkerGreen, darkerBlue)
    }

    val icon_singleplayer = ResourceLocation("curseclient", "icons/singleplayer.png")
    val icon_multiplayer = ResourceLocation("curseclient", "icons/multiplayer.png")
    val icon_altmanager = ResourceLocation("curseclient", "icons/altmanager.png")
    val icon_settings = ResourceLocation("curseclient", "icons/settings.png")
    val icon_shutdown = ResourceLocation("curseclient", "icons/shutdown.png")

    fun lerp(c1: Color, c2: Color, p: Double): Color {
        val r = lerp(c1.r, c2.r, p.toFloat())
        val g = lerp(c1.g, c2.g, p.toFloat())
        val b = lerp(c1.b, c2.b, p.toFloat())
        val a = lerp(c1.a, c2.a, p.toFloat())

        return Color(r, g, b, a)
    }

    fun pulseColor(color: Color, index: Int, count: Int): Color {
        val hsb = FloatArray(3)
        Color.RGBtoHSB(color.red, color.green, color.blue, hsb)
        var brightness = abs(((System.currentTimeMillis() % (1230675006L xor 0x495A9BEEL) / java.lang.Float.intBitsToFloat(java.lang.Float.floatToIntBits(0.0013786979f) xor 0x7ECEB56D) + index / count.toFloat() * java.lang.Float.intBitsToFloat(java.lang.Float.floatToIntBits(0.09192204f) xor 0x7DBC419F)) % java.lang.Float.intBitsToFloat(java.lang.Float.floatToIntBits(0.7858098f) xor 0x7F492AD5) - java.lang.Float.intBitsToFloat(java.lang.Float.floatToIntBits(6.46708f) xor 0x7F4EF252)).toDouble()).toFloat()
        brightness = java.lang.Float.intBitsToFloat(java.lang.Float.floatToIntBits(18.996923f) xor 0x7E97F9B3) + java.lang.Float.intBitsToFloat(java.lang.Float.floatToIntBits(2.7958195f) xor 0x7F32EEB5) * brightness
        hsb[2] = brightness % java.lang.Float.intBitsToFloat(java.lang.Float.floatToIntBits(0.8992331f) xor 0x7F663424)
        return Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]))
    }

    @JvmStatic
    fun reAlpha(color: Color, alpha: Int): Color = Color(color.red, color.green, color.blue, alpha.coerceIn(0, 255))

    @JvmStatic
    fun reAlpha(color: Color, alpha: Float): Color = Color(color.red / 255F, color.green / 255F, color.blue / 255F, alpha.coerceIn(0F, 1F))

}