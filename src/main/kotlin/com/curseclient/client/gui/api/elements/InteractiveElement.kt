package com.curseclient.client.gui.api.elements

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.impl.styles.StyleManager
import com.curseclient.client.utility.render.HoverUtils
import com.curseclient.client.utility.render.vector.Vec2d
import java.awt.Color

abstract class InteractiveElement(pos: Vec2d, width: Double, height: Double, gui: AbstractGui) : RectElement(pos, width, height, gui) {
    protected val hovered get() = isHovered(gui.mouse)
    protected open fun isHovered(mousePos: Vec2d): Boolean { return HoverUtils.isHovered(mousePos, pos, pos.plus(width, height)) }

    open fun setLocation(pos: Vec2d) {
        this.pos = pos
    }
}
