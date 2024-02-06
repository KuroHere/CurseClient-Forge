package com.curseclient.client.gui.impl.mainmenu.elements.animation

import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.render.animation.ease.EaseUtils

class HoverAnimation {
    private var hoverProgress = 0.0

    fun update(hovered: Boolean, partialTicks: Float) {
        if (hovered) {
            hoverProgress += 0.4 * partialTicks
        } else {
            hoverProgress -= 0.1 * partialTicks
        }

        hoverProgress = hoverProgress.coerceIn(0.0, 1.0)
    }

    fun getHoverProgress(): Double {
        return MathUtils.clamp(EaseUtils.getEase(hoverProgress, EaseUtils.EaseType.InQuad), 0.0, 1.0)
    }
}