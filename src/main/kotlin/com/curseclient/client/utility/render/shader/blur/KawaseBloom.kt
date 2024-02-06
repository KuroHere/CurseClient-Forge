package com.curseclient.client.utility.render.shader.blur

import com.curseclient.client.module.impls.visual.GlowESP.bindTexture
import com.curseclient.client.utility.render.RenderUtils2D.createFrameBuffer
import com.curseclient.client.utility.render.shader.RoundedUtil.setAlphaLimit
import com.curseclient.client.utility.render.shader.RoundedUtil.startBlend
import com.curseclient.client.utility.render.shader.ShaderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL14

object KawaseBloom {
    private val mc: Minecraft = Minecraft.getMinecraft()
    private var kawaseDown = ShaderUtils("kawaseDownBloom")
    private var kawaseUp = ShaderUtils("kawaseUpBloom")

    private var currentIterations = 0

    var framebuffer = Framebuffer(1, 1, true)
    private var stencilFramebuffer = Framebuffer(1, 1, false)
    private val framebufferList = mutableListOf<Framebuffer>()


    private fun initFramebuffers(iterations: Int) {
        framebufferList.forEach { it.deleteFramebuffer() }
        framebufferList.clear()
        //Have to make the framebuffer null so that it does not try to delete a framebuffer that has already been deleted
        framebuffer = createFrameBuffer(null, true)!!
        framebufferList.add(framebuffer)

        for (i in 0 until iterations) {
            val currentBuffer = Framebuffer(mc.displayWidth shr i, mc.displayHeight shr i, true)
            currentBuffer.setFramebufferFilter(GL11.GL_LINEAR)
            GlStateManager.bindTexture(currentBuffer.framebufferTexture)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL14.GL_MIRRORED_REPEAT)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL14.GL_MIRRORED_REPEAT)
            GlStateManager.bindTexture(0)
            framebufferList.add(currentBuffer)
        }
    }

    fun glBloom(data: Runnable, iterations: Int, offset: Int) {
        stencilFramebuffer = createFrameBuffer(stencilFramebuffer)
        stencilFramebuffer.framebufferClear()
        stencilFramebuffer.bindFramebuffer(false)
        data.run()
        stencilFramebuffer.unbindFramebuffer()
        renderBlur(stencilFramebuffer.framebufferTexture, iterations, offset)
    }

    fun renderBlur(framebufferTexture: Int, iterations: Int, offset: Int) {
        if (currentIterations != iterations || (framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight)) {
            initFramebuffers(iterations)
            currentIterations = iterations
        }
        setAlphaLimit(0f)
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE)
        GL11.glClearColor(0f, 0f, 0f, 0f)
        renderFBO(framebufferList[1], framebufferTexture, kawaseDown, offset.toFloat())

        //Downsample
        for (i in 1 until iterations) {
            renderFBO(
                framebufferList[i + 1],
                framebufferList[i].framebufferTexture,
                kawaseDown,
                offset.toFloat()
            )
        }
        //Upsample
        for (i in iterations downTo 2) {
            renderFBO(
                framebufferList[i - 1],
                framebufferList[i].framebufferTexture,
                kawaseUp,
                offset.toFloat()
            )
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
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE16)
        bindTexture(framebufferTexture)
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0)
        bindTexture(framebufferList[1].framebufferTexture)
        ShaderUtils.drawQuads()
        kawaseUp.unload()
        GL11.glClearColor(0f, 0f, 0f, 0f)
        mc.framebuffer.bindFramebuffer(false)
        bindTexture(framebufferList[0].framebufferTexture)
        setAlphaLimit(0f)
        startBlend()
        ShaderUtils.drawQuads()
        GlStateManager.bindTexture(0)
        setAlphaLimit(0f)
        startBlend()
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