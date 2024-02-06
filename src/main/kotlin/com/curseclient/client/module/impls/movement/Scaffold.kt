package com.curseclient.client.module.impls.movement

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.JumpMotionEvent
import com.curseclient.client.event.events.MoveEvent
import com.curseclient.client.event.events.PlayerPacketEvent
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.managers.HotbarManager
import com.curseclient.client.manager.managers.HotbarManager.updateSlot
import com.curseclient.client.manager.managers.PacketManager
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.settingName
import com.curseclient.client.utility.items.HotbarSlot
import com.curseclient.client.utility.items.firstItem
import com.curseclient.client.utility.items.hotbarSlots
import com.curseclient.client.utility.math.MSTimer
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.player.AngleUtils.yaw
import com.curseclient.client.utility.player.RotationUtils
import com.curseclient.client.utility.render.ColorUtils.setAlphaD
import com.curseclient.client.utility.render.esp.ESPRenderer
import com.curseclient.client.utility.world.RayCastUtils.getGroundPos
import com.curseclient.client.utility.world.placement.NeighborUtils.getNeighbor
import com.curseclient.client.utility.world.placement.PlaceInfo
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec2f
import java.awt.Color

object Scaffold : Module(
    "Scaffold",
    "Places blocks under you",
    Category.MOVEMENT
) {
    private val towerMode by setting("Tower Mode", TowerMode.None)
    private val matrixDelay by setting("Matrix Delay", 1.0, 0.1, 5.0, 0.1, visible = { towerMode == TowerMode.Matrix })
    private val asyncPlace by setting("Async Place", true)
    private val visibilityCheck by setting("Visibility Check", true)
    private val strictRotations by setting("Strict Rotations", false)
    private val placeDelay by setting("Place Delay", 1.0, 1.0, 20.0, 1.0)
    private val placeReach by setting("Place Reach", 4.25, 3.0, 6.0, 0.05)
    private val rotationTimeout by setting("Keep Rotation Ticks", 10.0, 1.0, 20.0, 1.0)
    private val sendSneakPacket by setting("Sneak Packet", false)
    val safeWalk by setting("Safe Walk", true)

    private val render by setting("ESP", false)
    private val color by setting("Color", Color.GREEN, { render })
    private val filledAlpha by setting("Filled Alpha", 0.25, 0.0, 1.0, 0.01, { render })
    private val outlineAlpha by setting("Outline Alpha", 0.5, 0.0, 1.0, 0.01, { render })
    private val duration by setting("Duration", 500.0, 100.0, 1000.0, 10.0, { render })

    private var placeInfo: PlaceInfo? = null

    private var rotations: Vec2f? = null
    private var reportedRotations: Vec2f? = null

    private var placeTicks = 0
    private var rotationTicks = 0

    private val msTimer = MSTimer()

    private val espMap = HashMap<BlockPos, Long>()
    private val renderer = ESPRenderer()

    private enum class TowerMode {
        None,
        Motion,
        Strict,
        Matrix
    }

    override fun getHudInfo() = towerMode.settingName

    override fun onDisable() {
        placeInfo = null

        rotations = null
        reportedRotations = null

        placeTicks = 0
        rotationTicks = 0
    }

    init {
        safeListener<PlayerPacketEvent.Data> {
            if (towerMode != TowerMode.Matrix) return@safeListener
            if (!shouldTower()) return@safeListener
            if (!msTimer.hasReached(100.0 * matrixDelay)) return@safeListener
            it.onGround = true
            player.jump()
            msTimer.reset()
        }

        safeListener<JumpMotionEvent> {
            if (towerMode != TowerMode.Strict || !shouldTower()) return@safeListener
            it.motion = 0.42f * 0.877f
        }

        safeListener<MoveEvent> {
            if (!shouldTower()) return@safeListener

            player.motionX = 0.0
            player.motionZ = 0.0

            if (towerMode == TowerMode.Motion) if (player.ticksExisted % 5 == 0) player.motionY = 0.42

            espMap.entries.removeIf {
                System.currentTimeMillis() - it.value > duration
            }
        }

        safeListener<PlayerPacketEvent.Data> { event ->
            placeTicks++

            val slot = player.hotbarSlots.firstItem<ItemBlock, HotbarSlot>()?.hotbarSlot
            placeInfo = getPlaceInfo()
            rotations = if (placeInfo != null) getRotations() else null

            if (rotations != null && slot != null) {
                reportedRotations = rotations
                rotationTicks = 0
                player.inventory.currentItem = slot
                updateSlot()
            } else {
                rotationTicks++
                if (rotationTicks > rotationTimeout.toInt()) reportedRotations = null
            }

            reportedRotations?.let { event.yaw = it.x; event.pitch = it.y }
        }

        safeListener<PlayerPacketEvent.Post> {
            val s = player.hotbarSlots.firstItem<ItemBlock, HotbarSlot>() ?: return@safeListener
            if (HotbarManager.lastReportedSlot != s.hotbarSlot) return@safeListener
            if (rotations == null) return@safeListener
            if (placeTicks < placeDelay && !shouldTower()) return@safeListener
            placeInfo?.let { doPlace(it, s) }
        }

        safeListener<Render3DEvent> {
            if (!render) return@safeListener
            espMap.entries.forEach {
                val alpha = 1.0 - MathUtils.clamp((System.currentTimeMillis() - it.value).toDouble() / duration, 0.0, 1.0)
                renderer.put(it.key, color.setAlphaD(filledAlpha * alpha), color.setAlphaD(outlineAlpha * alpha))
            }
            renderer.render()
        }
    }

    private fun getRotations(): Vec2f {
        val info = placeInfo!!
        if (!strictRotations) return RotationUtils.rotationsToVec(placeInfo!!.hitVec)

        val yaw = if (info.side != EnumFacing.UP) info.side.opposite.yaw else PacketManager.lastReportedYaw
        val pitch = 82.5f
        return Vec2f(yaw, pitch)
    }

    private fun SafeClientEvent.shouldTower(): Boolean =
        player.movementInput.jump &&
            player.hotbarSlots.firstItem<ItemBlock, HotbarSlot>() != null &&
            player.posY - getGroundPos(player).y < 1.5


    override fun onEnable() {
        espMap.clear()
    }

    private fun SafeClientEvent.getPlaceInfo(): PlaceInfo? {
        val posDown = BlockPos(player.posX, player.posY - 1.0, player.posZ)
        return if (world.getBlockState(posDown).material.isReplaceable) {
            getNeighbor(posDown, 1)
                ?: getNeighbor(posDown, 3, sides = EnumFacing.HORIZONTALS, range = placeReach.toFloat(), visibleSideCheck = visibilityCheck)
        } else {
            null
        }

    }

    private fun SafeClientEvent.doPlace(placeInfo: PlaceInfo, slot: HotbarSlot) {
        if (!world.getBlockState(placeInfo.placedPos).material.isReplaceable) return

        val itemStack = slot.stack
        val block = (itemStack.item as? ItemBlock?)?.block ?: return
        val metaData = itemStack.metadata

        val sneak = !PacketManager.lastReportedSneaking && sendSneakPacket
        if (sneak) connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_SNEAKING))

        connection.sendPacket(placeInfo.getPlacePacket(EnumHand.MAIN_HAND))
        player.swingArm(EnumHand.MAIN_HAND)

        if (sneak) connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))

        if (asyncPlace) {
            val blockState = block.getStateForPlacement(world, placeInfo.pos, placeInfo.side, placeInfo.hitVecOffset.x.toFloat(), placeInfo.hitVecOffset.y.toFloat(), placeInfo.hitVecOffset.z.toFloat(), metaData, player, EnumHand.MAIN_HAND)
            val soundType = blockState.block.getSoundType(blockState, world, placeInfo.pos, player)
            world.playSound(player, placeInfo.pos, soundType.placeSound, SoundCategory.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f)
            world.setBlockState(placeInfo.placedPos, blockState)
        }

        espMap[placeInfo.placedPos] = System.currentTimeMillis()

        placeTicks = 0
    }
}