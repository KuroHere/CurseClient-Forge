package com.curseclient.client.utility.render.shader

import com.curseclient.client.module.modules.client.ClickGui
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.utility.math.Quad
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.min

class RectBuilder(val pos1: Vec2d, val pos2: Vec2d) {
    private var colorIn = Quad<Color>(Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE)
    private var colorOut = Quad<Color>(Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE)

    private var roundRadius = 0.0f
    private var outlineWidth = 0.0f

    fun draw(block: RectBuilder.() -> Unit) {
        block(this)
        draw()
    }

    // region color
    fun color(leftTop: Color, rightTop: Color, leftBottom: Color, rightBottom: Color) =
        this.apply { colorIn = Quad(leftTop, rightTop, leftBottom, rightBottom) }

    fun color(color: Color) = color(color, color, color, color)

    fun colorV(top: Color, bottom: Color) =
        color(top, top, bottom, bottom)

    fun colorH(left: Color, right: Color) =
        color(left, right, left, right)
    // endregion

    // region outlineColor
    fun outlineColor(leftTop: Color, rightTop: Color, leftBottom: Color, rightBottom: Color) =
        this.apply { colorOut = Quad(leftTop, rightTop, leftBottom, rightBottom) }

    fun outlineColor(color: Color) = outlineColor(color, color, color, color)

    fun outlineColorV(top: Color, bottom: Color) =
        outlineColor(top, top, bottom, bottom)

    fun outlineColorH(left: Color, right: Color) =
        outlineColor(left, right, left, right)
    // endregion

    fun radius(value: Double) = this.apply { roundRadius = value.toFloat() }

    fun width(value: Double) = this.apply { outlineWidth = value.toFloat() }

    // Don't know why I'm doing that...
    fun shadow(xIn: Float, yIn: Float, widthIn: Float, heightIn: Float, blurRadiusIn: Int, colorIn: Color) {
        RenderUtils2D.drawBlurredShadow(xIn, yIn, widthIn, heightIn, blurRadiusIn, colorIn)
    }

    fun shadow(xIn: Double, yIn: Double, widthIn: Double, heightIn: Double, blurRadiusIn: Int, colorIn: Color) {
        RenderUtils2D.drawBlurredShadow(xIn.toFloat(), yIn.toFloat(), widthIn.toFloat(), heightIn.toFloat(), blurRadiusIn, colorIn)
    }

    companion object {
        private val sizeUniform by lazy { Shaders.rectShader.getUniform("size") }
        private val roundRadiusUniform by lazy { Shaders.rectShader.getUniform("roundRadius") }
        private val smoothFactorUniform by lazy { Shaders.rectShader.getUniform("smoothFactor") }
        private val outlineWidthUniform by lazy { Shaders.rectShader.getUniform("outlineWidth") }
        private val colorIn1Uniform by lazy { Shaders.rectShader.getUniform("colorIn1") }
        private val colorIn2Uniform by lazy { Shaders.rectShader.getUniform("colorIn2") }
        private val colorIn3Uniform by lazy { Shaders.rectShader.getUniform("colorIn3") }
        private val colorIn4Uniform by lazy { Shaders.rectShader.getUniform("colorIn4") }
        private val colorOut1Uniform by lazy { Shaders.rectShader.getUniform("colorOut1") }
        private val colorOut2Uniform by lazy { Shaders.rectShader.getUniform("colorOut2") }
        private val colorOut3Uniform by lazy { Shaders.rectShader.getUniform("colorOut3") }
        private val colorOut4Uniform by lazy { Shaders.rectShader.getUniform("colorOut4") }
    }

    fun draw(): RectBuilder {
        val mc = Minecraft.getMinecraft()
        val sr = ScaledResolution(mc)

        var x1 = pos1.x
        var y1 = pos1.y
        var x2 = pos2.x
        var y2 = pos2.y

        if (x1 > x2) {
            val i = x1
            x1 = x2
            x2 = i
        }

        if (y1 > y2) {
            val j = y1
            y1 = y2
            y2 = j
        }

        val x = x1.toFloat()
        val y = y1.toFloat()
        val width = (x2 - x1).toFloat()
        val height = (y2 - y1).toFloat()
        val scale = sr.scaleFactor.toFloat()
        val radius = min(roundRadius, min(width, height) / 2.0f)

        GlStateManager.resetColor()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_ALPHA_TEST)

        with(Shaders.rectShader) {
            begin()

            uniformf(sizeUniform, width * scale, height * scale)
            uniformf(roundRadiusUniform, radius * scale)
            uniformf(smoothFactorUniform, 1.2f)
            uniformf(outlineWidthUniform, outlineWidth * scale)

            colorUniform(colorIn1Uniform, colorIn.first)
            colorUniform(colorIn2Uniform, colorIn.third)
            colorUniform(colorIn3Uniform, colorIn.second)
            colorUniform(colorIn4Uniform, colorIn.fourth)

            colorUniform(colorOut1Uniform, colorOut.first)
            colorUniform(colorOut2Uniform, colorOut.third)
            colorUniform(colorOut3Uniform, colorOut.second)
            colorUniform(colorOut4Uniform, colorOut.fourth)

            val s = 0.4f
            render(x - s, y - s, width + s * 2.0f, height + s * 2.0f)

            end()
        }
        glEnable(GL_ALPHA_TEST)
        GlStateManager.disableBlend()


        return this
    }
}
