package com.curseclient.client.utility.render.shader

import baritone.api.utils.Helper
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.shader.RoundedUtil.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager.resetColor
import java.awt.Color


object GradientUtil {
    private val gradientMaskShader: ShaderUtils = ShaderUtils("gradientMask")
    private val gradientShader: ShaderUtils = ShaderUtils("gradient")
    fun drawGradient(x: Float, y: Float, width: Float, height: Float, alpha: Float, bottomLeft: Color, topLeft: Color, bottomRight: Color, topRight: Color) {
        val sr = ScaledResolution(Helper.mc)
        setAlphaLimit(0f)
        resetColor()
        startBlend()
        gradientShader.init()
        gradientShader.setUniformf("location", x * sr.scaleFactor, Minecraft.getMinecraft().displayHeight - height * sr.scaleFactor - y * sr.scaleFactor)
        gradientShader.setUniformf("rectSize", width * sr.scaleFactor, height * sr.scaleFactor)
        // Bottom Left
        gradientShader.setUniformf("color1", bottomLeft.red / 255f, bottomLeft.green / 255f, bottomLeft.blue / 255f, alpha)
        //Top left
        gradientShader.setUniformf("color2", topLeft.red / 255f, topLeft.green / 255f, topLeft.blue / 255f, alpha)
        //Bottom Right
        gradientShader.setUniformf("color3", bottomRight.red / 255f, bottomRight.green / 255f, bottomRight.blue / 255f, alpha)
        //Top Right
        gradientShader.setUniformf("color4", topRight.red / 255f, topRight.green / 255f, topRight.blue / 255f, alpha)

        //Apply the gradient to whatever is put here
        ShaderUtils.drawQuads(x, y, width, height)
        gradientShader.unload()
        endBlend()
    }

    fun drawGradient(x: Float, y: Float, width: Float, height: Float, bottomLeft: Color, topLeft: Color, bottomRight: Color, topRight: Color) {
        val sr = ScaledResolution(Helper.mc)
        resetColor()
        startBlend()
        gradientShader.init()
        gradientShader.setUniformf("location", x * sr.scaleFactor, Minecraft.getMinecraft().displayHeight - height * sr.scaleFactor - y * sr.scaleFactor)
        gradientShader.setUniformf("rectSize", width * sr.scaleFactor, height * sr.scaleFactor)
        // Bottom Left
        gradientShader.setUniformf("color1", bottomLeft.red / 255f, bottomLeft.green / 255f, bottomLeft.blue / 255f, bottomLeft.alpha / 255f)
        //Top left
        gradientShader.setUniformf("color2", topLeft.red / 255f, topLeft.green / 255f, topLeft.blue / 255f, topLeft.alpha / 255f)
        //Bottom Right
        gradientShader.setUniformf("color3", bottomRight.red / 255f, bottomRight.green / 255f, bottomRight.blue / 255f, bottomRight.alpha / 255f)
        //Top Right
        gradientShader.setUniformf("color4", topRight.red / 255f, topRight.green / 255f, topRight.blue / 255f, topRight.alpha / 255f)

        //Apply the gradient to whatever is put here
        ShaderUtils.drawQuads(x, y, width, height)
        gradientShader.unload()
        endBlend()
    }

    fun drawGradientLR(x: Float, y: Float, width: Float, height: Float, alpha: Float, left: Color, right: Color) {
        drawGradient(x, y, width, height, alpha, left, left, right, right)
    }

    fun drawGradientTB(x: Float, y: Float, width: Float, height: Float, alpha: Float, top: Color, bottom: Color) {
        drawGradient(x, y, width, height, alpha, bottom, top, bottom, top)
    }

    fun applyGradientHorizontal(x: Float, y: Float, width: Float, height: Float, alpha: Float, left: Color, right: Color, content: Runnable) {
        applyGradient(x, y, width, height, alpha, left, left, right, right, content)
    }

    fun applyGradientVertical(x: Float, y: Float, width: Float, height: Float, alpha: Float, top: Color, bottom: Color, content: Runnable) {
        applyGradient(x, y, width, height, alpha, bottom, top, bottom, top, content)
    }

    fun applyGradientCornerRL(x: Float, y: Float, width: Float, height: Float, alpha: Float, bottomLeft: Color, topRight: Color, content: Runnable) {
        val mixedColor: Color = ColorUtils.interpolateColorC(topRight, bottomLeft, .5f)
        applyGradient(x, y, width, height, alpha, bottomLeft, mixedColor, mixedColor, topRight, content)
    }

    fun applyGradientCornerLR(x: Float, y: Float, width: Float, height: Float, alpha: Float, bottomRight: Color, topLeft: Color, content: Runnable) {
        val mixedColor: Color = ColorUtils.interpolateColorC(bottomRight, topLeft, .5f)
        applyGradient(x, y, width, height, alpha, mixedColor, topLeft, bottomRight, mixedColor, content)
    }

    fun applyGradient(x: Float, y: Float, width: Float, height: Float, alpha: Float, bottomLeft: Color, topLeft: Color, bottomRight: Color, topRight: Color, content: Runnable) {
        resetColor()
        startBlend()
        gradientMaskShader.init()
        val sr = ScaledResolution(Helper.mc)
        gradientMaskShader.setUniformf("location", x * sr.scaleFactor, Minecraft.getMinecraft().displayHeight - height * sr.scaleFactor - y * sr.scaleFactor)
        gradientMaskShader.setUniformf("rectSize", width * sr.scaleFactor, height * sr.scaleFactor)
        gradientMaskShader.setUniformf("alpha", alpha)
        gradientMaskShader.setUniformi("tex", 0)
        // Bottom Left
        gradientMaskShader.setUniformf("color1", bottomLeft.red / 255f, bottomLeft.green / 255f, bottomLeft.blue / 255f)
        //Top left
        gradientMaskShader.setUniformf("color2", topLeft.red / 255f, topLeft.green / 255f, topLeft.blue / 255f)
        //Bottom Right
        gradientMaskShader.setUniformf("color3", bottomRight.red / 255f, bottomRight.green / 255f, bottomRight.blue / 255f)
        //Top Right
        gradientMaskShader.setUniformf("color4", topRight.red / 255f, topRight.green / 255f, topRight.blue / 255f)

        //Apply the gradient to whatever is put here
        content.run()
        gradientMaskShader.unload()
        endBlend()
    }
}