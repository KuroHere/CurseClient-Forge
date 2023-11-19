package com.curseclient.client.utility.math

import baritone.api.utils.Helper.mc
import net.minecraft.client.gui.ScaledResolution

object ScaleHelper {

    var lastScale = getScale()

    private fun getScale(): Int {
        val scaledWidth = mc.displayWidth
        val scaledHeight = mc.displayHeight
        var scaleFactor = 1
        val flag = mc.isUnicode
        var i = mc.gameSettings.guiScale

        if (i == 0) {
            i = 1000
        }
        while (scaleFactor < i && scaledWidth / (scaleFactor + 1) >= 320 && scaledHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor
        }
        if (flag && scaleFactor % 2 != 0 && scaleFactor != 1) {
            --scaleFactor
        }
        return scaleFactor
    }

    var scaledResolution = ScaledResolution(mc)
        set(value) {
            field = value
            lastScale = getScale()
        }

    val width get() = scaledResolution.scaledWidth
    val height get() = scaledResolution.scaledHeight

}