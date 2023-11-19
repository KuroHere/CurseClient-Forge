package com.curseclient.client.utility.render

import com.curseclient.CurseClient
import com.curseclient.CurseClient.Companion.LOG
import com.curseclient.client.module.modules.client.HUD
import org.lwjgl.opengl.GL11
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

class IconUtils {
    companion object {
        fun getFavicon(): Array<ByteBuffer> {
            try {
                return arrayOf(
                    readImageToBuffer(IconUtils::class.java.getResourceAsStream("/assets/minecraft/textures/icons/icon32x.png")),
                    readImageToBuffer(IconUtils::class.java.getResourceAsStream("/assets/minecraft/textures/icons/icon32x.png")))
            } catch (e: IOException) {
                LOG.error("Couldn't set Windows Icon", e);
            }

            return emptyArray()

        }

        @Throws(IOException::class) fun readImageToBuffer(imageStream: InputStream): ByteBuffer {
            val bufferedImage = ImageIO.read(imageStream)
            val rgb = bufferedImage.getRGB(0, 0, bufferedImage.width, bufferedImage.height, null, 0, bufferedImage.width)
            val byteBuffer = ByteBuffer.allocate(4 * rgb.size)
            for (i in rgb) byteBuffer.putInt(i shl 8 or (i shr 24 and 255))
            byteBuffer.flip()
            return byteBuffer
        }
    }


}

