package com.curseclient.client.utility.render.shader.blur

import com.curseclient.client.module.impls.visual.GlowESP.bindTexture
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.render.RenderUtils2D.createFrameBuffer
import com.curseclient.client.utility.render.StencilUtil
import com.curseclient.client.utility.render.shader.ShaderUtils
import com.curseclient.client.utility.render.shader.ShaderUtils.drawQuads
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.OpenGlHelper.glUniform1
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.BufferUtils.createFloatBuffer
import org.lwjgl.opengl.GL11.*

object GaussianBlur {

    var blurShader = ShaderUtils("shaders/client/gaussian.frag")
    var framebuffer = Framebuffer(1, 1, false)
    private val mc: Minecraft = Minecraft.getMinecraft()

    fun setupUniforms(dir1: Float, dir2: Float, radius: Float) {
        blurShader.setUniformi("textureIn", 0)
        blurShader.setUniformf("texelSize", 1.0F / mc.displayWidth.toFloat(), 1.0F / mc.displayHeight.toFloat())
        blurShader.setUniformf("direction", dir1, dir2)
        blurShader.setUniformf("radius", radius)

        val weightBuffer = createFloatBuffer(256)
        for (i in 0..radius.toInt()) {
            weightBuffer.put(MathUtils.calculateGaussianValue(i.toFloat(), radius / 2))
        }
        weightBuffer.rewind()
        glUniform1(blurShader.getUniform("weights"), weightBuffer)
    }

    //First way
    fun startBlur() {
        StencilUtil.initStencilToWrite()
    }

    fun endBlur(radius: Double, compression: Double) {
        endBlur(radius.toFloat(), compression.toFloat())
    }

    fun endBlur(radius: Float, compression: Float) {
        StencilUtil.readStencilBuffer(1)
        framebuffer = createFrameBuffer(framebuffer)
        framebuffer.framebufferClear()
        framebuffer.bindFramebuffer(false)
        blurShader.init()
        setupUniforms(compression, 0f, radius)
        bindTexture(mc.framebuffer.framebufferTexture)
        drawQuads()
        framebuffer.unbindFramebuffer()
        blurShader.unload()
        mc.framebuffer.bindFramebuffer(false)
        blurShader.init()
        setupUniforms(0f, compression, radius)
        bindTexture(framebuffer.framebufferTexture)
        drawQuads()
        blurShader.unload()
        StencilUtil.uninitStencilBuffer()
        resetColor()
        GlStateManager.bindTexture(0)
    }

    // Second way
    fun glBlur(data: Runnable, radius: Double, compression: Double) {
        glBlur(data, radius.toFloat(), compression.toFloat())
    }
    
    fun glBlur(data: Runnable, radius: Float, compression: Float) {
        StencilUtil.initStencilToWrite()
        data.run()
        StencilUtil.readStencilBuffer(1)
        renderBlur(radius, compression)
        StencilUtil.uninitStencilBuffer()
    }

    fun glDoubleDataBlur(data: Runnable, radius: Float, compression: Float) {
        StencilUtil.initStencilToWrite()
        data.run()
        StencilUtil.readStencilBuffer(1)
        renderBlur(radius, compression)
        StencilUtil.uninitStencilBuffer()
        data.run()
    }

    fun renderBlur(radius: Float, compression: Float) {
        GlStateManager.enableBlend()
        GlStateManager.color(1f, 1f, 1f, 1f)
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)

        framebuffer = createFrameBuffer(framebuffer)
        framebuffer.framebufferClear()
        framebuffer.bindFramebuffer(true)
        blurShader.init()
        setupUniforms(compression, 0f, radius)

        bindTexture(mc.framebuffer.framebufferTexture)
        drawQuads()
        framebuffer.unbindFramebuffer()
        blurShader.unload()

        mc.framebuffer.bindFramebuffer(true)
        blurShader.init()
        setupUniforms(0f, compression, radius)

        bindTexture(framebuffer.framebufferTexture)
        drawQuads()
        blurShader.unload()

        resetColor()
        GlStateManager.bindTexture(0)
    }
}
