package com.curseclient.client.module.impls.combat

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.PlayerHotbarSlotEvent
import com.curseclient.client.event.events.PlayerPacketEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.managers.HotbarManager
import com.curseclient.client.manager.managers.HotbarManager.updateSlot
import com.curseclient.client.manager.managers.PacketManager
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.items.HotbarSlot
import com.curseclient.client.utility.items.firstBlock
import com.curseclient.client.utility.items.hotbarSlots
import com.curseclient.client.utility.player.RotationUtils
import com.curseclient.client.utility.world.HoleType
import com.curseclient.client.utility.world.HoleUtils.checkHole
import com.curseclient.client.utility.world.HoleUtils.getHoleType
import com.curseclient.client.utility.world.getBlockSequence
import com.curseclient.client.utility.world.placement.NeighborUtils.getNeighbor
import com.curseclient.client.utility.world.placement.PlaceInfo
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos

object HoleFiller : Module(
    "HoleFiller",
    "Fills holes around player",
    Category.COMBAT
) {
    private val onlyInsideHole by setting("Only Inside Hole", true)
    private val visibilityCheck by setting("Visibility Check", true)
    private val placeDelay by setting("Place Delay", 4.0, 1.0, 20.0, 1.0)
    private val placeReach by setting("Place Reach", 4.25, 3.0, 6.0, 0.05)
    private val sendSneakPacket by setting("Sneak Packet", false)

    private var placeInfo: PlaceInfo? = null
    private var slot: Int? = null
    private var placeTicks = 0

    override fun onDisable() {
        placeInfo = null
        slot = null
        placeTicks = 0
    }

    init {
        safeListener<PlayerHotbarSlotEvent> { event ->
            slot?.let { event.slot = it }
        }

        safeListener<PlayerPacketEvent.Data> { event ->
            placeTicks++

            placeInfo = null
            slot = null

            val holes = getBlockSequence(
                BlockPos(player.positionVector),
                (placeReach + 2.0).toInt(),
                (placeReach).toInt()
            ).filter {
                val type = getHoleType(it)
                type == HoleType.OBSIDIAN || type == HoleType.BEDROCK

            }

            val type = checkHole(player)

            if (holes.isEmpty() ||
                player.hotbarSlots.firstBlock(Blocks.OBSIDIAN) == null ||
                ((type != HoleType.OBSIDIAN && type != HoleType.BEDROCK) && onlyInsideHole)) return@safeListener

            placeInfo = getPlaceInfo(holes) ?: return@safeListener

            RotationUtils.rotationsToVec(placeInfo!!.hitVec).let { event.yaw = it.x; event.pitch = it.y }
            slot = player.hotbarSlots.firstBlock(Blocks.OBSIDIAN)?.hotbarSlot
            updateSlot()
        }

        safeListener<PlayerPacketEvent.Post> {
            val s = player.hotbarSlots.firstBlock(Blocks.OBSIDIAN) ?: return@safeListener
            if (HotbarManager.lastReportedSlot != s.hotbarSlot) return@safeListener

            placeInfo?.let { doPlace(it, s) }
        }
    }


    private fun SafeClientEvent.getPlaceInfo(holeList: List<BlockPos>): PlaceInfo? {
        return holeList
            .mapNotNull { getNeighbor(it, 1, range = placeReach.toFloat(), visibleSideCheck = visibilityCheck) }
            .minByOrNull { player.getPositionEyes(1.0f).distanceTo(it.hitVec) }
    }

    private fun SafeClientEvent.doPlace(placeInfo: PlaceInfo, slot: HotbarSlot) {
        if (placeTicks < placeDelay) return

        if (!world.getBlockState(placeInfo.placedPos).material.isReplaceable) return

        val itemStack = slot.stack
        val block = (itemStack.item as? ItemBlock?)?.block ?: return
        val metaData = itemStack.metadata

        val sneak = !PacketManager.lastReportedSneaking && sendSneakPacket
        if (sneak) connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_SNEAKING))

        connection.sendPacket(placeInfo.getPlacePacket(EnumHand.MAIN_HAND))
        player.swingArm(EnumHand.MAIN_HAND)

        if (sneak) connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))

        val blockState = block.getStateForPlacement(world, placeInfo.pos, placeInfo.side, placeInfo.hitVecOffset.x.toFloat(), placeInfo.hitVecOffset.y.toFloat(), placeInfo.hitVecOffset.z.toFloat(), metaData, player, EnumHand.MAIN_HAND)
        val soundType = blockState.block.getSoundType(blockState, world, placeInfo.pos, player)
        world.playSound(player, placeInfo.pos, soundType.placeSound, SoundCategory.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f)
        world.setBlockState(placeInfo.placedPos, blockState)

        placeTicks = 0
    }
}