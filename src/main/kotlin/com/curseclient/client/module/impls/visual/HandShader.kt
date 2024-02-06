package com.curseclient.client.module.impls.visual

import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.graphic.GLUtils.renderGL
import com.curseclient.client.utility.render.RenderTessellator.partialTicks
import com.curseclient.client.utility.render.shader.PostProcessingShader
import com.curseclient.client.utility.threads.runAsync
import com.curseclient.mixin.accessor.render.AccessorEntityRenderer
import com.curseclient.mixin.accessor.render.AccessorShaderGroup
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderSpecificHandEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.GL11
import java.awt.Color

object HandShader : Module(
    "HandShader",
    "Potato pc killer",
    Category.VISUAL
) {
    private val lineWidth by setting("Line Width", 1.0, 1.0, 8.0, 0.1)
    private val blurRadius by setting("Blur Radius", 0.0, 0.0, 16.0, 1.0)
    private val showOriginal by setting("Show Original", false)
    private val color by setting("Color", Color(80, 200, 200, 250))
    private val filledAlpha by setting("Filled Alpha", 0.4, 0.0, 1.0, 0.05)
    private val outlineAlpha by setting("Outline Alpha", 0.4, 0.0, 1.0, 0.05)

    private val shader = PostProcessingShader(ResourceLocation("shaders/post/esp_outline.json"), listOf("final"))
    private val buffer = shader.getFrameBuffer("final")

    private var isRendering = false

    init {
        safeListener<RenderSpecificHandEvent> {
            if (!showOriginal && !isRendering) it.isCanceled = true
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START) return@safeListener

            if (player.ticksExisted % 3 != 0) return@safeListener

            runAsync {
                updateShader()
            }
        }
    }

    @JvmStatic
    fun drawArm(pTicks: Float, pass: Int) = draw(pTicks, pass)

    private fun draw(pTicks: Float, pass: Int) {
        if (!isEnabled()) return
        // Clean up the frame buffer and bind it
        buffer?.framebufferClear()
        buffer?.bindFramebuffer(false)

        //draw
        isRendering = true // do not hide esp arm when Show Original is false
        (mc.entityRenderer as AccessorEntityRenderer).invokeRenderHand(pTicks, pass)
        isRendering = false

        renderGL {
            // Push matrix
            GlStateManager.matrixMode(GL11.GL_PROJECTION)
            GlStateManager.pushMatrix()
            GlStateManager.matrixMode(GL11.GL_MODELVIEW)
            GlStateManager.pushMatrix()

            shader.shader?.render(partialTicks)

            // Re-enable blend because shader rendering will disable it at the end
            GlStateManager.enableBlend()
            GlStateManager.disableDepth()

            // Draw it on the main frame buffer
            mc.framebuffer.bindFramebuffer(false)
            buffer?.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false)

            // Revert states
            GlStateManager.enableBlend()
            GlStateManager.enableDepth()
            GlStateManager.disableTexture2D()
            GlStateManager.depthMask(false)
            GlStateManager.disableCull()

            // Revert matrix
            GlStateManager.matrixMode(GL11.GL_PROJECTION)
            GlStateManager.popMatrix()
            GlStateManager.matrixMode(GL11.GL_MODELVIEW)
            GlStateManager.popMatrix()
        }
    }

    private fun updateShader() {
        val group = shader.shader ?: return
        val shaders = (group as AccessorShaderGroup).listShaders ?: return

        shaders.forEach { shader ->
            shader.shaderManager.getShaderUniform("color")?.set(color.red / 255f, color.green / 255f, color.blue / 255f)
            shader.shaderManager.getShaderUniform("filledAlpha")?.set(filledAlpha.toFloat())
            shader.shaderManager.getShaderUniform("outlineAlpha")?.set(outlineAlpha.toFloat())
            shader.shaderManager.getShaderUniform("width")?.set(lineWidth.toFloat())
            shader.shaderManager.getShaderUniform("Radius")?.set(blurRadius.toFloat())
        }
    }
}