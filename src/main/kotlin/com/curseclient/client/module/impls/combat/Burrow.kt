package com.curseclient.client.module.impls.combat

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.entity.isInsideBlock
import com.curseclient.client.utility.extension.settingName
import com.curseclient.client.utility.items.HotbarSlot
import com.curseclient.client.utility.items.firstBlock
import com.curseclient.client.utility.items.firstItem
import com.curseclient.client.utility.items.hotbarSlots
import com.curseclient.client.utility.player.MovementUtils.centerPlayer
import com.curseclient.client.utility.player.MovementUtils.isInputting
import com.curseclient.client.utility.world.WorldUtils.getHitVecOffset
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent

object Burrow: Module(
    "Burrow",
    "Places block into self",
    Category.COMBAT
) {
    private val block by setting("Block", Mode.EnderChest)
    private val setbackDistance by setting("Setback Distance", 2.0, 1.0, 100.0, 0.5)
    private val delay by setting("Delay", 10.0, 1.0, 40.0, 1.0)
    private val sneakTrigger by setting("Sneak Trigger", false)
    private val packetSwing by setting("Packet Swing", false)
    private val center by setting("Center Before Place", false)
    private val holdOnCenter by setting("Hold On Center", false)
    private val autoDisable by setting("Auto Disable", false)

    private var placePos = BlockPos(0, 0, 0)
    private var firstPitch = 0f
    private var firstSlot = 0

    private var ticksAfterLastBurrow = 0

    private enum class Mode {
        Obsidian,
        EnderChest,
        Any
    }

    override fun getHudInfo() = block.settingName

    init {
        safeListener<TickEvent.ClientTickEvent> {
            ticksAfterLastBurrow++

            if (player.isInsideBlock) {
                if (holdOnCenter) player.centerPlayer()
                return@safeListener
            }

            if (!shouldBurrow()) return@safeListener

            if (center) {
                if (!player.centerPlayer()) return@safeListener
            }

            placePos = BlockPos(player.positionVector.x, player.positionVector.y - 0.5, player.positionVector.z)
            firstPitch = player.rotationPitch
            firstSlot = player.inventory.currentItem

            swapTo(getBlockSlot())

            player.setVelocity(0.0, 0.0, 0.0)
            connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY, player.posZ, true))

            connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 0.4199, player.posZ, false))
            connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 0.7531, player.posZ, false))
            connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 1.001, player.posZ, false))
            connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 1.1661, player.posZ, false))

            connection.sendPacket(CPacketPlayer.Rotation(player.rotationYaw, 90f, true))
            connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_SNEAKING))

            val vec = getHitVecOffset(EnumFacing.UP)
            connection.sendPacket(CPacketPlayerTryUseItemOnBlock(placePos, EnumFacing.UP, EnumHand.MAIN_HAND, vec.x.toFloat(), vec.y.toFloat(), vec.z.toFloat()))
            if (packetSwing) connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND)) else player.swingArm(EnumHand.MAIN_HAND)

            connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))
            connection.sendPacket(CPacketPlayer.Rotation(player.rotationYaw, firstPitch, false))

            connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY + setbackDistance, player.posZ, false))

            player.inventory.currentItem = firstSlot
            ticksAfterLastBurrow = 0
            if (autoDisable) setEnabled(false)
        }
    }

    private fun SafeClientEvent.swapTo(slot: HotbarSlot?){
        val slotNum = slot?.hotbarSlot
        if (slotNum !in 0..8) return
        if (slot != null){
            player.inventory.currentItem = slot.hotbarSlot
        }
        mc.playerController.updateController()
    }

    private fun SafeClientEvent.shouldBurrow(): Boolean{
        return player.onGround &&
            !player.isInWater &&
            (getBlockSlot() != null) &&
            !isInputting() &&
            !mc.gameSettings.keyBindJump.isKeyDown &&
            (!sneakTrigger || mc.gameSettings.keyBindSneak.isKeyDown) &&
            ticksAfterLastBurrow > delay
    }

    private fun SafeClientEvent.getBlockSlot(): HotbarSlot? {
        return when(block) {
            Mode.Obsidian -> player.hotbarSlots.firstBlock(Blocks.OBSIDIAN)
            Mode.EnderChest -> player.hotbarSlots.firstBlock(Blocks.ENDER_CHEST)
            Mode.Any -> player.hotbarSlots.firstItem<ItemBlock, HotbarSlot>()
        }
    }
}