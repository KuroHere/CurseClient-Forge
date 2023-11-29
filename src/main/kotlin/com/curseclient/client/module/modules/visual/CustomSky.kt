package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.events.EventRenderSky
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.texture.Texture
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import java.awt.Color

object CustomSky : Module(
    "CustomSky",
    "Custom your sky box (need fix)",
    Category.VISUAL
) {
    private val END_SKY_TEXTURES = ResourceLocation("textures/environment/end_sky.png")
    private val skyMode by setting("SkyMode", SkyMode.CURSE, description = "Edit the skybox")
    private val skyColor by setting("SkyColor", Color(0, 127, 255), description = "Edit the skybox color (COLOR mode only)")
    private val skyGamma by setting("SkyGamma", 128, 1, 255, 1, visible = { skyMode == SkyMode.CURSE || skyMode == SkyMode.RAINBOW }, description = "Edit the skybox gamma")
    private val skyGammaEnd by setting("SkyGammaEnd", 40, 1, 255, 1, visible = { skyMode == SkyMode.END }, description = "Edit the skybox gamma (END mode only)")

    private val curseSkyTexture = Texture("curse_sky.jpg")
    private val rainbowSkyTexture = Texture("spectrum.jpg")
    
    init {
        safeListener<EventRenderSky> { event ->
            if (skyMode != SkyMode.NORMAL) {
                event.cancel()
                renderSky()
            }
        }
    }

    /**
     * Similar GL code to the minecraft function #renderSkyEnd()
     */
    private fun renderSky() {
        GlStateManager.disableFog()
        GlStateManager.disableAlpha()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.depthMask(false)
        var needsTexture = false
        when (skyMode.name) {
            "CURSE" -> {
                curseSkyTexture.bind()
                needsTexture = true
            }
            "RAINBOW" -> {
                rainbowSkyTexture.bind()
                needsTexture = true
            }
            "END" -> {
                mc.renderManager.renderEngine.bindTexture(END_SKY_TEXTURES)
                needsTexture = true
            }
        }
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer

        if (needsTexture) {
            GlStateManager.enableTexture2D()
        } else {
            GlStateManager.disableTexture2D()
        }

        repeat(6) { k1 ->
            GlStateManager.pushMatrix()
            when (k1) {
                1 -> GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F)
                2 -> GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F)
                3 -> GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F)
                4 -> GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F)
                5 -> GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F)
            }

            if (needsTexture) {
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
            } else {
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
            }

            when (skyMode.name) {
                "CURSE", "RAINBOW" -> {
                    bufferbuilder.pos(-100.0, -100.0, -100.0).tex(0.0, 0.0).color(skyGamma.toFloat(), skyGamma.toFloat(), skyGamma.toFloat(), 255f).endVertex()
                    bufferbuilder.pos(-100.0, -100.0, 100.0).tex(0.0, 2.0).color(skyGamma.toFloat(), skyGamma.toFloat(), skyGamma.toFloat(), 255f).endVertex()
                    bufferbuilder.pos(100.0, -100.0, 100.0).tex(2.0, 2.0).color(skyGamma.toFloat(), skyGamma.toFloat(), skyGamma.toFloat(), 255f).endVertex()
                    bufferbuilder.pos(100.0, -100.0, -100.0).tex(2.0, 0.0).color(skyGamma.toFloat(), skyGamma.toFloat(), skyGamma.toFloat(), 255f).endVertex()
                }
                "COLOR" -> {
                    bufferbuilder.pos(-100.0, -100.0, -100.0).color(skyColor.red, skyColor.green, skyColor.blue, 255).endVertex()
                    bufferbuilder.pos(-100.0, -100.0, 100.0).color(skyColor.red, skyColor.green, skyColor.blue, 255).endVertex()
                    bufferbuilder.pos(100.0, -100.0, 100.0).color(skyColor.red, skyColor.green, skyColor.blue, 255).endVertex()
                    bufferbuilder.pos(100.0, -100.0, -100.0).color(skyColor.red, skyColor.green, skyColor.blue, 255).endVertex()
                }
                "END" -> {
                    bufferbuilder.pos(-100.0, -100.0, -100.0).tex(0.0, 0.0).color(skyGammaEnd.toFloat(), skyGammaEnd.toFloat(), skyGammaEnd.toFloat(), 255f).endVertex()
                    bufferbuilder.pos(-100.0, -100.0, 100.0).tex(0.0, 16.0).color(skyGammaEnd.toFloat(), skyGammaEnd.toFloat(), skyGammaEnd.toFloat(), 255f).endVertex()
                    bufferbuilder.pos(100.0, -100.0, 100.0).tex(16.0, 16.0).color(skyGammaEnd.toFloat(), skyGammaEnd.toFloat(), skyGammaEnd.toFloat(), 255f).endVertex()
                    bufferbuilder.pos(100.0, -100.0, -100.0).tex(16.0, 0.0).color(skyGammaEnd.toFloat(), skyGammaEnd.toFloat(), skyGammaEnd.toFloat(), 255f).endVertex()
                }
                "NONE" -> {
                    bufferbuilder.pos(-100.0, -100.0, -100.0).color(10, 10, 10, 255).endVertex()
                    bufferbuilder.pos(-100.0, -100.0, 100.0).color(10, 10, 10, 255).endVertex()
                    bufferbuilder.pos(100.0, -100.0, 100.0).color(10, 10, 10, 255).endVertex()
                    bufferbuilder.pos(100.0, -100.0, -100.0).color(10, 10, 10, 255).endVertex()
                }
            }

            tessellator.draw()
            GlStateManager.popMatrix()
        }

        GlStateManager.disableBlend()
        GlStateManager.depthMask(true)
        GlStateManager.enableTexture2D()
        GlStateManager.enableAlpha()
    }

    enum class SkyMode {
        NORMAL, COLOR, CURSE, RAINBOW, END, NONE
    }
}