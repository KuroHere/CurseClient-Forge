package com.curseclient.client.module.modules.visual.TwoDESP

import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color

object Utils {
    fun start() {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_CULL_FACE)
        GlStateManager.disableAlpha()
        GlStateManager.disableDepth()
    }

    fun stop() {
        GlStateManager.enableAlpha()
        GlStateManager.enableDepth()
        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        color(Color.white)
    }

    fun color(red: Double, green: Double, blue: Double, alpha: Double) {
        GL11.glColor4d(red, green, blue, alpha)
    }

    fun color(red: Double, green: Double, blue: Double) {
        color(red, green, blue, 1.0)
    }

    fun color(color: Color?) {
        val convertedColor = color ?: Color.WHITE
        color(convertedColor.red / 255.0, convertedColor.green / 255.0, convertedColor.blue / 255.0, convertedColor.alpha / 255.0)
    }

    fun color(color: Color?, alpha: Int) {
        val convertedColor = color ?: Color.WHITE
        color(convertedColor.red / 255.0, convertedColor.green / 255.0, convertedColor.blue / 255.0, 0.5)
    }

    fun lineNoGl(firstX: Double, firstY: Double, secondX: Double, secondY: Double, color: Color) {
        start()
        color(color)
        GL11.glLineWidth(1f)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(GL11.GL_LINES)
        run {
            GL11.glVertex2d(firstX, firstY)
            GL11.glVertex2d(secondX, secondY)
        }
        GL11.glEnd()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        stop()
    }

    fun rect(x: Double, y: Double, width: Double, height: Double, filled: Boolean, color: Color) {
        start()
        color(color)
        GL11.glBegin(if (filled) GL11.GL_TRIANGLE_FAN else GL11.GL_LINES)
        run {
            GL11.glVertex2d(x, y)
            GL11.glVertex2d(x + width, y)
            GL11.glVertex2d(x + width, y + height)
            GL11.glVertex2d(x, y + height)
            if (!filled) {
                GL11.glVertex2d(x, y)
                GL11.glVertex2d(x, y + height)
                GL11.glVertex2d(x + width, y)
                GL11.glVertex2d(x + width, y + height)
            }
        }
        GL11.glEnd()
        stop()
    }

    fun rect(x: Double, y: Double, width: Double, height: Double, filled: Boolean) {
        rect(x, y, width, height, filled)
    }

    fun rect(x: Double, y: Double, width: Double, height: Double, color: Color) {
        rect(x, y, width, height, true, color)
    }

    fun rect(x: Double, y: Double, width: Double, height: Double) {
        rect(x, y, width, height, true)
    }


    fun polygon(x: Double, y: Double, sideLength: Double, amountOfSides: Double, filled: Boolean, color: Color?) {
        var sideLength = sideLength
        sideLength /= 2
        start()
        if (color != null)
            color(color)
        if (!filled) GL11.glLineWidth(2f)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(if (filled) GL11.GL_TRIANGLE_FAN else GL11.GL_LINE_STRIP)
        run {
            var i = 0.0
            while (i <= amountOfSides / 4) {
                val angle = i * 4 * (Math.PI * 2) / 360
                GL11.glVertex2d(x + sideLength * Math.cos(angle) + sideLength, y + sideLength * Math.sin(angle) + sideLength)
                i++
            }
        }
        GL11.glEnd()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        stop()
    }

    fun circle(x: Double, y: Double, radius: Double, filled: Boolean, color: Color?) {
        polygon(x, y, radius, 360.0, filled, color)
    }

    fun circle(x: Double, y: Double, radius: Double, filled: Boolean) {
        polygon(x, y, radius, 360.0, filled, null)
    }

    fun circle(x: Double, y: Double, radius: Double, color: Color?) {
        polygon(x, y, radius, 360.0, true, color)
    }

    fun circle(x: Double, y: Double, radius: Double) {
        polygon(x, y, radius, 360.0, true, null)
    }

}