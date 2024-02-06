package com.curseclient.client.module.impls.visual

import com.curseclient.client.event.events.world.EventRenderSky
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.graphic.GLUtils.matrix
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
    private val skyMode by setting("SkyMode", SkyMode.CURSE, description = "Edit the skybox")
    private val skyColor by setting("SkyColor", Color(0, 127, 255), description = "Edit the skybox color (COLOR mode only)")
    private val skyGamma by setting("SkyGamma", 128, 1, 255, 1, visible = { skyMode == SkyMode.CURSE }, description = "Edit the skybox gamma")
    private val skyGammaEnd by setting("SkyGammaEnd", 40, 1, 255, 1, visible = { skyMode == SkyMode.END }, description = "Edit the skybox gamma (END mode only)")

    private val curseSkyTexture = ResourceLocation("textures/sky/night.jpg")
    private val END_SKY_TEXTURES = ResourceLocation("minecraft", "textures/environment/end_sky.png")

    init {
        safeListener<EventRenderSky> {
            if (skyMode != SkyMode.NORMAL) {
                it.cancel()
            }
            renderSky()
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
        when (skyMode) {
            SkyMode.CURSE -> {
                mc.renderManager.renderEngine.bindTexture(curseSkyTexture)
                needsTexture = true
            }
            SkyMode.END -> {
                mc.renderManager.renderEngine.bindTexture(END_SKY_TEXTURES)
                needsTexture = true
            }
            else -> null
        }
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer

        if (needsTexture) {
            GlStateManager.enableTexture2D()
        } else {
            GlStateManager.disableTexture2D()
        }

        repeat(6) {
            matrix {
                when (it) {
                    1 -> GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F)
                    2 -> GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F)
                    3 -> GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F)
                    4 -> GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F)
                    5 -> GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F)
                }

                if (needsTexture) {
                    buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
                } else {
                    buffer.begin(7, DefaultVertexFormats.POSITION_COLOR)
                }

                when (skyMode) {
                    SkyMode.CURSE -> {
                        buffer.pos(-100.0, -100.0, -100.0).tex(0.0, 0.0).color(skyGamma.toFloat(), skyGamma.toFloat(), skyGamma.toFloat(), 255f).endVertex()
                        buffer.pos(-100.0, -100.0, 100.0).tex(0.0, 2.0).color(skyGamma.toFloat(), skyGamma.toFloat(), skyGamma.toFloat(), 255f).endVertex()
                        buffer.pos(100.0, -100.0, 100.0).tex(2.0, 2.0).color(skyGamma.toFloat(), skyGamma.toFloat(), skyGamma.toFloat(), 255f).endVertex()
                        buffer.pos(100.0, -100.0, -100.0).tex(2.0, 0.0).color(skyGamma.toFloat(), skyGamma.toFloat(), skyGamma.toFloat(), 255f).endVertex()
                    }

                    SkyMode.COLOR -> {
                        buffer.pos(-100.0, -100.0, -100.0).color(skyColor.red, skyColor.green, skyColor.blue, 255).endVertex()
                        buffer.pos(-100.0, -100.0, 100.0).color(skyColor.red, skyColor.green, skyColor.blue, 255).endVertex()
                        buffer.pos(100.0, -100.0, 100.0).color(skyColor.red, skyColor.green, skyColor.blue, 255).endVertex()
                        buffer.pos(100.0, -100.0, -100.0).color(skyColor.red, skyColor.green, skyColor.blue, 255).endVertex()
                    }

                    SkyMode.END -> {
                        buffer.pos(-100.0, -100.0, -100.0).tex(0.0, 0.0).color(skyGammaEnd.toFloat(), skyGammaEnd.toFloat(), skyGammaEnd.toFloat(), 255f).endVertex()
                        buffer.pos(-100.0, -100.0, 100.0).tex(0.0, 16.0).color(skyGammaEnd.toFloat(), skyGammaEnd.toFloat(), skyGammaEnd.toFloat(), 255f).endVertex()
                        buffer.pos(100.0, -100.0, 100.0).tex(16.0, 16.0).color(skyGammaEnd.toFloat(), skyGammaEnd.toFloat(), skyGammaEnd.toFloat(), 255f).endVertex()
                        buffer.pos(100.0, -100.0, -100.0).tex(16.0, 0.0).color(skyGammaEnd.toFloat(), skyGammaEnd.toFloat(), skyGammaEnd.toFloat(), 255f).endVertex()
                    }

                    SkyMode.NONE -> {
                        buffer.pos(-100.0, -100.0, -100.0).color(10, 10, 10, 255).endVertex()
                        buffer.pos(-100.0, -100.0, 100.0).color(10, 10, 10, 255).endVertex()
                        buffer.pos(100.0, -100.0, 100.0).color(10, 10, 10, 255).endVertex()
                        buffer.pos(100.0, -100.0, -100.0).color(10, 10, 10, 255).endVertex()
                    }

                    SkyMode.NORMAL -> null
                }
                tessellator.draw()
            }
        }

        GlStateManager.disableBlend()
        GlStateManager.depthMask(true)
        GlStateManager.enableTexture2D()
        GlStateManager.enableAlpha()
    }

    enum class SkyMode {
        NORMAL, COLOR, CURSE, END, NONE
    }
}