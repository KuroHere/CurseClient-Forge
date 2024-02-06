package com.curseclient.client.module.impls.combat

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.PlayerHotbarSlotEvent
import com.curseclient.client.event.listener.runSafe
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.managers.HotbarManager
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.entity.flooredPosition
import com.curseclient.client.utility.items.firstItem
import com.curseclient.client.utility.items.hotbarSlots
import com.curseclient.client.utility.player.ChatUtils
import com.curseclient.client.utility.player.PacketUtils.send
import com.curseclient.client.utility.player.TargetingUtils
import com.curseclient.client.utility.world.HoleType
import com.curseclient.client.utility.world.HoleUtils
import com.curseclient.client.utility.world.HoleUtils.getNormalHole
import com.curseclient.client.utility.world.WorldUtils.getHitVec
import com.curseclient.client.utility.world.WorldUtils.getVisibleSides
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent

object HoleMiner : Module(
    "HoleMiner",
    "Mines your target's hole",
    Category.COMBAT
) {
    private val packetDelay by setting("Packet Delay", 2.0, 1.0, 30.0, 0.1)
    private val mineReach by setting("Mine Reach", 4.2, 3.0, 8.0, 0.1)
    private val targetReach by setting("Target Reach", 5.0, 3.0, 8.0, 0.1)
    private val targetPriority by setting("Target Priority", TargetPriority.Health)
    private val friends by setting("Friends", false)

    @Suppress("UNUSED")
    private enum class TargetPriority(val sortingFactor: SafeClientEvent.(entity: EntityLivingBase) -> Double) {
        Health({ player.getPositionEyes(1.0f).distanceTo(it.positionVector) }),
        Distance({ it.health.toDouble() })
    }

    private var breakInfo: BreakInfo? = null

    override fun onEnable() {
        runSafe {
            val target = TargetingUtils.getTargetList(players = true, friends, hostile = false, animal = false, invisible = true).filter { it.positionVector.distanceTo(player.getPositionEyes(1.0f)) < targetReach }.minByOrNull { targetPriority.sortingFactor(this, it) }
                ?: run {
                    ChatUtils.sendMessage("HoleMiner: No Target")
                    return@runSafe
                }

            val holeType = getNormalHole(target.flooredPosition)
            if (holeType != HoleType.OBSIDIAN) {
                ChatUtils.sendMessage("HoleMiner: Target is not in hole")
                return@runSafe
            }

            val minePos = HoleUtils.normalHole.mapNotNull { pos ->
                val side = getVisibleSides(pos).minByOrNull { side ->
                    getHitVec(pos, side).distanceTo(player.getPositionEyes(1.0f))
                } ?: return@mapNotNull  null

                pos to side
            }.minByOrNull {
                getHitVec(it.first, it.second).distanceTo(player.getPositionEyes(1.0f))
            } ?: run {
                ChatUtils.sendMessage("HoleMiner: No block to mine")
                return@runSafe
            }

            breakInfo = BreakInfo(minePos.first, minePos.second, target, target.flooredPosition)
            return
        }

        setEnabled(false)
    }

    override fun onDisable() {
        breakInfo = null
    }

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START) return@safeListener

            run {
                val info = breakInfo
                if (info == null) {
                    ChatUtils.sendMessage("HoleMiner: No block to mine")
                    return@run
                }

                if (!listOf(info.holePos, info.holePos.add(0, 1, 0)).contains(info.target.flooredPosition)) {
                    ChatUtils.sendMessage("HoleMiner: Target is out of hole")
                    return@run
                }

                if (getHitVec(info.minePosition, info.side).distanceTo(player.getPositionEyes(1.0f)) > mineReach + 1.0) {
                    ChatUtils.sendMessage("HoleMiner: No block to mine")
                    return@run
                }

                if (player.hotbarSlots.firstItem(Items.DIAMOND_PICKAXE) == null) {
                    ChatUtils.sendMessage("HoleMiner: No pickaxe!")
                    return@run
                }

                if (world.getBlockState(info.minePosition).block != Blocks.OBSIDIAN) {
                    ChatUtils.sendMessage("HoleMiner: Completed!")
                    return@run
                }

                if (player.hotbarSlots[HotbarManager.lastReportedSlot].stack.item != Items.DIAMOND_PICKAXE) {
                    return@safeListener
                }

                if (player.ticksExisted % packetDelay.toInt() != 0) {
                    return@safeListener
                }

                CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, info.minePosition, info.side).send()
                CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, info.minePosition, info.side).send()

                player.swingArm(EnumHand.MAIN_HAND)

                return@safeListener
            }

            setEnabled(false)
        }


        safeListener<PlayerHotbarSlotEvent>(-198) { event ->
            player.hotbarSlots.firstItem(Items.DIAMOND_PICKAXE)?.let {
                event.slot = it.hotbarSlot
            }
        }
    }

    private class BreakInfo(val minePosition: BlockPos, val side: EnumFacing, val target: EntityLivingBase, val holePos: BlockPos)
}