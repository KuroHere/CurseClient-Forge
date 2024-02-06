package com.curseclient.client.utility.render.shader

import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.graphic.GLUtils.glColor
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.*
import java.awt.Color


object RoundedUtil {

    var roundedShader = ShaderUtils("roundedRect")
    var roundedOutlineShader = ShaderUtils("roundRectOutline")
    private val roundedTexturedShader = ShaderUtils("roundRectTexture")
    private val roundedGradientShader = ShaderUtils("roundedRectGradient")

    @JvmStatic
    fun drawRound(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color) {
        drawRound(x, y, width, height, radius, false, color)
    }

    @JvmStatic
    fun drawGradientHorizontal(x: Float, y: Float, width: Float, height: Float, radius: Float, left: Color, right: Color) {
        drawGradientRound(x, y, width, height, radius, left, left, right, right)
    }

    @JvmStatic
    fun drawGradientVertical(x: Float, y: Float, width: Float, height: Float, radius: Float, top: Color, bottom: Color) {
        drawGradientRound(x, y, width, height, radius, bottom, top, bottom, top)
    }

    @JvmStatic
    fun drawGradientCornerLR(x: Float, y: Float, width: Float, height: Float, radius: Float, topLeft: Color, bottomRight: Color) {
        val mixedColor = ColorUtils.interpolateColorC(topLeft, bottomRight, 0.5f)
        drawGradientRound(x, y, width, height, radius, mixedColor, topLeft, bottomRight, mixedColor)
    }

    @JvmStatic
    fun drawGradientCornerRL(x: Float, y: Float, width: Float, height: Float, radius: Float, bottomLeft: Color, topRight: Color) {
        val mixedColor = ColorUtils.interpolateColorC(topRight, bottomLeft, 0.5f)
        drawGradientRound(x, y, width, height, radius, bottomLeft, mixedColor, mixedColor, topRight)
    }

    fun drawGradientRound(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        radius: Float,
        bottomLeft: Color,
        topLeft: Color,
        bottomRight: Color,
        topRight: Color
    ) {
        drawGradientRound(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius, bottomLeft, topLeft, bottomRight, topRight)
    }

    fun drawGradientRound(
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        radius: Float,
        bottomLeft: Color,
        topLeft: Color,
        bottomRight: Color,
        topRight: Color
    ) {
        drawGradientRound(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius, bottomLeft, topLeft, bottomRight, topRight)
    }

    fun drawGradientRound(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        bottomLeft: Color,
        topLeft: Color,
        bottomRight: Color,
        topRight: Color
    ) {
        setAlphaLimit(0f)
        resetColor()
        startBlend()
        roundedGradientShader.init()
        setupRoundedRectUniforms(x, y, width, height, radius, roundedGradientShader)
        //Top left
        roundedGradientShader.setUniformf("color1", topLeft.red / 255f, topLeft.green / 255f, topLeft.blue / 255f, topLeft.alpha / 255f)
        // Bottom Left
        roundedGradientShader.setUniformf("color2", bottomLeft.red / 255f, bottomLeft.green / 255f, bottomLeft.blue / 255f, bottomLeft.alpha / 255f)
        //Top Right
        roundedGradientShader.setUniformf("color3", topRight.red / 255f, topRight.green / 255f, topRight.blue / 255f, topRight.alpha / 255f)
        //Bottom Right
        roundedGradientShader.setUniformf("color4", bottomRight.red / 255f, bottomRight.green / 255f, bottomRight.blue / 255f, bottomRight.alpha / 255f)
        ShaderUtils.drawQuads(x - 1, y - 1, width + 2, height + 2)
        roundedGradientShader.unload()
        endBlend()
    }

    @JvmStatic
    fun drawRound(x: Float, y: Float, width: Float, height: Float, radius: Float, blur: Boolean, color: Color) {
        resetColor()
        startBlend()
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        setAlphaLimit(0f)
        roundedShader.init()

        setupRoundedRectUniforms(x, y, width, height, radius, roundedShader)
        roundedShader.setUniformi("blur", if (blur) 1 else 0)
        roundedShader.setUniformf("color", color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)

        ShaderUtils.drawQuads(x - 1, y - 1, width + 2, height + 2)
        roundedShader.unload()
        endBlend()
    }

