package com.curseclient.client.utility.render.shader

import baritone.api.utils.Helper.mc
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.mixin.accessor.render.AccessorShaderGroup
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11.*
import java.awt.Color


/**
 * @author Surge
 * @since 27/07/22
 */
@SideOnly(Side.CLIENT)
object BlurUtil {

    private var lastScale = -1
    private var lastScaleWidth = -1
    private var lastScaleHeight = -1
    private var framebuffer: Framebuffer? = null
    private var blurShader: ShaderGroup? = null

    private fun checkScale(scaleFactor: Int, widthFactor: Int, heightFactor: Int) {
        if (lastScale != scaleFactor || lastScaleWidth != widthFactor || lastScaleHeight != heightFactor || framebuffer == null || blurShader == null) {
            try {
                blurShader = ShaderGroup(
                    mc.textureManager, mc.resourceManager, mc.framebuffer, ResourceLocation("shaders/post/blur.json")
                )
                blurShader!!.createBindFramebuffers(mc.displayWidth, mc.displayHeight)
                framebuffer = (blurShader as AccessorShaderGroup?)!!.getMainFramebuffer()
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        lastScale = scaleFactor
        lastScaleWidth = widthFactor
        lastScaleHeight = heightFactor
    }

    fun drawBlurryRect(x: Float, y: Float, x1: Float, y1: Float, intensity: Float, color: Color) {
        rect(
            x,
            y,
            x1 - 0.5f,
            y1 - 0.5f,
            Color(color.rgb)
        )
        blur(
            x,
            y,

            x1 - x,
            y1 - y,
            intensity
        )
    }

    fun rect(startX: Float, startY: Float, endX: Float, endY: Float, color: Color) {
        val alpha = color.alpha.toFloat() / 255.0f
        val red = color.red.toFloat() / 255.0f
        val green = color.green.toFloat() / 255.0f
        val blue = color.blue.toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(startX.toDouble(), endY.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(endX.toDouble(), endY.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(endX.toDouble(), startY.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(startX.toDouble(), startY.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun blur(x: Float, y: Float, width: Float, height: Float, intensity: Float) {
        val resolution = ScaledResolution(mc)
        val currentScale = resolution.scaleFactor
        checkScale(currentScale, resolution.scaledWidth, resolution.scaledHeight)

        if (OpenGlHelper.isFramebufferEnabled()) {
            RenderUtils2D.pushScissor(x, y, width, height)

            (blurShader as AccessorShaderGroup?)!!.getListShaders()[0]?.shaderManager?.getShaderUniform("Radius")!!.set(
                intensity
            )

            (blurShader as AccessorShaderGroup?)!!.getListShaders()[1]?.shaderManager?.getShaderUniform("Radius")!!.set(
                intensity
            )

            (blurShader as AccessorShaderGroup?)!!.getListShaders()[0]?.shaderManager?.getShaderUniform("BlurDir")!!.set(
                0f, 1f
            )

            (blurShader as AccessorShaderGroup?)!!.getListShaders()[1]?.shaderManager?.getShaderUniform("BlurDir")!!.set(
                1f, 1f
            )

            framebuffer!!.bindFramebuffer(true)

            blurShader!!.render(mc.renderPartialTicks)

            mc.framebuffer.bindFramebuffer(true)

            RenderUtils2D.popScissor()

            GlStateManager.enableBlend()
            GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ZERO, GL_ONE)

            framebuffer!!.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false)

            GlStateManager.disableBlend()
            glScalef(currentScale.toFloat(), currentScale.toFloat(), 0f)
        }
    }

}