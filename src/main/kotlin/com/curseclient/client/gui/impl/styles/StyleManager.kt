package com.curseclient.client.gui.impl.styles

import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.animation.animaions.AstolfoAnimation
import net.minecraft.util.math.MathHelper

object StyleManager {

    enum class Styles(val color1: Int, val color2: Int) {
        Astolfo(HexColor.toColor("#000000"), HexColor.toColor("#FFFFFF")),
        Violet(HexColor.toColor("#FCFC36"), HexColor.toColor("#5D00B2")),
        Lavender(HexColor.toColor("#F4ECFF"), HexColor.toColor("#765AA5")),
        Sky(HexColor.toColor("#07338A"), HexColor.toColor("#0078FF")),
        Moonshine(HexColor.toColor("#E3C3FF"), HexColor.toColor("#67FFEC")),
        Asoka(HexColor.toColor("#FFC854"), HexColor.toColor("#4288FF")),
        Rainbow(HexColor.toColor("#FF00AA"), HexColor.toColor("#AA00FF")),
        Sucechery(HexColor.toColor("#FF5C7D"), HexColor.toColor("#5CFF9E")),
        Gold(HexColor.toColor("#FFC300"), HexColor.toColor("#FF5800")),
        Ice(HexColor.toColor("#B7EFFF"), HexColor.toColor("#FFAAFF")),
        Dawn(HexColor.toColor("#FF007F"), HexColor.toColor("#00FFB7")),
        Darkviloet(HexColor.toColor("#42275A"), HexColor.toColor("#734B6D")),
        Wave(HexColor.toColor("#343838"), HexColor.toColor("#005F6B")),
        Night(HexColor.toColor("#2C3E50"), HexColor.toColor("#FD746C")),
        Confectionery(HexColor.toColor("#76ACD7"), HexColor.toColor("#F15FE9")),
        Krovavy(HexColor.toColor("#FD3A3A"), HexColor.toColor("#3A3A3A")),
        Scarlet(HexColor.toColor("#8B8DF6"), HexColor.toColor("#E60101")),
        Lunar(HexColor.toColor("#F4ECFF"), HexColor.toColor("#76BEDF")),
        Coralloy(HexColor.toColor("#FF6347"), HexColor.toColor("#0044FF")),
        Ocean(HexColor.toColor("#373b44"), HexColor.toColor("#4286f4")),
        Huh(HexColor.toColor("#765AA5"), HexColor.toColor("#F4ECFF")),
        Pink(HexColor.toColor("#E5AAC3"), HexColor.toColor("#9A52C7")),
        Red(HexColor.toColor("#E65758"), HexColor.toColor("#771D32")),
        Horizon(HexColor.toColor("#EF33B1"), HexColor.toColor("#F6E6BC")),
        Lime(HexColor.toColor("#849B5C"), HexColor.toColor("#BFFFC7"));

        fun getColor(index: Int): Int {
            return if (this == Astolfo) {
                AstolfoAnimation.getColor(10, index, 0.5F, 1.0F, 1.0F)
            } else {
                ColorUtils.gradient(5, index, color1, color2)
            }
        }

        fun getColor(index: Int, alpha: Float): Int {
            return if (this == Astolfo) {
                AstolfoAnimation.getColor(10, index, 0.5F, 1.0F, alpha)
            } else {
                ColorUtils.gradient(5, index, 1.0F, alpha, color1, color2)
            }
        }

        fun getColor(index: Int, brightness: Float, alpha: Float): Int {
            return if (this == Astolfo) {
                AstolfoAnimation.getColor(10, index, 0.5F, brightness, alpha)
            } else {
                ColorUtils.gradient(5, index, brightness, alpha, color1, color2)
            }
        }

        fun getColor(speed:Int, index: Int, brightness: Float, alpha: Float): Int {
            return if (this == Astolfo) {
                AstolfoAnimation.getColor(speed * 2, index, 0.5F, brightness, alpha)
            } else {
                ColorUtils.gradient(speed, index, brightness, alpha, color1, color2)
            }
        }
    }

    object HexColor {

        fun toColor(hexColor: String): Int {
            val argb = hexColor.substring(1).toInt(16)
            return reAlphaInt(argb, 255)
        }

        private fun reAlphaInt(color: Int, alpha: Int): Int {
            return (MathHelper.clamp(alpha, 0, 255) shl 24) or (color and 16777215)
        }
    }
}