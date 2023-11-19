package com.curseclient.client.utility.render

import com.curseclient.client.utility.render.vector.Vec2d

object HoverUtils {
    fun isHovered(mouseX: Int, mouseY: Int, x: Int, y: Int, x2: Int, y2: Int): Boolean {
        return (mouseX > x) && (mouseY > y) && (mouseX < x2) && (mouseY < y2)
    }

    fun isHovered(mouse: Vec2d, posBegin: Vec2d, posEnd: Vec2d): Boolean {
        return (mouse.x in posBegin.x..posEnd.x) && (mouse.y in posBegin.y..posEnd.y)
    }
}