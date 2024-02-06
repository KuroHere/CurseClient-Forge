package com.curseclient.client.manager.managers

import com.curseclient.client.manager.Manager
import com.curseclient.client.utility.render.ClickCircle
import java.awt.Color
import java.util.function.Consumer

object ScreenManager: Manager("ScreenManager") {
    val clickCircles: MutableList<ClickCircle>

    init {
        clickCircles = ArrayList()
    }

    fun drawCircle(color: Color) {
        clickCircles.removeIf(ClickCircle::isRemovable)
        clickCircles.forEach(Consumer { clickCircle: ClickCircle -> clickCircle.draw(color.rgb) })
    }
}