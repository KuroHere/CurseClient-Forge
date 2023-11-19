package com.curseclient.client.utility.render

import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.render.ColorUtils.g
import com.curseclient.client.utility.render.ColorUtils.r
import com.curseclient.client.utility.render.ColorUtils.b
import com.curseclient.client.utility.render.ColorUtils.a
import com.curseclient.client.utility.render.ColorUtils.toColor
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11.*
import java.awt.Color

class VertexHelper(private val useVbo: Boolean = Minecraft.getMinecraft().gameSettings.useVbo) {
    private val tessellator = Tessellator.getInstance()
    private val buffer = tessellator.buffer

    fun begin(mode: Int) {
        if (useVbo) {
            buffer.begin(mode, DefaultVertexFormats.POSITION_COLOR)
        } else {
            glBegin(mode)
        }
    }

    fun put(pos: Vec3d, color: Color) {
        put(pos.x, pos.y, pos.z, color)
    }

    fun put(x: Double, y: Double, z: Double, color: Color) {
        if (useVbo) {
            buffer.pos(x, y, z).color(color.red, color.green, color.blue, color.alpha).endVertex()
        } else {
            color.setGLColor()
            glVertex3d(x, y, z)
        }
    }

    fun put(pos: Vec2d, color: Color) {
        put(pos.x, pos.y, color)
    }

    fun put(x: Double, y: Double, color: Color) {
        if (useVbo) {
            buffer.pos(x, y, 0.0).color(color.red, color.green, color.blue, color.alpha).endVertex()
        } else {
            color.setGLColor()
            glVertex2d(x, y)
        }
    }

    fun end() {
        if (useVbo) {
            tessellator.draw()
        } else {
            glEnd()
        }
    }

    fun Color.setGLColor() {
        glColor4f(this.red / 255f, this.green / 255f, this.blue / 255f, this.alpha / 255f)
    }

    companion object {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer

        fun begin(mode: Int) {
            buffer.begin(mode, DefaultVertexFormats.POSITION_COLOR)
        }


        fun put(pos: Vec3d, color: Color) {
            put(pos.x, pos.y, pos.z, color)
        }

        fun put(x: Double, y: Double, z: Double, color: Color) {
            buffer.pos(x, y, z).color(color.r, color.g, color.b, color.a).endVertex()
        }

        fun put(pos: Vec2d, color: Color) {
            put(pos.x, pos.y, color)
        }

        fun put(x: Int, y: Int, color: Color) {
            buffer.pos(x.toDouble(), y.toDouble(), 0.0).color(color.r, color.g, color.b, color.a).endVertex()
        }

        fun put(pos: Vec2d, color: Int) {
            put(pos.x, pos.y, color.toColor())
        }

        fun put(x: Double, y: Double, color: Color) {
            buffer.pos(x, y, 0.0).color(color.r, color.g, color.b, color.a).endVertex()
        }
        fun end() {
            tessellator.draw()
        }
    }
}