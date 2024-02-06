package com.curseclient.client.module.impls.client

import com.curseclient.client.event.listener.listener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityEnchantmentTable
import net.minecraftforge.fml.common.gameevent.TickEvent

object PerformancePlus : Module(
    "Performance+",
    "Boosts fps",
    Category.CLIENT
) {
    private val page by setting("Page", Page.Entity)

    private val playerRange by setting("Player Render Range", 32.0, 8.0, 64.0, 0.1, { page == Page.Entity })
    private val crystalRange by setting("Crystal Render Range", 32.0, 8.0, 64.0, 0.1, { page == Page.Entity })
    private val itemRange by setting("Item Render Range", 32.0, 8.0, 64.0, 0.1, { page == Page.Entity })
    private val tileEntityRange by setting("Tile Entity Range", 16.0, 8.0, 64.0, 0.1, { page == Page.Entity })

    val hideBlockParticles by setting("Hide Block Particles", false, { page == Page.World })
    val hideWeatherEffects by setting("Hide Weather Effects", false, { page == Page.World })
    private val textureDelay by setting("Texture Update Delay", 100.0, 0.0, 5000.0, 25.0, { page == Page.World })
    val fastLight by setting("Fast Light", false, { page == Page.World })
    val enchantmentTable by setting("Fast Enchantment Table", true, { page == Page.World })

    val fastOutline by setting("ESP Fast Outline", false, { page == Page.ESP })

    private var lastTextureUpdate = 0L
    private var lastLightMapUpdate = 0L

    private enum class Page {
        Entity,
        World,
        ESP
    }

    init {
        listener<TickEvent.ClientTickEvent> {
            if (!fastLight) return@listener
            if (it.phase != TickEvent.Phase.START) return@listener
            mc.gameSettings.gammaSetting = 1000f
        }
    }

    @JvmStatic
    fun getEntityRenderRange(entity: Entity): Double? {
        if (!isEnabled()) return null
        return when (entity) {
            is EntityPlayer -> playerRange
            is EntityEnderCrystal -> crystalRange
            is EntityItem -> itemRange
            else -> null
        }
    }

    @JvmStatic
    fun getTileEntityRenderRange(entity: TileEntity): Double? {
        if (!isEnabled()) return null
        if (enchantmentTable && entity is TileEntityEnchantmentTable) return 0.0

        return tileEntityRange
    }

    @JvmStatic
    fun shouldUpdateTextures(): Boolean {
        if (!isEnabled()) return true
        if (mc.player?.ticksExisted?.let { it < 100 } != false) return true

        val time = System.currentTimeMillis()

        val shouldUpdate = (time - lastTextureUpdate) > textureDelay
        if (shouldUpdate) lastTextureUpdate = time
        return shouldUpdate
    }

    @JvmStatic
    fun shouldUpdateLightMap(): Boolean {
        if (!isEnabled()) return true
        if (!fastLight) return true
        if (mc.player?.ticksExisted?.let { it < 100 } != false) return true

        val time = System.currentTimeMillis()

        val shouldUpdate = (time - lastLightMapUpdate) > 1000L
        if (shouldUpdate) lastLightMapUpdate = time
        return shouldUpdate
    }
}