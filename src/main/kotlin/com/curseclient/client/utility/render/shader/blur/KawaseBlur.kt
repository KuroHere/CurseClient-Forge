package com.curseclient.client.utility.render.shader.blur

import baritone.api.utils.Helper.mc
import com.curseclient.client.utility.render.RenderUtils2D.createFrameBuffer
import com.curseclient.client.utility.render.shader.RoundedUtil.setAlphaLimit
import com.curseclient.client.utility.render.shader.RoundedUtil.startBlend
import com.curseclient.client.utility.render.shader.ShaderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.bindTexture
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL14
import kotlin.math.pow

object KawaseBlur {

    var kawaseDown = ShaderUtils("kawaseDown")
    var kawaseUp = ShaderUtils("kawaseUp")
    var framebuffer = Framebuffer(1, 1, false)
    private var currentIterations: Int = 0
    private val framebufferList = mutableListOf<Framebuffer>()
    private var stencilFramebuffer = Framebuffer(1, 1, false)

    fun setupUniforms(offset: Float) {
        kawaseDown.setUniformf("offset", offset, offset)
        kawaseUp.setUniformf("offset", offset, offset)
    }

    private fun initFramebuffers(iterations: Float) {
        framebufferList.forEach { it.deleteFramebuffer() }
        framebufferList.clear()

        framebufferList.add(createFrameBuffer(null))

        for (i in 1..iterations.toInt()) {
            val currentBuffer = Framebuffer(
                (mc.displayWidth / 2.0.pow(i)).toInt(),
                (mc.displayHeight / 2.0.pow(i)).toInt(),
                false
            )
            currentBuffer.setFramebufferFilter(GL11.GL_LINEAR)

            GlStateManager.bindTexture(currentBuffer.framebufferTexture)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL14.GL_MIRRORED_REPEAT)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL14.GL_MIRRORED_REPEAT)
            GlStateManager.bindTexture(0)

            framebufferList.add(currentBuffer)
        }
    }

    fun glBlur(data: Runnable, iterations: Int, offset: Int) {
        stencilFramebuffer = createFrameBuffer(stencilFramebuffer)
        stencilFramebuffer.framebufferClear()
        stencilFramebuffer.bindFramebuffer(false)
        data.run()
        stencilFramebuffer.unbindFramebuffer()
        renderBlur(stencilFramebuffer.framebufferTexture, iterations, offset)
    }

    private fun renderBlur(stencilFrameBufferTexture: Int, iterations: Int, offset: Int) {
        if (currentIterations != iterations || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            initFramebuffers(iterations.toFloat())
            currentIterations = iterations
        }

        renderFBO(framebufferList[1], mc.framebuffer.framebufferTexture, kawaseDown, offset.toFloat())

        for (i in 1 until iterations) {
            renderFBO(framebufferList[i + 1], framebufferList[i].framebufferTexture, kawaseDown, offset.toFloat())
        }

        for (i in iterations downTo 2) {
            renderFBO(framebufferList[i - 1], framebufferList[i].framebufferTexture, kawaseUp, offset.toFloat())
        }

        val lastBuffer = framebufferList[0]
        lastBuffer.framebufferClear()
        lastBuffer.bindFramebuffer(false)
        kawaseUp.init()
        kawaseUp.setUniformf("offset", offset.toFloat(), offset.toFloat())
        kawaseUp.setUniformi("inTexture", 0)
        kawaseUp.setUniformi("check", 1)
        kawaseUp.setUniformi("textureToCheck", 16)
        kawaseUp.setUniformf("halfpixel", 1.0f / lastBuffer.framebufferWidth, 1.0f / lastBuffer.framebufferHeight)
        kawaseUp.setUniformf("iResolution", lastBuffer.framebufferWidth.toFloat(), lastBuffer.framebufferHeight.toFloat())
        GL13.glActiveTexture(GL13.GL_TEXTURE16)
        bindTexture(stencilFrameBufferTexture)
        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        bindTexture(framebufferList[1].framebufferTexture)
        ShaderUtils.drawQuads()
        kawaseUp.unload()

        mc.framebuffer.bindFramebuffer(true)
        bindTexture(framebufferList[0].framebufferTexture)
        setAlphaLimit(0f)
        startBlend()
        ShaderUtils.drawQuads()
        GlStateManager.bindTexture(0)
    }

    private fun renderFBO(framebuffer: Framebuffer, framebufferTexture: Int, shader: ShaderUtils, offset: Float) {
        framebuffer.framebufferClear()
        framebuffer.bindFramebuffer(false)
        shader.init()
        bindTexture(framebufferTexture)
        shader.setUniformf("offset", offset, offset)
        shader.setUniformi("inTexture", 0)
        shader.setUniformi("check", 0)
        shader.setUniformf("halfpixel", 1.0f / framebuffer.framebufferWidth, 1.0f / framebuffer.framebufferHeight)
        shader.setUniformf("iResolution", framebuffer.framebufferWidth.toFloat(), framebuffer.framebufferHeight.toFloat())
        ShaderUtils.drawQuads()
        shader.unload()
    }
}