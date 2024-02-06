package com.curseclient.client.utility.items

import baritone.api.utils.Helper.mc
import com.curseclient.client.event.listener.runSafe
import com.curseclient.mixin.accessor.entity.AccessorPlayerControllerMP
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.ClickType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketHeldItemChange


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

        fun searchSlot(inItem: Item, inventoryRegion: InventoryRegion): Int {
            var slot = -1
            for (i in inventoryRegion.getStart() until inventoryRegion.getBound()) {
                if (mc.player.inventory.getStackInSlot(i).item == inItem) {
                    slot = i
                    break
                }
            }
            return slot
        }

        fun switchToSlot(inSlot: Int, swap: Switch) {
            if (InventoryPlayer.isHotbar(inSlot)) {
                if (mc.player.inventory.currentItem != inSlot) {
                    when (swap) {
                        Switch.NORMAL -> {
                            mc.player.inventory.currentItem = inSlot
                            mc.player.connection.sendPacket(CPacketHeldItemChange(inSlot))
                        }
                        Switch.PACKET -> {
                            mc.player.connection.sendPacket(CPacketHeldItemChange(inSlot))
                            (mc.playerController as AccessorPlayerControllerMP).setCurrentPlayerItem(inSlot)
                            // (mc.playerController as IPlayerControllerMP).hookSyncCurrentPlayItem()
                        }

                        Switch.NONE -> null
                    }
                }
            }
        }

        fun switchToItem(inItem: Item, swap: Switch) {
            val slot = searchSlot(inItem, InventoryRegion.HOTBAR)
            switchToSlot(slot, swap)
        }
    }

    fun click(button: Int, type: ClickType, window: Int = 0) {
        runSafe {
            playerController.windowClick(window, slotId, button, type, player)
        }
    }
}

// based on slot ids from: https://c4k3.github.io/wiki.vg/images/1/13/Inventory-slots.png
enum class InventoryRegion(private val start: Int, private val bound: Int) {
    INVENTORY(0, 45),
    HOTBAR(0, 8),
    CRAFTING(80, 83),
    ARMOR(100, 103);

    fun getStart(): Int {
        return start
    }
    fun getBound(): Int {
        return bound
    }
}

enum class Switch {
    NORMAL,
    PACKET,
    NONE
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