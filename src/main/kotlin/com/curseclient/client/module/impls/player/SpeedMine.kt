package com.curseclient.client.module.impls.player

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.PacketEvent
import com.curseclient.client.event.events.RotationUpdateEvent
import com.curseclient.client.event.events.block.BlockResetEvent
import com.curseclient.client.event.events.block.LeftClickBlockEvent
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.managers.RotationManager
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.items.*
import com.curseclient.client.utility.player.AngleUtils
import com.curseclient.client.utility.player.Rotation
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.esp.AnimatedESPRenderer
import com.curseclient.client.utility.world.BlockUtils
import com.curseclient.mixin.accessor.entity.AccessorPlayerControllerMP
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.init.Enchantments
import net.minecraft.init.MobEffects
import net.minecraft.inventory.ClickType
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.*
import net.minecraft.potion.PotionEffect
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.pow

// I will not fix it - ᓚᘏᗢ
object SpeedMine : Module(
    "SpeedMine",
    "Mines faster [ not work ]", Category.PLAYER
) {

    // General
    var mode by setting("Mode", Mode.PACKET, description = "Mode for SpeedMine")

    var mineSwitch by setting("AutoSwitch", Switch.PACKET, visible = { mode == Mode.PACKET }, description = "Mode when switching to a pickaxe")

    var damage by setting("Damage", 0.0, 0.8, 1.0, 0.1, visible = { mode == Mode.DAMAGE }, description = "Instant block damage")

    // Anticheat
    var rotate by setting("Rotation", Rotation.Rotate.NONE, description = "How to rotate to the mine")

    var strict by setting("AlternateSwitch", true, visible = { mode == Mode.PACKET }, description = "Mines on the correct direction")
    var strictReMine by setting("StrictBreak", true, visible = { mode == Mode.PACKET }, description = "Limits re-mines")
    var reset by setting("NoReset", false, description = "Doesn't allow block break progress to be reset")
    // General
    var range by setting("Range", 5.0, 1.0, 6.0, 1.0, visible = { mode == Mode.PACKET }, description = "Range to mine blocks")

    // Render
    var render by setting("Render", true, description = "Renders a visual over current mining block")
    private val renderer = AnimatedESPRenderer { Triple(HUD.color1.setAlpha(50), HUD.color2.setAlpha(120), 1.0f) }

    // mine info
    private var minePosition: BlockPos? = null
    private var mineFacing: EnumFacing? = null

    // mine damage
    private var mineDamage = 0f
    private var mineBreaks = 0

    // potion info
    private var previousHaste = 0

    override fun onEnable() {
        // save old haste
        if (mc.player.isPotionActive(MobEffects.HASTE)) {
            previousHaste = mc.player.getActivePotionEffect(MobEffects.HASTE)!!.duration
        }
        renderer.reset()
    }

    override fun onDisable() {
        // remove haste effect
        if (mc.player.isPotionActive(MobEffects.HASTE)) {
            mc.player.removePotionEffect(MobEffects.HASTE)
        }

        if (previousHaste > 0) {
            // reapply old haste
            mc.player.addPotionEffect(PotionEffect(MobEffects.HASTE, previousHaste))
        }

        // reset our block info
        minePosition = null
        mineFacing = null
        mineDamage = 0f
        mineBreaks = 0
    }

    init {
        safeListener<Render3DEvent> {
            if (mode == Mode.PACKET && !mc.player.capabilities.isCreativeMode) {
                if (minePosition != null && !mc.world.isAirBlock(minePosition!!)) {
                    // draw box
                    if (render) {
                        renderer.setPosition(minePosition)
                        renderer.draw()
                    }
                }
            }
        }
        safeListener<LeftClickBlockEvent> { event ->
            // make sure the block is breakable
            if (BlockUtils.isBreakable(event.getPos()) && !mc.player.capabilities.isCreativeMode) {
                when (mode) {
                    Mode.CREATIVE -> {
                        // instantly break the block and set the block to air
                        mc.playerController.onPlayerDestroyBlock(event.getPos())
                        mc.world.setBlockToAir(event.getPos())
                    }
                    Mode.PACKET -> {
                        // left click block info
                        if (event.getPos() != minePosition) {
                            // new mine info
                            minePosition = event.getPos()
                            mineFacing = event.getFace()
                            mineDamage = 0f
                            mineBreaks = 0

                            if (minePosition != null && mineFacing != null) {
                                // send the packets to mine the position
                                mc.player.connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, minePosition, mineFacing))
                                mc.player.connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, minePosition, EnumFacing.UP))
                            }
                        }
                    }

                    Mode.DAMAGE -> null
                    Mode.VANILLA -> null
                }
            }
        }
        safeListener<RotationUpdateEvent> { event ->
            if (isActive() && mc.player != null && mc.world != null) {
                // server-side update our rotations
                if (rotate != Rotation.Rotate.NONE) {
                    // mine is complete
                    if (mineDamage > 0.95) {
                        // cancel vanilla rotations, we'll send our own
                        event.isCanceled = true
                        // check if mine position exists
                        minePosition?.let { position ->
                            // angles to block
                            val mineRotation = AngleUtils.calculateAngles(position.add(0.5, 0.5, 0.5))

                            // update rots
                            if (rotate == Rotation.Rotate.CLIENT) {
                                mc.player.rotationYaw = mineRotation.yaw
                                mc.player.rotationYawHead = mineRotation.yaw
                                mc.player.rotationPitch = mineRotation.pitch
                            }

                            // send rotations
                            RotationManager.setRotation(mineRotation)
                        }
                    }
                }
            }
        }
        safeListener<BlockResetEvent> { event ->
            // don't allow block break progress to be reset
            if (reset) {
                event.setCanceled(true)
            }
        }
        safeListener<TickEvent.PlayerTickEvent> {
            // no reason to speedmine in creative mode, blocks break instantly
            if (!mc.player.capabilities.isCreativeMode) {
                if (minePosition != null) {

                    // distance to mine position
                    val mineDistance = BlockUtils.getDistanceToCenter(mc.player, minePosition!!)

                    // limit re-mines
                    if ((mineBreaks >= 2 && strictReMine) || mineDistance > range) {

                        // reset our block info
                        minePosition = null
                        mineFacing = null
                        mineDamage = 0f
                        mineBreaks = 0
                    }
                }

                /*
                 * Idea behind this mode is that blocks often are considered broken on NCP before the damage is
                 * greater than 1, so by breaking them at an earlier time we can break the block slightly faster
                 * (working on NCP)
                 */
                when (mode) {
                    Mode.DAMAGE -> {
                        // if the damage is greater than our specified damage, set the block to full damage
                        if ((mc.playerController as AccessorPlayerControllerMP).curBlockDamageMP > damage.toFloat()) {
                            (mc.playerController as AccessorPlayerControllerMP).curBlockDamageMP = 1f

                            // destroy the block
                            mc.playerController.onPlayerDestroyBlock(minePosition!!)
                        }
                    }

                    Mode.PACKET -> {
                        if (minePosition != null && !mc.world.isAirBlock(minePosition!!)) {

                            // if the block is broken
                            if (mineDamage >= 1) {

                                // make sure combat modules aren't active
                                if (!isEnabled() && !isEnabled()) {

                                    // previous slot
                                    val previousSlot = mc.player.inventory.currentItem

                                    // slot of item (based on slot ids from: https://c4k3.github.io/wiki.vg/images/1/13/Inventory-slots.png)
                                    val swapSlot =
                                        SlotInfo.searchSlot(
                                            getEfficientItem(mc.world.getBlockState(minePosition!!)).item,
                                            InventoryRegion.HOTBAR
                                        ) + 36

                                    // swap with window clicks
                                    if (strict) {

                                        // transaction id
                                        val nextTransactionID = mc.player.openContainer.getNextTransactionID(mc.player.inventory)

                                        // window click
                                        val itemstack = mc.player.openContainer.slotClick(
                                            swapSlot,
                                            mc.player.inventory.currentItem,
                                            ClickType.SWAP,
                                            mc.player
                                        )
                                        mc.player.connection.sendPacket(
                                            CPacketClickWindow(
                                                mc.player.inventoryContainer.windowId,
                                                swapSlot,
                                                mc.player.inventory.currentItem,
                                                ClickType.SWAP,
                                                itemstack,
                                                nextTransactionID
                                            )
                                        )
                                    } else {
                                        // switch to the most efficient item
                                        SlotInfo.switchToItem(
                                            getEfficientItem(mc.world.getBlockState(minePosition!!)).item,
                                            mineSwitch
                                        )
                                    }

                                    // break the block
                                    mc.player.connection.sendPacket(
                                        CPacketPlayerDigging(
                                            CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                                            minePosition!!,
                                            mineFacing!!
                                        )
                                    )
                                    mc.player.connection.sendPacket(
                                        CPacketPlayerDigging(
                                            CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK,
                                            minePosition!!,
                                            EnumFacing.UP
                                        )
                                    )

                                    // FAST SPEED FAST SPEED
                                    if (strict) {
                                        mc.player.connection.sendPacket(
                                            CPacketPlayerDigging(
                                                CPacketPlayerDigging.Action.START_DESTROY_BLOCK,
                                                minePosition!!,
                                                mineFacing!!
                                            )
                                        )
                                    }

                                    mc.player.connection.sendPacket(
                                        CPacketPlayerDigging(
                                            CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                                            minePosition!!,
                                            mineFacing!!
                                        )
                                    )

                                    // save our current slot
                                    if (previousSlot != -1) {

                                        // swap with window clicks
                                        if (strict) {

                                            // transaction id
                                            val nextTransactionID = mc.player.openContainer.getNextTransactionID(mc.player.inventory)

                                            // window click
                                            val itemstack = mc.player.openContainer.slotClick(
                                                swapSlot,
                                                mc.player.inventory.currentItem,
                                                ClickType.SWAP,
                                                mc.player
                                            )
                                            mc.player.connection.sendPacket(
                                                CPacketClickWindow(
                                                    mc.player.inventoryContainer.windowId,
                                                    swapSlot,
                                                    mc.player.inventory.currentItem,
                                                    ClickType.SWAP,
                                                    itemstack,
                                                    nextTransactionID
                                                )
                                            )

                                            // confirm packets
                                            mc.player.connection.sendPacket(
                                                CPacketConfirmTransaction(
                                                    mc.player.inventoryContainer.windowId,
                                                    nextTransactionID,
                                                    true
                                                )
                                            )
                                        } else {
                                            // switch to our previous slot
                                            SlotInfo.switchToSlot(previousSlot, Switch.PACKET)
                                        }
                                    }

                                    // reset don't want this position to be re-mined without delay
                                    mineDamage = 0f
                                    mineBreaks++
                                }
                            }

                            // update block damage
                            mineDamage += getBlockStrength(mc.world.getBlockState(minePosition!!), minePosition!!)
                        } else {
                            mineDamage = 0f // not currently mining
                        }
                    }

                    Mode.VANILLA -> {
                        // add haste and set the block hit delay to 0
                        (mc.playerController as AccessorPlayerControllerMP).setBlockHitDelay(0)
                        mc.player.addPotionEffect(PotionEffect(MobEffects.HASTE.setPotionName("SpeedMine"), 80950, 1, false, false))
                    }

                    Mode.CREATIVE -> null
                }
            }
            // clear haste effect on mode change
            if (mode != Mode.VANILLA) {
                if (mc.player.isPotionActive(MobEffects.HASTE)) {
                    mc.player.removePotionEffect(MobEffects.HASTE)
                }

                if (previousHaste > 0) {
                    // reapply old haste
                    mc.player.addPotionEffect(PotionEffect(MobEffects.HASTE, previousHaste))

                }
            }
        }
        safeListener<PacketEvent.Send> { event ->
            // packet for switching held item
            if (event.packet is CPacketHeldItemChange) {

                // reset our mine time
                if (strict) {
                    mineDamage = 0F
                }
            }
        }
    }

    override fun isActive() = isEnabled() && minePosition != null && !mc.world.isAirBlock(minePosition) && mineDamage > 0


    private fun SafeClientEvent.getEfficientItem(state: IBlockState): ItemStack {
        var bestSlot = -1
        var bestBreakSpeed = 0.0f

        for (i in 0 until 9) {
            val stack = mc.player.inventory.getStackInSlot(i)
            if (!stack.isEmpty) {
                val breakSpeed = stack.getDestroySpeed(state)

                if (breakSpeed > 1) {
                    val efficiencyModifier = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack)
                    var modifiedBreakSpeed = breakSpeed

                    if (efficiencyModifier > 0) {
                        modifiedBreakSpeed += efficiencyModifier.toDouble().pow(2.0).toFloat() + 1
                    }

                    if (modifiedBreakSpeed > bestBreakSpeed) {
                        bestBreakSpeed = modifiedBreakSpeed
                        bestSlot = i
                    }
                }
            }
        }

        return if (bestSlot != -1) {
            mc.player.inventory.getStackInSlot(bestSlot)
        } else mc.player.inventory.getStackInSlot(mc.player.inventory.currentItem)
    }

    private fun SafeClientEvent.getBlockStrength(
        state: IBlockState,
        position: BlockPos
    ): Float {
        val hardness = state.getBlockHardness(mc.world, position)

        if (hardness < 0) {
            return 0f
        }

        return if (!canHarvestBlock(state.block, position)) {
            getDigSpeed(state) / hardness / 100F
        } else {
            getDigSpeed(state) / hardness / 30F
        }
    }

    private fun SafeClientEvent.canHarvestBlock(
        block: Block,
        position: BlockPos
    ): Boolean {
        val worldState = mc.world.getBlockState(position)
        val state = worldState.block.getActualState(worldState, mc.world, position)

        if (state.material.isToolNotRequired) {
            return true
        }

        val stack = getEfficientItem(state)
        val tool = block.getHarvestTool(state)

        return when {
            stack.isEmpty || tool == null -> mc.player.canHarvestBlock(state)
            else -> {
                val toolLevel = stack.item.getHarvestLevel(stack, tool, mc.player, state)
                toolLevel >= 0 && toolLevel >= block.getHarvestLevel(state)
            }
        }
    }

    private fun SafeClientEvent.getDigSpeed(state: IBlockState): Float {
        var digSpeed = getDestroySpeed(state)

        if (digSpeed > 1) {
            val stack = getEfficientItem(state)
            val efficiencyModifier = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack)

            if (efficiencyModifier > 0) {
                digSpeed += efficiencyModifier.toDouble().pow(2.0).toFloat() + 1
            }
        }

        if (mc.player.isPotionActive(MobEffects.HASTE)) {
            digSpeed *= 1 + (mc.player.getActivePotionEffect(MobEffects.HASTE)!!.amplifier + 1) * 0.2F
        }

        // Remaining code for other effects and conditions goes here...

        return if (digSpeed < 0) 0f else digSpeed
    }

    private fun SafeClientEvent.getDestroySpeed(state: IBlockState): Float {
        var destroySpeed = 1.0f

        val stack = getEfficientItem(state)
        if (!stack.isEmpty) {
            destroySpeed *= stack.getDestroySpeed(state)
        }

        return destroySpeed
    }

    enum class Mode {
        /**
         * Mines the block with packets, so the block breaking animation shouldn't be visible
         */
        PACKET,
        /**
         * Sets the block damage when block breaking animation is nearly complete
         */
        DAMAGE,
        /**
         * Adds the [net.minecraft.init.MobEffects] Haste potion effect
         */
        VANILLA,
        /**
         * Sets the block to air
         */
        CREATIVE
    }
}

