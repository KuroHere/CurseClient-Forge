package com.curseclient.client.module.impls.misc

import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.*
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object ChestStealer : Module(
    "ChestStealer",
    "Automatically steals items from chest",
    Category.MISC
) {
    private val delay by setting("Steal Delay", 5.0, 1.0, 20.0, 1.0)

    private val any by setting("Any", true)
    private val weapon by setting("Weapon", true, visible = { !any })
    private val tool by setting("Tool", true, visible = { !any })
    private val armor by setting("Armor", true, visible = { !any })
    private val food by setting("Food", true, visible = { !any })
    private val block by setting("Block", true, visible = { !any })
    private val totem by setting("Totem", true, visible = { !any })

    private var cooldown = 0

    override fun onEnable() {
        cooldown = delay.toInt()
    }

    init {
        safeListener<ClientTickEvent> {
            cooldown--
            val container = player.openContainer
            if(container !is ContainerChest || cooldown > 0) return@safeListener

            for (slot in container.inventorySlots) {
                if(slot.stack.isNeeded()){
                    mc.playerController.windowClick(container.windowId, slot.slotIndex, 0, ClickType.QUICK_MOVE, player)
                    cooldown = delay.toInt()
                    break
                }
            }
        }
    }

    private fun ItemStack.isNeeded(): Boolean {
        val item = this.item

        if (item is ItemAir) return false
        if (any) return true

        return (((item is ItemSword || item is ItemAxe) && weapon)
            || (item is ItemTool && tool)
            || (item is ItemArmor && armor)
            || (item is ItemFood && food)
            || (item is ItemBlock && block))
            || (item == Items.TOTEM_OF_UNDYING && totem)
    }
}