package com.curseclient.client.gui.impl.mainmenu.elements.alt

import com.curseclient.CurseClient
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.client.FMLClientHandler
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

// Lazy to fix (～￣▽￣)～
class Account(val accountName: String) {

    val dateAdded: Long = System.currentTimeMillis()
    constructor(accountName: String, dateAdded: Long) : this(accountName)
    class WrappedResource(val location: ResourceLocation)

    companion object {
        private val imageCache: HashMap<String, ResourceLocation> = HashMap()
        fun getTexture(name: String, format: String): ResourceLocation? {
            if (imageCache.containsKey(name)) {
                return imageCache[name]
            }
            val bufferedImage: BufferedImage
            try {
                bufferedImage = ImageIO.read(File(CurseClient.DIR,"/heads/$name.$format"))
            } catch (e: java.lang.Exception) {
                return null
            }
            val texture = DynamicTexture(bufferedImage)
            val wr = WrappedResource(FMLClientHandler.instance().client.textureManager.getDynamicTextureLocation("$name.$format", texture))
            imageCache[name] = wr.location
            return wr.location
        }
    }

}