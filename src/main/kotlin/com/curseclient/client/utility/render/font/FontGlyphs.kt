package com.curseclient.client.utility.render.font

import com.curseclient.client.utility.render.texture.MipmapTexture
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL14
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.min

class FontGlyphs(private val font: Font, private val fallbackFont: Font) {
    private val chunkMap = HashMap<Int, GlyphChunk>()

    val fontHeight: Float

    init {
        fontHeight = loadGlyphChunk(0)?.let { chunk ->
            chunkMap[0] = chunk
            chunk.charInfoArray.maxByOrNull { it.height }?.height?.toFloat() ?: 64.0f
        } ?: 64.0f
    }

    fun getCharInfo(char: Char): CharInfo {
        val charInt = char.code
        val chunk = charInt shr 8
        val chunkStart = chunk shl 8
        return getChunk(chunk).charInfoArray[charInt - chunkStart]
    }

    fun getChunk(char: Char) = getChunk(char.code shr 8)

    private fun getChunk(chunk: Int): GlyphChunk = chunkMap.getOrPut(chunk) {
        loadGlyphChunk(chunk) ?: return chunkMap[0]!!
    }

    fun destroy() {
        for (chunk in chunkMap.values) {
            chunk.texture.deleteTexture()
        }
        chunkMap.clear()
    }

    private fun loadGlyphChunk(chunk: Int): GlyphChunk? {
        return try {
            val chunkStart = chunk shl 8
            val bufferedImage = BufferedImage(TEXTURE_WIDTH, MAX_TEXTURE_HEIGHT, BufferedImage.TYPE_INT_ARGB)
            val graphics2D = bufferedImage.graphics as Graphics2D
            graphics2D.background = Color(0, 0, 0, 0)

            var rowHeight = 0
            var positionX = 1
            var positionY = 1

            val builderArray = Array(256) {
                val char = (chunkStart + it).toChar()
                val charImage = getCharImage(char)

                if (positionX + charImage.width >= TEXTURE_WIDTH) {
                    positionX = 1
                    positionY += rowHeight
                    rowHeight = 0
                }

                val builder = CharInfoBuilder(positionX, positionY, charImage.width, charImage.height)
                rowHeight = max(charImage.height, rowHeight)
                graphics2D.drawImage(charImage, positionX, positionY, null)

                positionX += charImage.width + 2
                builder
            }

            val textureHeight = min(ceilToPOT(positionY + rowHeight), MAX_TEXTURE_HEIGHT)
            val textureImage = BufferedImage(TEXTURE_WIDTH, textureHeight, BufferedImage.TYPE_INT_ARGB)
            (textureImage.graphics as Graphics2D).drawImage(bufferedImage, 0, 0, null)

            val texture = createTexture(textureImage)
            val charInfoArray = builderArray.map { it.build(textureHeight.toDouble()) }.toTypedArray()
            GlyphChunk(chunk, texture, charInfoArray)

        } catch (e: Exception) {
            null
        }
    }

    private fun ceilToPOT(valueIn: Int): Int {
        var i = valueIn
        i--
        i = i or (i shr 1)
        i = i or (i shr 2)
        i = i or (i shr 4)
        i = i or (i shr 8)
        i = i or (i shr 16)
        i++
        return i
    }

    private fun getCharImage(char: Char): BufferedImage {
        val font = when {
            font.canDisplay(char.code) -> font
            fallbackFont.canDisplay(char.code) -> fallbackFont
            else -> font
        }

        val tempGraphics2D = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics()
        tempGraphics2D.font = font
        val fontMetrics = tempGraphics2D.fontMetrics
        tempGraphics2D.dispose()

        val charWidth = if (fontMetrics.charWidth(char) > 0) fontMetrics.charWidth(char) else 8
        val charHeight = if (fontMetrics.height > 0) fontMetrics.height else font.size

        val charImage = BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics2D = charImage.createGraphics()

        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics2D.font = font
        graphics2D.color = Color.WHITE
        graphics2D.drawString(char.toString(), 0, fontMetrics.ascent)
        graphics2D.dispose()

        return charImage
    }

    private fun createTexture(bufferedImage: BufferedImage) = MipmapTexture(bufferedImage, GL11.GL_ALPHA, 4).apply {
        bindTexture()
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR)
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0.0f)
        unbindTexture()
    }

    class CharInfoBuilder(val posX: Int, val posY: Int, val width: Int, val height: Int) {
        fun build(textureHeight: Double): CharInfo {
            return CharInfo(
                width.toDouble(),
                height.toDouble(),
                posX / TEXTURE_WIDTH_DOUBLE,
                posY / textureHeight,
                (posX + width) / TEXTURE_WIDTH_DOUBLE,
                (posY + height) / textureHeight
            )
        }
    }

    companion object {
        var assumeNonVolatile: Boolean = false
        const val TEXTURE_WIDTH = 1024
        const val TEXTURE_WIDTH_DOUBLE = 1024.0
        const val MAX_TEXTURE_HEIGHT = 4096
    }
}