package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.esp.ESPRenderer
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.tileentity.TileEntityEnderChest
import net.minecraft.tileentity.TileEntityShulkerBox
import java.awt.Color

// TODO: Shader thing
object StorageESP : Module(
    "StorageESP",
    "StorageESP",
    Category.VISUAL
) {
    private val chest by setting("Chest", true)
    private val chestFilled by setting("Chest Filled", Color(200, 100, 0, 60), { chest })
    private val chestOutline by setting("Chest Outline", Color(200, 100, 0, 100), { chest })

    private val enderchest by setting("EnderChest", true)
    private val enderChestFilled by setting("EnderChest Filled", Color(180, 0, 200, 60), { enderchest })
    private val enderChestOutline by setting("EnderChest Outline", Color(180, 0, 200, 100), { enderchest })

    private val shulker by setting("Shulker", true)
    private val shulkerFilled by setting("Shulker Filled", Color(180, 0, 200, 60), { shulker })
    private val shulkerOutline by setting("Shulker Outline", Color(180, 0, 200, 100), { shulker })

    private val renderer = ESPRenderer()

    init {
        safeListener<Render3DEvent> {
            world.tickableTileEntities.forEach {
                when (it) {
                    is TileEntityChest -> if (chest) renderer.put(it.pos, chestFilled, chestOutline)
                    is TileEntityEnderChest -> if (enderchest) renderer.put(it.pos, enderChestFilled, enderChestOutline)
                    is TileEntityShulkerBox -> if (shulker) renderer.put(it.pos, shulkerFilled, shulkerOutline)
                }
            }
            renderer.render()
        }
    }
}