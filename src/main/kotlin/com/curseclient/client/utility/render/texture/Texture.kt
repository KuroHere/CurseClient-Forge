package com.curseclient.client.utility.render.texture

import com.curseclient.client.utility.render.RenderUtils2D.drawTexture
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation

class Texture(name: String) {
    private val textureLocation: ResourceLocation = ResourceLocation("minecraft","textures/sky/$name")

    fun render(x: Float, y: Float, width: Float, height: Float, u: Float, v: Float, t: Float, s: Float) {
        bind()
        GlStateManager.enableTexture2D()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        drawTexture(x, y, width, height, u, v, t, s)
    }

    fun render(x: Float, y: Float, textureX: Float, textureY: Float, width: Float, height: Float) {
        bind()
        GlStateManager.enableTexture2D()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        drawTexture(x, y, textureX, textureY, width, height)
    }

    fun render(x: Float, y: Float, width: Float, height: Float) {
        render(x, y, width, height, 0f, 0f, 1f, 1f)
    }

    fun bind() {
        Minecraft.getMinecraft().textureManager.bindTexture(textureLocation)
    }
}
