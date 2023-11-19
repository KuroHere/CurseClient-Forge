package com.curseclient.client.module.modules.combat

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.*
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.listener
import com.curseclient.client.event.listener.runSafe
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.event.listener.withSync
import com.curseclient.client.manager.managers.HotbarManager
import com.curseclient.client.manager.managers.HotbarManager.updateSlot
import com.curseclient.client.manager.managers.PacketManager
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.mixins.useEntityAction
import com.curseclient.client.utility.extension.mixins.useEntityId
import com.curseclient.client.utility.extension.transformIf
import com.curseclient.client.utility.items.firstItem
import com.curseclient.client.utility.items.hotbarSlots
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.render.vector.Conversion.toVec3d
import com.curseclient.client.utility.render.vector.Conversion.toVec3dCenter
import com.curseclient.client.utility.player.PacketUtils.send
import com.curseclient.client.utility.player.RotationUtils.calcAngleDiff
import com.curseclient.client.utility.player.RotationUtils.normalizeAngle
import com.curseclient.client.utility.player.RotationUtils.rotationsToVec
import com.curseclient.client.utility.player.TargetingUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.esp.AnimatedESPRenderer
import com.curseclient.client.utility.threads.loop.DelayedLoopThread
import com.curseclient.client.utility.world.CrystalUtils.calcCrystalDamage
import com.curseclient.client.utility.world.CrystalUtils.getBlockPosInSphere
import com.curseclient.client.utility.world.CrystalUtils.getCrystalList
import com.curseclient.client.utility.world.RayCastUtils.checkSideVisibility
import com.curseclient.client.utility.world.WorldUtils.getHitVec
import com.curseclient.client.utility.world.WorldUtils.getHitVecOffset
import com.curseclient.client.utility.world.WorldUtils.getVisibleSidesSmart
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemPickaxe
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.network.play.server.SPacketSpawnObject
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object CrystalAura : Module(
    "CrystalAura",
    "Automatically places and breaks ender crystals",
    Category.COMBAT
) {
    // region Settings
    private val page by setting("Page", Page.General)

    // General
    private val targetReach by setting("Target Reach", 12.0, 3.0, 30.0, 0.5, { page == Page.General })
    private val targetPriority by setting("Target Priority", TargetPriority.Health, { page == Page.General })
    private val players by setting("Players", true, { page == Page.General })
    private val friends by setting("Friends", false, { players && page == Page.General })
    private val mobs by setting("Mobs", false, { page == Page.General })
    private val placeRotate by setting("Place Rotate", true, { page == Page.General })
    private val explodeRotate by setting("Explode Rotate", true, { page == Page.General })
    private val yawSpeed by setting("Yaw Speed", 45.0, 1.0, 180.0, 1.0, { page == Page.General && (placeRotate || explodeRotate) })
    private val pitchSpeed by setting("Pitch Speed", 25.0, 1.0, 90.0, 1.0, { page == Page.General && (placeRotate || explodeRotate) })
    private val maxYawDiff by setting("Max Yaw Diff", 3.0, 1.0, 180.0, 1.0, { page == Page.General && (placeRotate || explodeRotate) })
    private val maxPitchDiff by setting("Max Pitch Diff", 5.0, 1.0, 90.0, 1.0, { page == Page.General && (placeRotate || explodeRotate) })
    private val keepRotationTicks by setting("Keep Rotation Ticks", 5.0, 2.0, 20.0, 1.0, { page == Page.General && (placeRotate || explodeRotate) })
    private val hotbarMode by setting("Hotbar Mode", HotbarMode.Swap, { page == Page.General })
    private val swapBack by setting("Swap Back", true, { page == Page.General && hotbarMode == HotbarMode.Swap })
    private val keepHotbarTicks by setting("Keep Hotbar Ticks", 10.0, 2.0, 40.0, 1.0, { page == Page.General && hotbarMode != HotbarMode.None })
    private val swapPause by setting("Swap Pause", 0.0, 0.0, 1000.0, 5.0, { page == Page.General })
    private val pauseWhileDigging by setting("Pause While Digging", true, { page == Page.General })
    private val pauseWhileEating by setting("Pause While Eating", true, { page == Page.General })
    private val ignoreOffhand by setting("Ignore Offhand", true, { page == Page.General && pauseWhileEating })

    // Place
    private val place by setting("Place", true, { page == Page.Place })
    private val placeReach by setting("Reach (Place)", 3.25, 2.0, 8.0, 0.05, { page == Page.Place && place })
    private val placeWallReach by setting("Wall Reach (Place)", 3.0, 2.0, 8.0, 0.05, { page == Page.Place && place })
    private val minDamagePlace by setting("Min Damage (Place)", 4.0, 1.0, 20.0, 0.25, { page == Page.Place && place })
    private val maxSelfDamagePlace by setting("Max Self Damage (Place)", 5.0, 2.0, 36.0, 0.5, { page == Page.Place && place })
    private val antiSuicidePlace by setting("Anti Suicide (Place)", 0.0, 0.0, 10.0, 0.25, { page == Page.Place && place })
    private val placeDelay by setting("Delay (Place)", 10.0, 0.0, 1000.0, 5.0, { page == Page.Place && place })
    private val maxPlaceAttempts by setting("Max Attempts (Place)", 50.0, 1.0, 50.0, 1.0, { page == Page.Place && place })
    private val placeRetryDelay by setting("Retry Delay (Place)", 1000.0, 200.0, 5000.0, 10.0, { page == Page.Place && place })
    private val placeSwing by setting("Swing Mode (Place)", SwingMode.Client, { page == Page.Place && place })
    private val multiPlacing by setting("Multi-Placing", false, { page == Page.Place && place })
    private val maxCrystals by setting("Max Crystals", 2.0, 2.0, 10.0, 1.0, { page == Page.Place && place && multiPlacing })
    private val packetVec by setting("Packet Offset Mode", HitVecMode.BySide, { page == Page.Place && place })
    private val packetOffset by setting("Packet Offset", 0.5, 0.0, 1.0, 0.05, { page == Page.Place && place && packetVec == HitVecMode.Custom })
    private val newPlace by setting("1.13 Crystal", false, { page == Page.Place && place })
    private val placeSync by setting("Place Sync", false, { page == Page.Place && place })
    private val extraPlace by setting("Extra Place", false, { page == Page.Place && place })

    // Explode
    private val explode by setting("Explode", true, { page == Page.Explode })
    private val explodeReach by setting("Reach (Explode)", 3.25, 2.0, 8.0, 0.05, { page == Page.Explode && explode })
    private val explodeWallReach by setting("Wall Reach (Explode)", 3.0, 2.0, 8.0, 0.05, { page == Page.Explode && explode })
    private val minDamageExplode by setting("Min Damage (Explode)", 4.0, 1.0, 20.0, 0.25, { page == Page.Explode && explode })
    private val maxSelfDamageExplode by setting("Max Self Damage (Explode)", 5.0, 2.0, 36.0, 0.5, { page == Page.Explode && explode })
    private val antiSuicideExplode by setting("Anti Suicide (Explode)", 0.0, 0.0, 10.0, 0.25, { page == Page.Explode && explode })
    private val explodeDelay by setting("Delay (Explode)", 10.0, 0.0, 1000.0, 5.0, { page == Page.Explode && explode })
    private val maxExplodeAttempts by setting("Max Attempts (Explode)", 50.0, 1.0, 50.0, 1.0, { page == Page.Explode && explode })
    private val explodeRetryDelay by setting("Retry Delay (Explode)", 1000.0, 200.0, 5000.0, 10.0, { page == Page.Explode && explode })
    private val explodeSwing by setting("Swing Mode (Explode)", SwingMode.Client, { page == Page.Explode && explode })
    private val crystalHeight by setting("Crystal Height", 0.00, 0.0, 1.0, 0.02, { page == Page.Explode && explode && explodeRotate })
    private val spawnExplode by setting("Spawn Explode", false, { page == Page.Explode && explode })

    // Extras
    private val forcePlace by setting("Force Place", false, { page == Page.Extras })
    private val forcePlaceMinDamage by setting("FP Min Damage", 1.0, 0.1, 5.0, 0.05, { page == Page.Extras && forcePlace })
    private val forcePlaceMaxTargetHealth by setting("FP Health Threshold", 4.0, 2.0, 20.0, 0.1, { page == Page.Extras && forcePlace })
    private val forcePlaceMaxTargetDura by setting("FP Armor Dura Threshold", 5.0, 2.0, 100.0, 1.0, { page == Page.Extras && forcePlace })
    private val antiSurround by setting("Anti Surround", false, { page == Page.Extras })
    private val antiSurroundPickaxeOnly by setting("AS Pickaxe Only", true, { page == Page.Extras && antiSurround })

    // Multithreading
    private val damageCalculationDelay by setting("Calculation Thread Delay", 50.0, 25.0, 100.0, 1.0, { page == Page.Multithreading })
    private val interactDelay by setting("Interact Thread Delay", 15.0, 1.0, 50.0, 1.0, { page == Page.Multithreading })
    @Suppress("UNUSED") private val reloadSetting by setting("Reload Threads", { calculationThread.reload(); interactThread.reload() }, { page == Page.Multithreading })

    // Render
    private val render by setting("Render", true, { page == Page.Render })
    private val boxSize by setting("Box Size", 1.0, 0.2, 1.0, 0.05, { page == Page.Render && render })
    private val moveSpeed by setting("Move Speed", 1.0, 0.5, 3.0, 0.1, { page == Page.Render && render })
    private val lineWidth by setting("Line Width", 1.0, 1.0, 5.0, 0.1, { page == Page.Render && render })
    private val fadeDelay by setting("Fade Delay", 500.0, 0.0, 2000.0, 50.0, { page == Page.Render && render })

    // endregion

    // region Enum's
    private enum class Page {
        General,
        Place,
        Explode,
        Extras,
        Multithreading,
        Render
    }

    private enum class HotbarMode {
        None,
        Swap,
        Spoof
    }

    @Suppress("UNUSED")
    private enum class TargetPriority(val sortingFactor: SafeClientEvent.(entity: EntityLivingBase) -> Double) {
        Health({ player.getPositionEyes(1.0f).distanceTo(it.positionVector) }),
        Distance({ it.health.toDouble() })
    }

    @Suppress("UNUSED")
    private enum class SwingMode(val swing: SafeClientEvent.(hand: EnumHand) -> Unit) {
        None({ }),
        Client({ player.swingArm(it) }),
        Server({ CPacketAnimation(it).send() })
    }

    @Suppress("UNUSED")
    private enum class HitVecMode(val getHitVec: (side: EnumFacing) -> Vec3d) {
        BySide({ getHitVecOffset(it) }),
        Up({ Vec3d(0.5, 1.0, 0.5) }),
        Down({ Vec3d(0.5, 0.0, 0.5) }),
        Custom({ Vec3d(0.5, packetOffset, 0.5) })
    }

    // endregion

    // region Variables
    var target: EntityLivingBase? = null

    private var prevSlot: Int? = null
    private var currentSlot: Int? = null

    private var currentLookVec: Vec3d? = null // junky way to sync rotations with multithreading
    private var currentRotation: Vec2f? = null

    private var lastHotbarActivity = 0L
    private var lastRotationActivity = 0L

    private var lastPlaceTime = 0L
    private var lastExplodeTime = 0L

    private val damageMap = HashMap<BlockPos, DamageInfo>()
    private val placeMap = ArrayList<PlaceSyncInfo>()
    private val explodeMap = ArrayList<ExplodeSyncInfo>()

    private var placeAttempts = 0
    private var explodeAttempts = 0

    private val renderer = AnimatedESPRenderer { Triple(HUD.getColor().setAlpha(60), HUD.getColor().setAlpha(120), lineWidth.toFloat()) }
    private var renderPos: BlockPos? = null
    private var lastRenderActivity = 0L
    // endregion

    // region Multithreading
    private val calculationThread = DelayedLoopThread("Crystal Aura Calculation Thread", { isEnabled() && mc.world != null }, { damageCalculationDelay.toLong() }) {
        runSafe { update() }
    }

    private val interactThread = DelayedLoopThread("Crystal Aura Interact Thread", { isEnabled() && mc.world != null }, { interactDelay.toLong() }) {
        runSafe { interact() }
    }

    init {
        calculationThread.reload()
        interactThread.reload()
    }

    // endregion

    // region Event Listeners
    init {
        safeListener<TickEvent.ClientTickEvent> { event ->
            if (event.phase != TickEvent.Phase.START) return@safeListener

            updateSlot()
        }

        safeListener<PlayerPacketEvent.Data>(-2) { event ->
            currentLookVec?.let { rotate(it) } ?: run { currentRotation = null }
            spoofRotation(event)
        }

        safeListener<PacketEvent.Receive> { event ->
            when (val packet = event.packet) {
                is SPacketSpawnObject -> {
                    if (packet.type != 51) return@safeListener

                    val vec = Vec3d(packet.x, packet.y, packet.z)
                    handleSpawnPacket(packet.entityID, vec)
                }

                is SPacketSoundEffect -> {
                    if (packet.category != SoundCategory.BLOCKS) return@safeListener
                    if (packet.sound != SoundEvents.ENTITY_GENERIC_EXPLODE) return@safeListener

                    val vec = Vec3d(packet.x, packet.y, packet.z)
                    handleSoundPacket(vec)
                }
            }
        }

        safeListener<PlayerHotbarSlotEvent>(-2) { event ->
            spoofSlot(event)
        }

        safeListener<Render3DEvent> {
            if (!render) {
                renderer.reset()
                return@safeListener
            }

            renderer.setPosition(renderPos)

            renderer.animationSpeed = moveSpeed
            renderer.maxSize = boxSize

            renderer.draw()
        }

        listener<ConnectionEvent.Connect> {
            reset()
        }

        listener<ConnectionEvent.Disconnect> {
            reset()
        }
    }

    override fun onEnable() {
        calculationThread.interrupt()
        interactThread.interrupt()

        reset()
    }

    override fun onDisable() {
        reset()
    }

    private fun reset() {
        target = null
        prevSlot = null
        currentSlot = null

        lastHotbarActivity = 0L
        lastRotationActivity = 0L
        lastRenderActivity = 0L

        currentLookVec = null
        currentRotation = null

        lastPlaceTime = 0L
        lastExplodeTime = 0L

        placeAttempts = 0
        explodeAttempts = 0

        damageMap.withSync {
            damageMap.clear()
        }

        placeMap.withSync {
            placeMap.clear()
        }

        explodeMap.withSync {
            explodeMap.clear()
        }

        renderer.reset()
    }
    // endregion

    // region Hotbar
    private fun SafeClientEvent.updateCurrentSlot(isPlacing: Boolean) {
        if (hotbarMode == HotbarMode.None) return
        val offhand = player.heldItemOffhand.item == Items.END_CRYSTAL
        val slot = player.hotbarSlots.firstItem(Items.END_CRYSTAL)?.hotbarSlot

        slot?.let {
            if (offhand || !isPlacing) return@let

            lastHotbarActivity = System.currentTimeMillis()
            currentSlot = it
        }

        val maxTime = if (hotbarMode == HotbarMode.Swap && !swapBack && player.inventory.currentItem == slot) 100.0
        else keepHotbarTicks * 50.0

        val timeExisted = System.currentTimeMillis() - lastHotbarActivity
        if (timeExisted > maxTime)
            currentSlot = null

        currentSlot?.let {
            if (hotbarMode == HotbarMode.Swap) {
                if (player.inventory.currentItem != it) {
                    if (prevSlot == null) {
                        prevSlot = player.inventory.currentItem
                    }

                    player.inventory.currentItem = it
                }
            } else {
                prevSlot = null
            }
        } ?: run {
            prevSlot?.let { // swapBack
                if (swapBack && hotbarMode == HotbarMode.Swap) player.inventory.currentItem = it
                prevSlot = null
            }
        }
    }

    private fun spoofSlot(event: PlayerHotbarSlotEvent) {
        if (hotbarMode != HotbarMode.Spoof) return
        currentSlot?.let {
            event.slot = it
        }
    }

    private fun SafeClientEvent.hasCrystal(): Boolean {
        val offhandItem = player.heldItemOffhand.item
        val offhandCheck = offhandItem == Items.END_CRYSTAL

        val hotbarCrystal = player.hotbarSlots.firstItem(Items.END_CRYSTAL)
        val hotbarCheck = hotbarCrystal != null

        return offhandCheck || hotbarCheck
    }

    private fun SafeClientEvent.getHand(): EnumHand? {
        return when(Items.END_CRYSTAL) {
            player.heldItemOffhand.item -> EnumHand.OFF_HAND
            player.hotbarSlots[HotbarManager.lastReportedSlot].stack.item -> EnumHand.MAIN_HAND
            else -> null
        }
    }
    // endregion

    // region Rotation
    private fun checkRotation(lookVec: Vec3d): Boolean {
        val rotation = rotationsToVec(lookVec)
        val yaw = PacketManager.lastReportedYaw
        val pitch = PacketManager.lastReportedPitch

        val yawDiff = abs(rotation.x - yaw) % 180.0f
        val pitchDiff = abs(rotation.y - pitch)

        return (yawDiff < maxYawDiff) && (pitchDiff < maxPitchDiff)
    }

    private fun updateRotation(info: InteractInfo?) {
        (info as? ExplodeInfo)?.let { explodeInfo ->
            if (explodeRotate) {
                updateLookVec(explodeInfo.lookVec)
            }
        }

        (info as? PlaceInfo)?.let { placeInfo ->
            if (placeRotate) {
                updateLookVec(placeInfo.lookVec)
            }
        }

        val timeElapsed = System.currentTimeMillis() - lastRotationActivity
        if (timeElapsed > keepRotationTicks * 50.0) currentLookVec = null
    }

    private fun updateLookVec(lookVec: Vec3d) {
        currentLookVec = lookVec
        lastRotationActivity = System.currentTimeMillis()
    }

    private fun rotate(lookVec: Vec3d) {
        currentRotation = updateAngles(currentRotation, rotationsToVec(lookVec))
    }

    private fun updateAngles(from: Vec2f?, to: Vec2f): Vec2f {
        val yaw = from?.x ?: PacketManager.lastReportedYaw
        val pitch = from?.y ?: PacketManager.lastReportedPitch

        val yawDiff = calcAngleDiff(to.x, yaw)
        val pitchDiff = calcAngleDiff(to.y, pitch)

        val yawTo = if (abs(yawDiff) <= yawSpeed) to.x
        else normalizeAngle(yaw + yawDiff.coerceIn((-yawSpeed).toFloat(), yawSpeed.toFloat()))

        val pitchTo = if (abs(pitchDiff) <= pitchSpeed) to.y
        else normalizeAngle(pitch + pitchDiff.coerceIn((-pitchSpeed).toFloat(), pitchSpeed.toFloat()))

        return Vec2f(yawTo, pitchTo)
    }

    private fun spoofRotation(event: PlayerPacketEvent.Data) {
        currentRotation?.let { r ->
            event.yaw = r.x
            event.pitch = r.y
        }
    }
    // endregion

    // region Features
    private fun SafeClientEvent.doPlace(info: PlaceInfo, handIn: EnumHand) {
        placeAttempts++

        val offset = packetVec.getHitVec(info.side)

        CPacketPlayerTryUseItemOnBlock(
            info.pos,
            info.side,
            handIn,
            offset.x.toFloat(),
            offset.y.toFloat(),
            offset.z.toFloat()
        ).send(this)

        placeSwing.swing(this, handIn)

        renderPos = info.pos
        lastRenderActivity = System.currentTimeMillis()
        lastPlaceTime = System.currentTimeMillis()

        placeMap.withSync {
            placeMap.add(PlaceSyncInfo(info.pos))
        }
    }

    private fun SafeClientEvent.doExplode(info: ExplodeInfo) {
        explodeAttempts++

        val crystal = info.crystal
        val id = crystal.entityId
        explodeDirect(id)

        renderPos = BlockPos(info.lookVec.subtract(0.0, crystalHeight + 0.5, 0.0))
        lastRenderActivity = System.currentTimeMillis()
        lastExplodeTime = System.currentTimeMillis()

        explodeMap.withSync {
            explodeMap.add(ExplodeSyncInfo(id, crystal.positionVector))
        }

        extraPlace(info)
    }

    private fun SafeClientEvent.explodeDirect(id: Int) {
        CPacketUseEntity().apply {
            this.useEntityAction = CPacketUseEntity.Action.ATTACK
            this.useEntityId = id
        }.send()

        explodeSwing.swing(this, EnumHand.MAIN_HAND)
    }

    private fun SafeClientEvent.extraPlace(eInfo: ExplodeInfo) {
        if (!extraPlace) return
        val hand = getHand() ?: return

        val pos = BlockPos(eInfo.lookVec.subtract(0.0, crystalHeight + 0.5, 0.0))

        val damage = damageMap[pos] ?: return
        if (!checkPlaceDamage(damage)) return

        val side = getSide(pos) ?: return
        val offset = side.second

        CPacketPlayerTryUseItemOnBlock(
            pos,
            side.first,
            hand,
            offset.x.toFloat(),
            offset.y.toFloat(),
            offset.z.toFloat()
        ).send(this)
    }

    private fun SafeClientEvent.handleSpawnPacket(id: Int, pos: Vec3d) {
        val eyeDist = pos.distanceTo(player.getPositionEyes(1.0f))

        if (eyeDist <= placeReach + 1.0)
            placeAttempts = 0

        placeMap.withSync {
            placeMap.removeIf { syncInfo ->
                val vec = syncInfo.pos.toVec3dCenter()
                val dist = vec.distanceTo(pos)
                dist <= 1.0
            }
        }

        if (spawnExplode && explode && eyeDist <= explodeReach) {
            val blockPos = BlockPos(pos.subtract(0.0, 0.5, 0.0))
            val damage = damageMap[blockPos]
            val check = damage?.let { checkExplodeDamage(it) } ?: false

            if (check)
                explodeDirect(id)
        }
    }

    private fun SafeClientEvent.handleSoundPacket(pos: Vec3d) {
        val eyeDist = pos.distanceTo(player.getPositionEyes(1.0f))

        if (eyeDist <= explodeReach + 1.0)
            explodeAttempts = 0

        explodeMap.withSync {
            explodeMap.removeIf { syncInfo ->
                val vec = syncInfo.position
                val dist = vec.distanceTo(pos)
                dist <= 6.0
            }
        }

        placeMap.withSync {
            placeMap.removeIf { syncInfo ->
                val vec = syncInfo.pos.toVec3dCenter()
                val dist = vec.distanceTo(pos)
                dist <= 6.0
            }
        }
    }

    private fun Entity.isIgnored() =
        explodeMap.any { it.entityId == this.entityId }

    private fun SafeClientEvent.checkDigging() =
        !pauseWhileDigging || player.heldItemMainhand.item !is ItemPickaxe || !playerController.isHittingBlock

    private fun SafeClientEvent.checkEating() =
        !pauseWhileEating || !player.isHandActive || player.activeItemStack.item !is ItemFood || (ignoreOffhand && player.activeHand == EnumHand.OFF_HAND)

    private fun checkSwapPause() =
        (System.currentTimeMillis() - HotbarManager.lastSwapTime) >= swapPause

    // endregion

    // region Damage Calculating & Update
    private fun SafeClientEvent.update() {
        updateTarget()
        updateDamage()
    }

    private fun SafeClientEvent.updateTarget() {
        target = TargetingUtils.getTargetList(players, friends, mobs, mobs, true)
            .filter { player.getDistance(it) <= targetReach }
            .minByOrNull { targetPriority.sortingFactor(this, it) }
    }

    private fun SafeClientEvent.updateDamage() {
        damageMap.withSync { map ->
            damageMap.clear()
            target?.let { updateDamageMap(it, map) }
        }
    }

    private fun SafeClientEvent.interact() {
        val time = System.currentTimeMillis()

        val placeTimeExisted = time - lastPlaceTime
        val explodeTimeExisted = time - lastExplodeTime

        if (placeTimeExisted > placeRetryDelay) {
            placeAttempts = 0
        }

        if (explodeTimeExisted > explodeRetryDelay) {
            explodeAttempts = 0
        }

        placeMap.withSync {
            placeMap.removeIf {
                val timeExisted = System.currentTimeMillis() - it.syncTime
                timeExisted > placeRetryDelay
            }
        }

        explodeMap.withSync {
            explodeMap.removeIf {
                val timeExisted = System.currentTimeMillis() - it.syncTime
                timeExisted > explodeRetryDelay
            }
        }

        val isPaused = !checkEating() || !checkDigging() || HoleMiner.isEnabled()
        var info: InteractInfo? = null

        val explodeCheck = explode &&
            checkExplodeDelay() &&
            checkExplodeAttempts() &&
            !isPaused

        val placeCheck = place &&
            checkPlaceDelay() &&
            checkPlaceAttempts() &&
            checkCrystalCount() &&
            hasCrystal() &&
            !isPaused

        if (explodeCheck) info = getExplodeInfo()
        if (placeCheck && info == null) info = getPlaceInfo()

        updateRenderer()
        updateRotation(info)
        updateCurrentSlot(info is PlaceInfo)

        (info as? ExplodeInfo)?.let {
            if (!checkRotation(it.lookVec) && explodeRotate) return@let
            doExplode(it)
        }

        (info as? PlaceInfo)?.let {
            val hand = getHand() ?: return@let
            if ((!checkRotation(it.lookVec) && placeRotate)) return@let
            if (!checkSwapPause()) return@let

            doPlace(it, hand)
        }
    }

    private fun SafeClientEvent.updateDamageMap(target: EntityLivingBase, map: HashMap<BlockPos, DamageInfo>) {
        map.apply {
            val range = max(placeReach, explodeReach).toFloat() + 1f
            val blocks = getBlockPosInSphere(player.getPositionEyes(1f), range)

            for (pos in blocks) {
                // material check
                val state1 = world.getBlockState(pos)
                val block = state1.block
                if (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK) continue

                // check for upper blocks
                val state2 = world.getBlockState(pos.up())
                val material2 = state2.material
                if (!material2.isReplaceable || material2.isLiquid) continue

                if (getCrystalPlaceHeight() > 1.0) {
                    val state3 = world.getBlockState(pos.up().up())
                    val material3 = state3.material
                    if (!material3.isReplaceable || material3.isLiquid) continue
                }

                // damage
                val targetDamage = calcCrystalDamage(pos, target)
                val selfDamage = calcCrystalDamage(pos, player)

                this[pos] = DamageInfo(target, targetDamage, selfDamage)
            }
        }
    }

    private fun updateRenderer() {
        val timeElapsed = System.currentTimeMillis() - lastRenderActivity
        if (timeElapsed > fadeDelay) renderPos = null
    }
    // endregion

    // region Place
    private fun SafeClientEvent.getPlaceInfo(): PlaceInfo? {
        return damageMap.withSync { map ->
            return@withSync map
                .mapNotNull { checkPlace(it.key, it.value) }
                .maxByOrNull { it.third }?.let { PlaceInfo(it.first, it.second.first, it.second.second) }
        }
    }

    private fun SafeClientEvent.checkPlace(pos: BlockPos, damageInfo: DamageInfo): Triple<BlockPos, Pair<EnumFacing, Vec3d>, Float>? {
        val up = pos.up()

        // damage checks
        if (!checkPlaceDamage(damageInfo)) return null

        // side & reach check
        val side = getSide(pos) ?: return null

        // hitbox check for colliding with other crystals
        val crystalHitboxCheck = world.getEntitiesWithinAABBExcludingEntity(null, getCrystalCollidingBox(up)).all { entity: Entity? ->
            if (entity !is EntityEnderCrystal) return@all true

            entity.isDead || (entity.isIgnored() && BlockPos(entity.positionVector.add(0.0, 0.5, 0.0)) == up)
        }

        if (!crystalHitboxCheck) return null

        // hitbox check for colliding with other entities
        val entityHitboxCheck = world.getEntitiesWithinAABBExcludingEntity(null, getEntityCollidingBox(up)).all { entity: Entity? ->
            if (entity is EntityEnderCrystal) return@all true
            if (entity == null) return@all true

            val healthCheck = entity is EntityLivingBase && entity.health <= 0.0f
            entity.isDead || healthCheck
        }

        if (!entityHitboxCheck) return null

        return Triple(pos, side, damageInfo.targetDamage)
    }

    private fun getCrystalCollidingBox(pos: BlockPos): AxisAlignedBB {
        val vec1 = pos.toVec3d().subtract(0.5, 0.0, 0.5)
        val vec2 = pos.toVec3d().add(1.5, getCrystalPlaceHeight(), 1.5)
        return AxisAlignedBB(vec1, vec2)
    }

    private fun getEntityCollidingBox(pos: BlockPos): AxisAlignedBB {
        val vec1 = pos.toVec3d()
        val vec2 = pos.toVec3d().add(1.0, getCrystalPlaceHeight(), 1.0)
        return AxisAlignedBB(vec1, vec2)
    }

    private fun getCrystalPlaceHeight(): Double {
        return if (newPlace) 1.0 else 2.0
    }

    private fun SafeClientEvent.checkPlaceDamage(damageInfo: DamageInfo): Boolean {
        val targetDamage = damageInfo.targetDamage
        val selfDamage = damageInfo.selfDamage

        val forcePlaceCheck = checkForcePlace(damageInfo)
        val antiSurroundCheck = checkAntiSurround()

        var minDmg = minDamagePlace
        if (forcePlaceCheck) minDmg = min(minDmg, forcePlaceMinDamage)
        if (antiSurroundCheck) minDmg = min(minDmg, 0.1)

        // damage checks
        if (targetDamage < minDmg) return false
        if (selfDamage > maxSelfDamagePlace) return false

        //anti suicide
        if (antiSuicidePlace != 0.0 && (player.health - selfDamage < antiSuicidePlace)) return false

        return true
    }

    private fun checkForcePlace(damageInfo: DamageInfo): Boolean {
        if (!forcePlace) return false
        val t = (damageInfo.target as? EntityPlayer) ?: return false

        val durability = t.armorInventoryList
            .filter { !it.isEmpty && it.isItemStackDamageable }
            .maxByOrNull { it.itemDamage }
            ?.let {
                (it.maxDamage - it.itemDamage) * 100 / it.maxDamage
            } ?: 100

        val healthCheck = t.health <= forcePlaceMaxTargetHealth
        val duraCheck = durability < forcePlaceMaxTargetDura

        return healthCheck || duraCheck
    }

    private fun SafeClientEvent.checkAntiSurround(): Boolean {
        if (!antiSurround) return false

        if (getHand() != EnumHand.OFF_HAND) return false
        if (!playerController.isHittingBlock) return false
        val currentTarget = target as? EntityPlayer ?: return false
        if (player.heldItemMainhand.item !is ItemPickaxe && antiSurroundPickaxeOnly) return false

        if (mc.objectMouseOver.typeOfHit != RayTraceResult.Type.BLOCK) return false
        val breakingBlockPos = mc.objectMouseOver.blockPos
        val targetBlockPos = BlockPos(currentTarget.positionVector)

        if (world.getBlockState(breakingBlockPos).block != Blocks.OBSIDIAN) return false
        if (breakingBlockPos.toVec3dCenter().distanceTo(targetBlockPos.toVec3dCenter()) > 2.0) return false
        if (breakingBlockPos.y != targetBlockPos.y) return false

        // finnaly!
        return true
    }

    private fun SafeClientEvent.getSide(pos: BlockPos): Pair<EnumFacing, Vec3d>? {
        val sides = getVisibleSidesSmart(pos).mapNotNull { facing ->
            val rayCastCheck = checkSideVisibility(pos, facing)
            val hitVec = getHitVec(pos, facing)
            val dist = hitVec.distanceTo(player.getPositionEyes(1.0f))

            val reach = if (rayCastCheck) placeReach else placeWallReach
            if (dist > reach) return@mapNotNull null

            Triple(facing, hitVec, dist)
        }

        return if (sides.any { it.first == EnumFacing.UP }) (EnumFacing.UP to getHitVec(pos, EnumFacing.UP)) // based side
        else sides.minByOrNull { it.third }?.let { it.first to it.second }
    }

    private fun SafeClientEvent.checkCrystalCount() =
        getCrystalCount() < getMaxCrystalCount()

    private fun getMaxCrystalCount() =
        if (!multiPlacing) 1 else maxCrystals.toInt()

    private fun SafeClientEvent.getCrystalCount(): Int {
        return damageMap.withSync { map ->

            val placeSyncCount = placeMap.count { syncInfo ->
                map.contains(syncInfo.pos)
            }.transformIf(!placeSync) { 0 }

            return@withSync placeSyncCount + getCrystalList(player.getPositionEyes(1.0f), placeReach.toFloat() + 1.0f).count { crystal ->
                if (crystal.isIgnored()) return@count false

                val box = crystal.entityBoundingBox
                val pos = Vec3d(lerp(box.minX, box.maxX, 0.5), box.minY - 0.5, lerp(box.minZ, box.maxZ, 0.5))
                val block = BlockPos(pos)
                map.contains(block)
            }
        }
    }

    private fun checkPlaceDelay(): Boolean {
        return System.currentTimeMillis() - lastPlaceTime >= placeDelay
    }

    private fun checkPlaceAttempts(): Boolean {
        return placeAttempts < maxPlaceAttempts - 1 + getMaxCrystalCount()
    }

    // endregion

    // region Explode
    private fun SafeClientEvent.getExplodeInfo(): ExplodeInfo? {
        val crystals = getCrystalList(player.getPositionEyes(1.0f), max(explodeReach, explodeWallReach).toFloat() + 2.0f)

        return damageMap.withSync { map ->
            map.mapNotNull { checkExplode(it, crystals) }
                .maxByOrNull { it.second }
                ?.let { c ->
                    val crystal = c.first
                    ExplodeInfo(crystal, crystal.getCrystalRotation())
                }
        }
    }

    private fun SafeClientEvent.checkExplode(cache: Map.Entry<BlockPos, DamageInfo>, crystals: List<EntityEnderCrystal>): Pair<EntityEnderCrystal, Float>? {
        val pos = cache.key
        val damageInfo = cache.value

        // damage checks
        if (!checkExplodeDamage(damageInfo)) return null

        val crystal = crystals.firstOrNull { crystal ->
            if (crystal.isIgnored()) return@firstOrNull false

            val box = crystal.entityBoundingBox
            val p = Vec3d(lerp(box.minX, box.maxX, 0.5), box.minY, lerp(box.minZ, box.maxZ, 0.5))
            if (pos.toVec3d().add(0.5, 1.0, 0.5).distanceTo(p) > 0.5) return@firstOrNull false

            val visible = player.canEntityBeSeen(crystal)
            val reach = if (visible) explodeReach else explodeWallReach
            val dist = crystal.getCrystalRotation().distanceTo(player.getPositionEyes(1.0f))

            dist <= reach
        } ?: return null

        return crystal to damageInfo.targetDamage
    }

    private fun SafeClientEvent.checkExplodeDamage(damageInfo: DamageInfo): Boolean {
        val targetDamage = damageInfo.targetDamage
        val selfDamage = damageInfo.selfDamage

        val forcePlaceCheck = checkForcePlace(damageInfo)
        val antiSurroundCheck = checkAntiSurround()

        var minDmg = minDamageExplode
        if (forcePlaceCheck) minDmg = min(minDmg, forcePlaceMinDamage)
        if (antiSurroundCheck) minDmg = min(minDmg, 0.1)

        // damage checks
        if (targetDamage < minDmg) return false
        if (selfDamage > maxSelfDamageExplode) return false

        //anti suicide
        if (antiSuicideExplode != 0.0 && (player.health - selfDamage < antiSuicideExplode)) return false

        return true
    }

    private fun EntityEnderCrystal.getCrystalRotation(): Vec3d {
        val box = this.entityBoundingBox
        val x = lerp(box.minX, box.maxX, 0.5)
        val y = box.minY + crystalHeight
        val z = lerp(box.minZ, box.maxZ, 0.5)
        return Vec3d(x, y, z)
    }

    private fun checkExplodeDelay(): Boolean {
        return System.currentTimeMillis() - lastExplodeTime >= explodeDelay
    }

    private fun checkExplodeAttempts(): Boolean {
        return explodeAttempts < maxExplodeAttempts - 1 + getMaxCrystalCount()
    }

    // endregion

    private class DamageInfo(val target: EntityLivingBase, val targetDamage: Float, val selfDamage: Float)

    private abstract class InteractInfo(val lookVec: Vec3d)
    private class PlaceInfo(val pos: BlockPos, val side: EnumFacing, lookVec: Vec3d): InteractInfo(lookVec)
    private class ExplodeInfo(val crystal: EntityEnderCrystal, lookVec: Vec3d): InteractInfo(lookVec)

    private abstract class SyncInfo { val syncTime = System.currentTimeMillis() }
    private class PlaceSyncInfo(val pos: BlockPos): SyncInfo()
    private class ExplodeSyncInfo(val entityId: Int, val position: Vec3d): SyncInfo()
}