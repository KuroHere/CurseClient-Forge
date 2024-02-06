package com.curseclient.client.module.impls.combat

import com.curseclient.client.event.events.RootEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.Timer
import com.curseclient.client.utility.world.CrystalUtils
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.item.Item
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import java.util.*
import java.util.stream.Collectors

object Offhand: Module(
    "OffHand",
    "Manage your offhand",
    Category.COMBAT
) {

    private val totem by setting("Totem", true)
    private val gapple by setting("Gapple", true)
    private val crystal by setting("Crystal", true)

    private val delay by setting("Delay", 0.0, 0.0, 5.0, 0.05)

    private val hotbarTotem by setting("HotbarTotem", false)

    private val totemHealthThreshold by setting("TotemHealth", 5.0, 0.0, 36.0, 0.5)
    private val rightClick by setting("RightClickGap", true, visible = { gapple })
    private val crystalCheck by setting("CrystalCheck", CrystalCheck.DAMAGE)
    private val crystalRange by setting("CrystalRange", 10.0, 1.0, 15.0, 1.0, visible = { crystalCheck != CrystalCheck.NONE })
    private val fallCheck by setting("FallCheck", true)
    private val fallDist by setting("FallDist", 15.0, 0.0, 50.0, 1.0, visible = { fallCheck })
    private val totemOnElytra by setting("TotemOnElytra", true)
    private val extraSafe by setting("ExtraSafe", false)

    private val clearAfter by setting("ClearAfter", true)
    private val hard by setting("Hard", false)
    private val notFromHotbar by setting("NotFromHotbar", true)
    private val defaultItem by setting("DefaultItem", Default.TOTEM)


    private val clickQueue: Queue<Int> = LinkedList()

    private val timer: Timer = Timer()
    private enum class CrystalCheck {
        NONE,
        DAMAGE,
        RANGE
    }

    private enum class Default(var item: Item) {
        TOTEM(Items.TOTEM_OF_UNDYING),
        CRYSTAL(Items.END_CRYSTAL),
        GAPPLE(Items.GOLDEN_APPLE),
        AIR(Items.AIR);
    }

    init {
        safeListener<RootEvent> {
            if (mc.player == null || mc.world == null) return@safeListener

            if (mc.currentScreen !is GuiContainer && mc.currentScreen !is GuiInventory) {
                if (!clickQueue.isEmpty()) {
                    if (!timer.hasPassed(delay * 100F)) return@safeListener
                    val slot = clickQueue.poll()
                    try {
                        timer.reset()
                        mc.playerController.windowClick(mc.player!!.inventoryContainer.windowId, slot, 0, ClickType.PICKUP, mc.player)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    if (!mc.player!!.inventory.itemStack.isEmpty) {
                        var index = 44
                        while (index >= 9) {
                            if (mc.player!!.inventoryContainer.getSlot(index).stack.isEmpty) {
                                mc.playerController.windowClick(0, index, 0, ClickType.PICKUP, mc.player)
                                return@safeListener
                            }
                            index--
                        }
                    }

                    if (totem) {
                        if (mc.player!!.health + mc.player!!.absorptionAmount <= totemHealthThreshold
                            || (totemOnElytra && mc.player!!.isElytraFlying)
                            || (fallCheck && mc.player!!.fallDistance >= fallDist && !mc.player!!.isElytraFlying)
                        ) {
                            putItemIntoOffhand(Items.TOTEM_OF_UNDYING)
                            return@safeListener
                        } else if (crystalCheck == CrystalCheck.RANGE) {
                            val crystal = mc.world!!.loadedEntityList.stream()
                                .filter { e -> e is EntityEnderCrystal && mc.player!!.getDistance(e) <= crystalRange }
                                .min(Comparator.comparing { c -> mc.player!!.getDistance(c) })
                                .orElse(null) as EntityEnderCrystal?

                            if (crystal != null) {
                                putItemIntoOffhand(Items.TOTEM_OF_UNDYING)
                                return@safeListener
                            }
                        } else if (crystalCheck == CrystalCheck.DAMAGE) {
                            var damage = 0.0f

                            val crystalsInRange = mc.world!!.loadedEntityList.stream()
                                .filter { e -> e is EntityEnderCrystal }
                                .filter { e -> mc.player!!.getDistance(e) <= crystalRange }
                                .collect(Collectors.toList())

                            for (entity in crystalsInRange) {
                                damage += CrystalUtils.calculateDamage(entity as EntityEnderCrystal, mc.player)
                            }

                            if (mc.player!!.health + mc.player!!.absorptionAmount - damage <= totemHealthThreshold) {
                                putItemIntoOffhand(Items.TOTEM_OF_UNDYING)
                                return@safeListener
                            }
                        }

                        if (extraSafe && crystalCheck()) {
                            putItemIntoOffhand(Items.TOTEM_OF_UNDYING)
                            return@safeListener
                        }
                    }

                    if (gapple && isSword(mc.player!!.heldItemMainhand.item)) {
                        if (rightClick && !mc.gameSettings.keyBindUseItem.isKeyDown) {
                            if (clearAfter) {
                                putItemIntoOffhand(defaultItem.item)
                            }
                            return@safeListener
                        }

                        putItemIntoOffhand(Items.GOLDEN_APPLE)
                        return@safeListener
                    }

                    if (crystal) {
                        if (CrystalAura.isEnabled()) {
                            putItemIntoOffhand(Items.END_CRYSTAL)
                            return@safeListener
                        } else if (clearAfter) {
                            putItemIntoOffhand(defaultItem.item)
                            return@safeListener
                        }
                    }

                    if (hard) {
                        putItemIntoOffhand(defaultItem.item)
                    }
                }
            }
        }
    }

    private fun isSword(item: Item) = item === Items.DIAMOND_SWORD || item === Items.IRON_SWORD || item === Items.GOLDEN_SWORD || item === Items.STONE_SWORD || item === Items.WOODEN_SWORD

    private fun findItemSlot(item: Item): Int {
        var itemSlot = -1
        val startSlot = if (notFromHotbar) 9 else 0

        for (i in startSlot until 36) {
            val stack = mc.player.inventory.getStackInSlot(i)

            if (!stack.isEmpty && stack.item == item) {
                itemSlot = i
                break
            }
        }

        return itemSlot
    }

    private fun putItemIntoOffhand(item: Item) {
        if (mc.player.heldItemOffhand.item === item) return
        val slot = findItemSlot(item)
        if (hotbarTotem && item === Items.TOTEM_OF_UNDYING) {
            for (i in 0..8) {
                val stack = mc.player.inventory.mainInventory[i]
                if (stack.item === Items.TOTEM_OF_UNDYING) {
                    if (mc.player.inventory.currentItem != i) {
                        mc.player.inventory.currentItem = i
                    }
                    return
                }
            }
        }
        if (slot != -1) {
            if (delay > 0f) {
                if (timer.hasPassed(delay * 100f)) {
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, if (slot < 9) slot + 36 else slot, 0, ClickType.PICKUP, mc.player)
                    timer.reset()
                } else {
                    clickQueue.add(if (slot < 9) slot + 36 else slot)
                }
                clickQueue.add(45)
                clickQueue.add(if (slot < 9) slot + 36 else slot)
            } else {
                timer.reset()
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, if (slot < 9) slot + 36 else slot, 0, ClickType.PICKUP, mc.player)
                try {
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP, mc.player)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, if (slot < 9) slot + 36 else slot, 0, ClickType.PICKUP, mc.player)
            }
        }
    }

    private fun crystalCheck(): Boolean {
        var cumDmg = 0f
        val damageValues = ArrayList<Float>()
        damageValues.add(calculateDamageAABB(mc.player.position.add(1, 0, 0)))
        damageValues.add(calculateDamageAABB(mc.player.position.add(-1, 0, 0)))
        damageValues.add(calculateDamageAABB(mc.player.position.add(0, 0, 1)))
        damageValues.add(calculateDamageAABB(mc.player.position.add(0, 0, -1)))
        damageValues.add(calculateDamageAABB(mc.player.position))
        for (damage in damageValues) {
            cumDmg += damage
            if (mc.player.health + mc.player.absorptionAmount - damage <= totemHealthThreshold) {
                return true
            }
        }
        return if (mc.player.health + mc.player.absorptionAmount - cumDmg <= totemHealthThreshold) {
            true
        } else false
    }

    private fun calculateDamageAABB(pos: BlockPos): Float {
        val crystalsInAABB = mc.world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB(pos)).filterIsInstance<EntityEnderCrystal>()
        var totalDamage = 0f
        for (crystal in crystalsInAABB) {
            totalDamage += CrystalUtils.calculateDamage(crystal.posX, crystal.posY, crystal.posZ, mc.player)
        }
        return totalDamage
    }
}