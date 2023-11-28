package com.curseclient.client.manager.managers

import com.curseclient.client.gui.impl.clickgui.ClickGuiHud
import com.curseclient.client.manager.Manager
import com.curseclient.client.utility.render.ClickCircle
import java.util.function.Consumer


object ScreenManager: Manager("ScreenManager") {
    val clickGuiHud: ClickGuiHud = ClickGuiHud()
    val clickCircles: MutableList<ClickCircle>

    init {
        clickCircles = ArrayList()
    }

    fun drawCircle(color: Int) {
        clickCircles.removeIf(ClickCircle::isRemovable)
        clickCircles.forEach(Consumer { clickCircle: ClickCircle -> clickCircle.draw(color) })
    }
}