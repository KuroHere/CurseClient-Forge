package com.curseclient.client.module.modules.combat

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.MoveEvent
import com.curseclient.client.event.events.PlayerHotbarSlotEvent
import com.curseclient.client.event.events.PlayerPacketEvent
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.*
import com.curseclient.client.manager.managers.HotbarManager
import com.curseclient.client.manager.managers.HotbarManager.updateSlot
import com.curseclient.client.manager.managers.PacketManager
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.entity.flooredPosition
import com.curseclient.client.utility.extension.entity.isCentered
import com.curseclient.client.utility.extension.settingName
import com.curseclient.client.utility.items.*
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.player.MovementUtils.centerPlayer
import com.curseclient.client.utility.player.MovementUtils.isInputting
import com.curseclient.client.utility.player.PacketUtils.send
import com.curseclient.client.utility.player.RotationUtils
import com.curseclient.client.utility.render.ColorUtils.setAlphaD
import com.curseclient.client.utility.render.esp.ESPRenderer
import com.curseclient.client.utility.threads.loop.DelayedLoopThread
import com.curseclient.client.utility.world.HoleUtils
import com.curseclient.client.utility.world.placement.NeighborUtils.getNeighbor
import com.curseclient.client.utility.world.placement.PlaceInfo
import net.minecraft.block.Block
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec2f
import java.awt.Color
import kotlin.math.abs
import kotlin.math.min

