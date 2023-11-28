package com.curseclient.client.utility.render.shader

import baritone.api.utils.Helper.mc
import com.curseclient.client.module.modules.client.ClickGui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11.*
import java.awt.Color


object GradientShader {
    private val shader = ShaderUtils("shaders/client/gradient.frag")
    private var framebuffer = Framebuffer(1, 1, false)

    fun setupUniforms(step: Float, speed: Float, color: Color, color2: Color, color3: Color, color4: Color, opacity: Float) {
        shader.setUniformi("texture", 0)
        shader.setUniformf("rgb", color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f)
        shader.setUniformf("rgb1", color2.red / 255.0f, color2.green / 255.0f, color2.blue / 255.0f)
        shader.setUniformf("rgb2", color3.red / 255.0f, color3.green / 255.0f, color3.blue / 255.0f)
        shader.setUniformf("rgb3", color4.red / 255.0f, color4.green / 255.0f, color4.blue / 255.0f)
        shader.setUniformf("step", 300 * step)
        shader.setUniformf("offset", (((System.currentTimeMillis().toDouble() * speed) % (mc.displayWidth * mc.displayHeight)) / 10.0f).toFloat())
        shader.setUniformf("mix", opacity)
    }

    fun setup() {
        setup(ClickGui.step.toFloat(), ClickGui.speed.toFloat(), ClickGui.getGradient()[0], ClickGui.getGradient()[1], ClickGui.getGradient()[2], ClickGui.getGradient()[3])
    }

    fun setup(opacity: Float) {
        setup(ClickGui.step.toFloat(), ClickGui.speed.toFloat(), ClickGui.getGradient()[0], ClickGui.getGradient()[1], ClickGui.getGradient()[2], ClickGui.getGradient()[3], opacity)
    }

    fun setup(step: Float, speed: Float, color: Color, color2: Color, color3: Color, color4: Color) {
        setup(step, speed, color, color2, color3, color4, 1.0f)
    }

    fun setup(step: Float, speed: Float, color: Color, color2: Color, color3: Color, color4: Color, opacity: Float) {
        GlStateManager.enableBlend()
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)

        framebuffer = createFrameBuffer(framebuffer)

        mc.framebuffer.bindFramebuffer(true)
        shader.init()
        setupUniforms(step, speed, color, color2, color3, color4, opacity)

        glBindTexture(GL_TEXTURE_2D, framebuffer.framebufferTexture)
    }

    private fun createFrameBuffer(framebuffer: Framebuffer?): Framebuffer {
        if (framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            framebuffer?.deleteFramebuffer()
            return Framebuffer(mc.displayWidth, mc.displayHeight, true)
        }
        return framebuffer
    }
    
    fun finish() {
        shader.unload()
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.bindTexture(0)
        glEnable(GL_BLEND)
    }
}