package com.curseclient.client.gui.impl.particles.simple.util

import org.lwjgl.opengl.GL11
import kotlin.math.cos
import kotlin.math.sin


object RenderUtils {

    fun connectPoints(xOne: Float, yOne: Float, xTwo: Float, yTwo: Float) {
        GL11.glPushMatrix()
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.8f)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glLineWidth(0.5f)
        GL11.glBegin(GL11.GL_LINES)
        GL11.glVertex2f(xOne, yOne)
        GL11.glVertex2f(xTwo, yTwo)
        GL11.glEnd()
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glPopMatrix()
    }

    fun drawCircle(x: Float, y: Float, radius: Float, color: Int) {
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f
        GL11.glColor4f(red, green, blue, alpha)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glPushMatrix()
        GL11.glLineWidth(1f)
        GL11.glBegin(GL11.GL_POLYGON)
        for (i in 0..360) GL11.glVertex2d(x + sin(i * Math.PI / 180.0) * radius, y + cos(i * Math.PI / 180.0) * radius)
        GL11.glEnd()
        GL11.glPopMatrix()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glColor4f(1f, 1f, 1f, 1f)
    }

}