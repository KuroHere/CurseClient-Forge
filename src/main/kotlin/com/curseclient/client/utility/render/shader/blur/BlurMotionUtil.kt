package com.curseclient.client.utility.render.shader.blur

import baritone.api.utils.Helper
import com.curseclient.client.module.impls.visual.MotionBlur.amount
import com.curseclient.mixin.accessor.render.AccessorShaderGroup
import com.google.gson.JsonSyntaxException
import net.minecraft.client.shader.Shader
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.util.function.Consumer


class BlurMotionUtil {
    private var shader: ShaderGroup? = null
    private var shaderBlur = 0f
    val blurFactor: Float
        get() = amount.toFloat() / 10

    fun getShader(): ShaderGroup? {
        if (shader == null) {
            shaderBlur = Float.NaN
            try {
                shader = ShaderGroup(Helper.mc.textureManager, Helper.mc.resourceManager, Helper.mc.framebuffer, location)
                shader!!.createBindFramebuffers(Helper.mc.displayWidth, Helper.mc.displayHeight)
            } catch (error: JsonSyntaxException) {
                logger.error("Could not load motion blur shader", error)
                return null
            } catch (error: IOException) {
                logger.error("Could not load motion blur shader", error)
                return null
            }
        }
        if (shaderBlur != blurFactor) {
            (shader as AccessorShaderGroup).listShaders.forEach(Consumer { shader: Shader ->
                val blendFactorUniform = shader.shaderManager.getShaderUniform("BlurFactor")
                blendFactorUniform?.set(blurFactor)
            })
            shaderBlur = blurFactor
        }
        return shader
    }

    companion object {
        val instance = BlurMotionUtil()
        private val location = ResourceLocation("minecraft:shaders/post/motion_blur.json")
        private val logger = LogManager.getLogger()
    }
}