    fun drawRoundOutline(x: Float, y: Float, width: Float, height: Float, radius: Float, outlineThickness: Float, color: Color, outlineColor: Color) {
        resetColor()
        startBlend()
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        setAlphaLimit(0f)
        roundedOutlineShader.init()
        val sr = ScaledResolution(Minecraft.getMinecraft())
        setupRoundedRectUniforms(x, y, width, height, radius, roundedOutlineShader)
        roundedOutlineShader.setUniformf("outlineThickness", outlineThickness * sr.scaleFactor)
        roundedOutlineShader.setUniformf("color", color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
        roundedOutlineShader.setUniformf("outlineColor", outlineColor.red / 255f, outlineColor.green / 255f, outlineColor.blue / 255f, outlineColor.alpha / 255f)
        ShaderUtils.drawQuads(x - (2 + outlineThickness), y - (2 + outlineThickness), width + (4 + outlineThickness * 2), height + (4 + outlineThickness * 2))
        roundedOutlineShader.unload()
        endBlend()
    }

    fun drawRoundTextured(x: Float, y: Float, width: Float, height: Float, radius: Float, alpha: Float) {
        resetColor()
        setAlphaLimit(0f)
        startBlend()
        roundedTexturedShader.init()
        roundedTexturedShader.setUniformi("textureIn", 0)
        setupRoundedRectUniforms(x, y, width, height, radius, roundedTexturedShader)
        roundedTexturedShader.setUniformf("alpha", alpha)
        ShaderUtils.drawQuads(x - 1, y - 1, width + 2, height + 2)
        roundedTexturedShader.unload()
        endBlend()
    }

    private fun setupRoundedRectUniforms(x: Float, y: Float, width: Float, height: Float, radius: Float, roundedTexturedShader: ShaderUtils) {
        val sr = ScaledResolution(Minecraft.getMinecraft())
        roundedTexturedShader.setUniformf("location", x * sr.scaleFactor, (Minecraft.getMinecraft().displayHeight - (height * sr.scaleFactor)) - (y * sr.scaleFactor))
        roundedTexturedShader.setUniformf("rectSize", width * sr.scaleFactor, height * sr.scaleFactor)
        roundedTexturedShader.setUniformf("radius", radius * sr.scaleFactor)
    }

    // This method colors the next avalible texture with a specified alpha value ranging from 0-1
    fun color(color: Int, alpha: Float) {
        val r = (color shr 16 and 255).toFloat() / 255.0f
        val g = (color shr 8 and 255).toFloat() / 255.0f
        val b = (color and 255).toFloat() / 255.0f
        GlStateManager.color(r, g, b, alpha)
    }

    // Colors the next texture without a specified alpha value
    fun color(color: Int) {
        color(color, (color shr 24 and 255).toFloat() / 255.0f)
    }

    // This will set the alpha limit to a specified value ranging from 0-1
    fun setAlphaLimit(limit: Float) {
        GlStateManager.enableAlpha()
        GlStateManager.alphaFunc(GL_GREATER, (limit * .01).toFloat())
    }

    fun drawImage(resourceLocation: String, x: Float, y: Float, imgWidth: Float, imgHeight: Float, color: Color) {
        startBlend()
        glColor(color)
        Minecraft.getMinecraft().textureManager.bindTexture(ResourceLocation(resourceLocation))
        Gui.drawModalRectWithCustomSizedTexture(x.toInt(), y.toInt(), 0f, 0f, imgWidth.toInt(), imgHeight.toInt(), imgWidth, imgHeight)
        endBlend()
    }

    fun drawImage(resourceLocation: ResourceLocation, x: Float, y: Float, imgWidth: Float, imgHeight: Float, color: Color) {
        startBlend()
        glColor(color)
        Minecraft.getMinecraft().textureManager.bindTexture(resourceLocation)
        Gui.drawModalRectWithCustomSizedTexture(x.toInt(), y.toInt(), 0f, 0f, imgWidth.toInt(), imgHeight.toInt(), imgWidth, imgHeight)
        endBlend()
    }

    fun drawImage(resourceLocation: ResourceLocation, x: Float, y: Float, imgWidth: Float, imgHeight: Float, color: Color, alpha: Float) {
        val red: Float = color.red / 255f
        val green: Float = color.green / 255f
        val blue: Float = color.blue / 255f
        startBlend()
        GlStateManager.color(red, green, blue, alpha / 255f)
        Minecraft.getMinecraft().textureManager.bindTexture(resourceLocation)
        Gui.drawModalRectWithCustomSizedTexture(x.toInt(), y.toInt(), 0f, 0f, imgWidth.toInt(), imgHeight.toInt(), imgWidth, imgHeight)
        endBlend()
    }

    fun startBlend() {
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }

    fun endBlend() {
        GlStateManager.disableBlend()
    }

}