object Surround : Module(
    "Surround",
    "Surrounds you with obsidian blocks",
    Category.COMBAT
) {
    private val structure by setting("Structure", Structure.Normal)
    private val rotationMode by setting("Rotation Mode", RotationMode.Lazy)
    private val center by setting("Center", true)
    private val delay by setting("Place Delay", 10.0, 1.0, 50.0, 1.0)
    private val retryDelay by setting("Retry Delay", 200.0, 50.0, 1000.0, 5.0)
    private val placeReach by setting("Place Reach", 4.2, 2.0, 5.0, 0.1)
    private val maxRange by setting("Max Range", 3.0, 1.0, 4.0, 1.0)
    private val autoDisable by setting("Update Disable", true)
    private val updateState by setting("Update State", true)
    private val placeSound by setting("Place Sound", true)
    private val standingOnly by setting("Standing Only", true)
    private val sendSneakPacket by setting("Sneak Packet", false)
    private val overrideCrystals by setting("Override Crystals", false)
    private val maxCrystalTicks by setting("Max Crystal Ticks", 10.0, 1.0, 20.0, 1.0, { overrideCrystals })
    private val esp by setting("ESP", false)
    private val color by setting("Color", Color.GREEN, { esp })
    private val filledAlpha by setting("Filled Alpha", 0.25, 0.0, 1.0, 0.01, { esp })
    private val outlineAlpha by setting("Outline Alpha", 0.5, 0.0, 1.0, 0.01, { esp })
    private val duration by setting("Duration", 500.0, 100.0, 1000.0, 10.0, { esp })

    private var currentSlot: Int? = null
    private var currentRotation: Vec2f? = null
    private var lastPlaceTime = 0L

    private var shouldCenter = false

    private val placeMap = HashMap<BlockPos, Long>()
    private val espMap = HashMap<BlockPos, Long>()
    private val renderer = ESPRenderer()

    @Suppress("UNUSED")
    private enum class Structure(val offsetMap: Array<BlockPos>) {
        Normal(HoleUtils.normalHole),
        Full(HoleUtils.surroundOffsetFull),
        Trap( HoleUtils.surroundOffsetTrap)
    }

    private enum class RotationMode {
        Lazy,
        Strict,
        Packet
    }

    override fun getHudInfo() = structure.settingName

    private val loopThread = DelayedLoopThread("Surround Loop Thread", { isEnabled() && mc.world != null }, { min(5L, delay.toLong()) }) {
        onMainThreadSafe {
            shouldCenter = false

            placeMap.entries.removeIf {
                System.currentTimeMillis() - it.value > retryDelay
            }

            val prevStates = placeMap.keys.map {
                it to world.getBlockState(it)
            }

            placeMap.keys.map {
                world.setBlockState(it, Blocks.OBSIDIAN.defaultState)
            }

            val placeInfo = structure.offsetMap
                .map { it.add(BlockPos(player.posX, player.posY, player.posZ)) }
                .filter { world.getBlockState(it).material.isReplaceable }
                .firstNotNullOfOrNull { getNeighbor(it, maxRange.toInt(), range = placeReach.toFloat(), hitboxCheck = { pos ->
                    val box = AxisAlignedBB(pos)
                    return@getNeighbor world.getEntitiesWithinAABBExcludingEntity(null, box).all { entity ->
                        (entity is EntityItem) || (entity is EntityEnderCrystal && entity.ticksExisted <= maxCrystalTicks && overrideCrystals) || entity.isDead
                    }
                }) }

            prevStates.forEach {
                world.setBlockState(it.first, it.second)
            }

            val obsidianSlot = player.hotbarSlots.firstBlock(Blocks.OBSIDIAN)?.hotbarSlot

            if (placeInfo == null || obsidianSlot == null || !player.onGround || (isInputting() && standingOnly)) {
                reset()
                return@onMainThreadSafe
            }

            if (center && !player.isCentered(player.flooredPosition)) {
                shouldCenter = true
                reset()
                return@onMainThreadSafe
            }

            val rotation = RotationUtils.rotationsToVec(placeInfo.hitVec)

            currentSlot = obsidianSlot
            currentRotation = rotation

            if (rotationMode == RotationMode.Packet)
                currentRotation = null

            updateSlot()

            val serverSlot = player.hotbarSlots[HotbarManager.lastReportedSlot].stack
            if (serverSlot.item.block != Blocks.OBSIDIAN) return@onMainThreadSafe

            val serverRotation = Vec2f(PacketManager.lastReportedYaw, PacketManager.lastReportedPitch)
            if (!checkRotation(serverRotation, rotation)) return@onMainThreadSafe

            if (!checkDelay()) return@onMainThreadSafe

            if (rotationMode == RotationMode.Packet)
                packetRotate(rotation)

            doPlace(placeInfo, serverSlot.item.block, serverSlot.metadata)

            placeMap[placeInfo.placedPos] = System.currentTimeMillis()
            espMap[placeInfo.placedPos] = System.currentTimeMillis()

            lastPlaceTime = System.currentTimeMillis()
        }
    }

    init {
        loopThread.reload()

        safeListener<PlayerHotbarSlotEvent>(-200) { event ->
            currentSlot?.let { slot ->
                event.slot = slot
            }
        }

        safeListener<PlayerPacketEvent.Data>(-200) { event ->

            currentRotation?.let { rotation ->
                event.yaw = rotation.x
                event.pitch = rotation.y
            }
        }

        safeListener<MoveEvent>(-100) { event ->
            if (event.y > 0.4 && autoDisable) {
                Surround.setEnabled(false)
            }
            espMap.entries.removeIf {
                System.currentTimeMillis() - it.value > duration
            }

            if (!shouldCenter) return@safeListener
            player.centerPlayer()
        }

        safeListener<Render3DEvent> {
            if (!esp) return@safeListener
            espMap.entries.forEach {
                val alpha = 1.0 - clamp((System.currentTimeMillis() - it.value).toDouble() / duration, 0.0, 1.0)
                renderer.put(it.key, color.setAlphaD(filledAlpha * alpha), color.setAlphaD(outlineAlpha * alpha))
            }
            renderer.render()
        }
    }

    override fun onEnable() {
        placeMap.clear()
        espMap.clear()
        loopThread.interrupt()
    }

    override fun onDisable() {
        reset()
        lastPlaceTime = 0L
        shouldCenter = false
    }

    private fun reset() {
        currentSlot = null
        currentRotation = null
    }

    private fun checkDelay() =
        System.currentTimeMillis() - lastPlaceTime >= delay

    private fun checkRotation(current: Vec2f, target: Vec2f): Boolean {
        if (rotationMode != RotationMode.Strict) return true
        val maxDiff = 15f

        val yawDiff = abs(current.x - target.x) % 180.0f
        val pitchDiff = abs(current.y - target.y)

        return (yawDiff < maxDiff) && (pitchDiff < maxDiff)
    }

    private fun SafeClientEvent.packetRotate(rotation: Vec2f) {
        if (PacketManager.lastReportedYaw == rotation.x && PacketManager.lastReportedPitch == rotation.y)
            return

        connection.sendPacket(CPacketPlayer.Rotation(rotation.x, rotation.y, player.onGround))

        PacketManager.lastReportedYaw = rotation.x
        PacketManager.lastReportedPitch = rotation.y
    }

    private fun SafeClientEvent.doPlace(placeInfo: PlaceInfo, block: Block, metaData: Int) {
        val sneak = !PacketManager.lastReportedSneaking && sendSneakPacket
        if (sneak) CPacketEntityAction(player, CPacketEntityAction.Action.START_SNEAKING).send()

        placeInfo.getPlacePacket(EnumHand.MAIN_HAND).send()
        player.swingArm(EnumHand.MAIN_HAND)

        if (sneak) CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING).send()
        val blockState = block.getStateForPlacement(world, placeInfo.pos, placeInfo.side, placeInfo.hitVecOffset.x.toFloat(), placeInfo.hitVecOffset.y.toFloat(), placeInfo.hitVecOffset.z.toFloat(), metaData, player, EnumHand.MAIN_HAND)
        val soundType = blockState.block.getSoundType(blockState, world, placeInfo.pos, player)

        if (updateState) world.setBlockState(placeInfo.placedPos, blockState)
        if (placeSound) world.playSound(player, placeInfo.pos, soundType.placeSound, SoundCategory.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f)
    }
}