package com.curseclient.client.gui.api.elements

import com.curseclient.client.gui.api.AbstractGui
import com.curseclient.client.utility.render.vector.Vec2d

abstract class RectElement(var pos: Vec2d, var width: Double, var height: Double, gui: AbstractGui): Element(gui)