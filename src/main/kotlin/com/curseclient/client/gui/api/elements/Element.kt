package com.curseclient.client.gui.api.elements

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.gui.api.other.IGuiElement
import com.curseclient.client.utility.render.font.Fonts

abstract class Element(val gui: AbstractGui) : IGuiElement {
    val fr get() = Fonts.DEFAULT
}