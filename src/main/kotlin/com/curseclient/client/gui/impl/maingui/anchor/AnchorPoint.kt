package com.curseclient.client.gui.impl.maingui.anchor

import net.minecraft.client.gui.ScaledResolution


/**
 * Author Seth
 * 8/7/2019 @ 1:18 PM.
 */
class AnchorPoint {
    var x = 0f
    var y = 0f
    var point: Point

    constructor(point: Point) {
        this.point = point
    }

    constructor(x: Float, y: Float, point: Point) {
        this.x = x
        this.y = y
        this.point = point
    }

    fun updatePosition(sr: ScaledResolution) {
        when (point) {
            Point.TOP_LEFT -> {
                x = 2f
                y = 2f
            }

            Point.TOP_RIGHT -> {
                x = (sr.scaledWidth - 2).toFloat()
                y = 2f
            }

            Point.BOTTOM_LEFT -> {
                x = 2f
                y = (sr.scaledHeight - 2).toFloat()
            }

            Point.BOTTOM_RIGHT -> {
                x = (sr.scaledWidth - 2).toFloat()
                y = (sr.scaledHeight - 2).toFloat()
            }

            Point.TOP_CENTER -> {
                x = sr.scaledWidth / 2.0f
                y = 2f
            }

            Point.BOTTOM_CENTER -> {
                x = sr.scaledWidth / 2.0f
                y = (sr.scaledHeight - 2).toFloat()
            }
        }
    }

    enum class Point {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        TOP_CENTER,
        BOTTOM_CENTER
    }
}