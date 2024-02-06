package com.curseclient.client.utility.render

import baritone.api.utils.Helper
import net.minecraft.client.Minecraft
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.EXTFramebufferObject
import org.lwjgl.opengl.EXTPackedDepthStencil
import org.lwjgl.opengl.GL11

object StencilUtil {
    var mc: Minecraft = Minecraft.getMinecraft()

    /*
     * Given to me by igs
     *
     */
    fun checkSetupFBO(framebuffer: Framebuffer?) {
        if (framebuffer != null) {
            if (framebuffer.depthBuffer > -1) {
                setupFBO(framebuffer)
                framebuffer.depthBuffer = -1
            }
        }
    }

    /**
     * @implNote Sets up the Framebuffer for Stencil use
     */
    fun setupFBO(framebuffer: Framebuffer) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(framebuffer.depthBuffer)
        val stencilDepthBufferID = EXTFramebufferObject.glGenRenderbuffersEXT()
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilDepthBufferID)
        EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT, mc.displayWidth, mc.displayHeight)
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilDepthBufferID)
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilDepthBufferID)
    }

    /**
     * @implNote Initializes the Stencil Buffer to write to
     */
    fun initStencilToWrite() {
        //init
        mc.framebuffer.bindFramebuffer(false)
        checkSetupFBO(mc.framebuffer)
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT)
        GL11.glEnable(GL11.GL_STENCIL_TEST)
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 1)
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE)
        GL11.glColorMask(false, false, false, false)
    }

    /**
     * @param ref (usually 1)
     * @implNote Reads the Stencil Buffer and stencils it onto everything until
     * @see StencilUtil.uninitStencilBuffer
     */
    fun readStencilBuffer(ref: Int) {
        GL11.glColorMask(true, true, true, true)
        GL11.glStencilFunc(GL11.GL_EQUAL, ref, 1)
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP)
    }

    fun uninitStencilBuffer() {
        GL11.glDisable(GL11.GL_STENCIL_TEST)
    }

    fun glStencil(data: Runnable) {
        initStencilToWrite()
        data.run()
        readStencilBuffer(1)
    }
}