package com.curseclient.client.utility.render.texture

import com.curseclient.client.utility.render.texture.TextureUtils.scaleDownPretty
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.glTexParameteri
import org.lwjgl.opengl.GL12.*
import java.awt.image.BufferedImage

class MipmapTexture(bufferedImage: BufferedImage, format: Int, levels: Int) {
    var textureID: Int = -1; private set
    val width = bufferedImage.width
    val height = bufferedImage.height

    private fun genTexture() {
        textureID = GL11.glGenTextures()
    }

    fun bindTexture() {
        if (textureID != -1) {
            GlStateManager.bindTexture(textureID)
        }
    }

    fun unbindTexture() {
        GlStateManager.bindTexture(0)
    }

    fun deleteTexture() {
        if (textureID != -1) {
            GlStateManager.deleteTexture(textureID)
            textureID = -1
        }
    }

    override fun equals(other: Any?) =
        this === other
            || other is MipmapTexture
            && this.textureID == other.textureID

    override fun hashCode() = textureID

    init {
        // Generate texture id and bind it
        genTexture()
        bindTexture()

        // Setup mipmap levels
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_LOD, 0)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LOD, levels)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, levels)

        // Generate level 0 (original size) texture
        TextureUtils.uploadImage(bufferedImage, 0, format, width, height)

        // Generate mipmaps
        if (levels > 0) {
            for (i in 1..levels) {
                val newWidth = width shr i
                val newHeight = height shr i
                val scaled = bufferedImage.scaleDownPretty(newWidth, newHeight)

                TextureUtils.uploadImage(scaled, i, format, newWidth, newHeight)
            }
        }

        // Unbind texture
        unbindTexture()
    }
}