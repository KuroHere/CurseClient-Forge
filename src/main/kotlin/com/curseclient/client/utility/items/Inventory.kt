package com.curseclient.client.utility.items

import com.curseclient.client.event.listener.runSafe
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.ClickType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

fun InventoryPlayer.firstEmpty() =
    firstStack { it.isEmpty }

fun InventoryPlayer.firstStack(item: Item) =
    firstStack { it.item == item }

fun InventoryPlayer.firstStack(predicate: (stack: ItemStack) -> Boolean) =
    findStacks(predicate).firstOrNull()

fun InventoryPlayer.findStacks(predicate: (stack: ItemStack) -> Boolean): List<SlotInfo> =
    getStackList().filter { predicate(it.stack) }

fun InventoryPlayer.getStackList(): List<SlotInfo> =
    mutableListOf<SlotInfo>().apply {
        for (i in 0..35) {
            val stack = getStackInSlot(i)
            add(SlotInfo(if (i < 9) i + 36 else i, stack))
        }
    }

class SlotInfo(val slotId: Int, val stack: ItemStack) {
    companion object {
        val offhand get() = SlotInfo(45, Minecraft.getMinecraft().player.offhandSlot.stack)
    }

    fun click(button: Int, type: ClickType, window: Int = 0) {
        runSafe {
            playerController.windowClick(window, slotId, button, type, player)
        }
    }
}

fun ArmorSlot.click(button: Int, type: ClickType, window: Int = 0) =
    this.slot.click(button, type, window)

val ArmorSlot.slot get() =
    SlotInfo(this.index, Minecraft.getMinecraft().player.inventoryContainer.inventorySlots[this.index].stack)

enum class ArmorSlot(val index: Int) {
    HELMET(5),
    CHESTPLATE(6),
    LEGGINS(7),
    BOOTS(8)
}