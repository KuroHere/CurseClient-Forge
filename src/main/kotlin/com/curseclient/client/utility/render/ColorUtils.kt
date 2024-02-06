package com.curseclient.client.utility.render

import com.curseclient.client.gui.impl.styles.StyleManager
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.math.MathUtils.interpolateFloat
import com.curseclient.client.utility.math.MathUtils.interpolateInt
import com.curseclient.client.utility.math.MathUtils.lerp
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11.glColor4f
import java.awt.Color
import java.lang.Float.floatToIntBits
import java.lang.Float.intBitsToFloat
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object ColorUtils {

    fun getColorStyle(index: Float, s: StyleManager.Styles) = s.getColor(index.toInt())

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

    fun gradient(speed: Int, index: Int, vararg colors: Int): Int {
        val angle = ((System.currentTimeMillis() / speed + index) % 360).toInt()
        val adjustedAngle = if (angle > 180) 360 - angle else angle + 180
        var colorIndex = (adjustedAngle / 360f * colors.size).toInt()
        if (colorIndex == colors.size) {
            colorIndex--
        }
        val color1 = colors[colorIndex]
        val color2 = colors[if (colorIndex == colors.size - 1) 0 else colorIndex + 1]
        return interpolateColor(color1, color2, adjustedAngle / 360f * colors.size - colorIndex)
    }

    fun gradient(speed: Int, index: Int, brightness: Float, alpha: Float, vararg colors: Int): Int {
        val angle = ((System.currentTimeMillis() / speed + index) % 360).toInt()
        val adjustedAngle = if (angle > 180) 360 - angle else angle + 180
        var colorIndex = (adjustedAngle / 360f * colors.size).toInt()
        if (colorIndex == colors.size) {
            colorIndex--
        }
        val color1 = adjustBrightnessAndAlpha(colors[colorIndex], brightness, alpha)
        val color2 = adjustBrightnessAndAlpha(colors[if (colorIndex == colors.size - 1) 0 else colorIndex + 1], brightness, alpha)
        return interpolateColor(color1, color2, adjustedAngle / 360f * colors.size - colorIndex)
    }

    private fun adjustBrightnessAndAlpha(color: Int, brightness: Float, alpha: Float): Int {
        val red = (color shr 16 and 0xFF) * brightness
        val green = (color shr 8 and 0xFF) * brightness
        val blue = (color and 0xFF) * brightness
        val adjustedAlpha = (color shr 24 and 0xFF) * alpha

        return (adjustedAlpha.toInt() shl 24) or (red.toInt() shl 16) or (green.toInt() shl 8) or blue.toInt()
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

    private fun applyOpacity(color: Color, opacity: Float): Color {
        var opacity = opacity
        opacity = min(1, max(0, opacity.toInt())).toFloat()
        return Color(color.red, color.green, color.blue, (color.alpha * opacity).toInt())
    }

    fun interpolateColorsBackAndForth(speed: Int, index: Int, start: Color, end: Color, trueColor: Boolean): Color {
        var angle = ((System.currentTimeMillis() / speed + index) % 360).toInt()
        angle = (if (angle >= 180) 360 - angle else angle) * 2
        return if (trueColor) interpolateColorHue(start, end, angle / 360f) else interpolateColorC(start, end, angle / 360f)
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

    fun interpolateColor(color1: Int, color2: Int, amount: Float): Int {
        var amount = amount
        amount = min(1.0, max(0.0, amount.toDouble())).toFloat()

        val red1: Int = getRed(color1)
        val green1: Int = getGreen(color1)
        val blue1: Int = getBlue(color1)
        val alpha1: Int = getAlpha(color1)

        val red2: Int = getRed(color2)
        val green2: Int = getGreen(color2)
        val blue2: Int = getBlue(color2)
        val alpha2: Int = getAlpha(color2)

        val interpolatedRed: Int = interpolateInt(red1, red2, amount.toDouble())
        val interpolatedGreen: Int = interpolateInt(green1, green2, amount.toDouble())
        val interpolatedBlue: Int = interpolateInt(blue1, blue2, amount.toDouble())
        val interpolatedAlpha: Int = interpolateInt(alpha1, alpha2, amount.toDouble())

        return (interpolatedAlpha shl 24) or (interpolatedRed shl 16) or (interpolatedGreen shl 8) or interpolatedBlue
    }

    fun interpolateColor(startColor: Color, endColor: Color, t: Float): Color {
        val r = lerp(startColor.red.toFloat(), endColor.red.toFloat(), t)
        val g = lerp(startColor.green.toFloat(), endColor.green.toFloat(), t)
        val b = lerp(startColor.blue.toFloat(), endColor.blue.toFloat(), t)
        val a = lerp(startColor.alpha.toFloat(), endColor.alpha.toFloat(), t)

        return Color(r.toInt(), g.toInt(), b.toInt(), a.toInt())
    }

    fun pulseColor(color: Color, index: Int, count: Int): Color {
        val hsb = FloatArray(3)
        Color.RGBtoHSB(color.red, color.green, color.blue, hsb)
        var brightness = abs(((System.currentTimeMillis() % (1230675006L xor 0x495A9BEEL) / intBitsToFloat(floatToIntBits(0.0013786979f) xor 0x7ECEB56D) + index / count.toFloat() * intBitsToFloat(floatToIntBits(0.09192204f) xor 0x7DBC419F)) % intBitsToFloat(floatToIntBits(0.7858098f) xor 0x7F492AD5) - intBitsToFloat(floatToIntBits(6.46708f) xor 0x7F4EF252)).toDouble()).toFloat()
        brightness = intBitsToFloat(floatToIntBits(18.996923f) xor 0x7E97F9B3) + intBitsToFloat(floatToIntBits(2.7958195f) xor 0x7F32EEB5) * brightness
        hsb[2] = brightness % intBitsToFloat(floatToIntBits(0.8992331f) xor 0x7F663424)
        return Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]))
    }

    fun pulseAlpha(color: Color, index: Int, count: Int): Color {
        val hsb = FloatArray(3)
        Color.RGBtoHSB(color.red, color.green, color.blue, hsb)
        var alpha = abs(((System.currentTimeMillis() % (1230675006L xor 0x495A9BEEL) / intBitsToFloat(floatToIntBits(0.0013786979f) xor 0x7ECEB56D) + index / count.toFloat() * intBitsToFloat(floatToIntBits(0.09192204f) xor 0x7DBC419F)) % intBitsToFloat(floatToIntBits(0.7858098f) xor 0x7F492AD5) - intBitsToFloat(floatToIntBits(6.46708f) xor 0x7F4EF252)).toDouble()).toFloat()
        alpha = intBitsToFloat(floatToIntBits(18.996923f) xor 0x7E97F9B3) + intBitsToFloat(floatToIntBits(2.7958195f) xor 0x7F32EEB5) * alpha
        return Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2])).setAlpha((alpha * 255).toInt())
    }

    fun hsbToRGB(h: Float, s: Float, b: Float): Color {
        return Color.HSBtoRGB(h, s, b).toColor()
    }

    fun rgb(color: Int) =
        floatArrayOf((color shr 16 and 0xFF) / 255f,
            (color shr 8 and 0xFF) / 255f,
            (color and 0xFF) / 255f,
            (color shr 24 and 0xFF) / 255f
        )

    val Color.r get() = red.toFloat() / 255f
    val Color.g get() = green.toFloat() / 255f
    val Color.b get() = blue.toFloat() / 255f
    val Color.a get() = alpha.toFloat() / 255f

    fun Int.toColor() = Color(this)

    fun Color.glColor() {
        glColor4f(this.red / 255f, this.green / 255f, this.blue / 255f, this.alpha / 255f)
    }

    fun setAlphaColor(color: Int, alpha: Float) {
        val red = (color shr 16 and 255).toFloat() / 255.0f
        val green = (color shr 8 and 255).toFloat() / 255.0f
        val blue = (color and 255).toFloat() / 255.0f
        glColor4f(red, green, blue, alpha)
    }

    fun setColor(color: Int) {
        setAlphaColor(color, (color shr 24 and 255).toFloat() / 255.0f)
    }

    fun Color.setAlphaD(amount: Double) = this.setAlpha((clamp(amount, 0.0, 1.0) * 255.0).toInt())

    fun Color.multAlpha(amount: Double) = this.setAlpha(clamp(amount * this.alpha.toDouble(), 0.0, 255.0).toInt())

    fun Color.setAlpha(amount: Int) = Color(this.red, this.green, this.blue, amount)


    private fun getRed(hex: Int) = hex shr 16 and 255


    private fun getGreen(hex: Int) = hex shr 8 and 255


    private fun getBlue(hex: Int) = hex and 255

    private fun getAlpha(hex: Int) = hex shr 24 and 255


    fun Color.setDarkness(amount: Int): Color {
        val darkerRed = (this.red * (1 - amount / 255.0)).toInt()
        val darkerGreen = (this.green * (1 - amount / 255.0)).toInt()
        val darkerBlue = (this.blue * (1 - amount / 255.0)).toInt()

        return Color(darkerRed, darkerGreen, darkerBlue)
    }

    // TODO: Need to make imageManager
    val icon_singleplayer = ResourceLocation( "textures/icons/mainmenu/singleplayer.png")
    val icon_multiplayer = ResourceLocation("textures/icons/mainmenu/multiplayer.png")
    val icon_altmanager = ResourceLocation("textures/icons/mainmenu/altmanager.png")
    val icon_settings = ResourceLocation("textures/icons/mainmenu/settings.png")
    val icon_shutdown = ResourceLocation("textures/icons/mainmenu/shutdown.png")

    fun lerp(c1: Color, c2: Color, p: Double): Color {
        val r = lerp(c1.r, c2.r, p.toFloat())
        val g = lerp(c1.g, c2.g, p.toFloat())
        val b = lerp(c1.b, c2.b, p.toFloat())
        val a = lerp(c1.a, c2.a, p.toFloat())

        return Color(r, g, b, a)
    }

}