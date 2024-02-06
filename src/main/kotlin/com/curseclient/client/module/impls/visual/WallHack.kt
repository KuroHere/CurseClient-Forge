package com.curseclient.client.module.impls.visual

import com.curseclient.client.event.events.render.*
import com.curseclient.client.event.events.world.SetOpaqueCubeEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.world.WorldUtils.getBlockState
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.common.ForgeModContainer
import net.minecraftforge.event.entity.minecart.MinecartUpdateEvent
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

object WallHack: Module(
    "WallHack",
    "Makes blocks transparent to see ores or other blocks.",
    Category.VISUAL
) {

    var mode by setting("Mode", Mode.Circuits)
    var opacity by setting("Opacity", 120F, 0f, 255f, 5f, )
    var softReload by setting("SoftReload", true)
    var bypass by setting("Bypass", false)

    private val normalBlock = getNormalBlocks()
    private val circuitBlock = getCircuitBlocks()

    private fun getNormalBlocks(): ArrayList<Block> {
        return arrayListOf<Block>().apply {
            add(Blocks.EMERALD_ORE)
            add(Blocks.GOLD_ORE)
            add(Blocks.IRON_ORE)
            add(Blocks.COAL_ORE)
            add(Blocks.LAPIS_ORE)
            add(Blocks.DIAMOND_ORE)
            add(Blocks.REDSTONE_ORE)
            add(Blocks.LIT_REDSTONE_ORE)
            add(Blocks.TNT)
            add(Blocks.EMERALD_ORE)
            add(Blocks.FURNACE)
            add(Blocks.LIT_FURNACE)
            add(Blocks.DIAMOND_BLOCK)
            add(Blocks.IRON_BLOCK)
            add(Blocks.GOLD_BLOCK)
            add(Blocks.EMERALD_BLOCK)
            add(Blocks.QUARTZ_ORE)
            add(Blocks.BEACON)
            add(Blocks.MOB_SPAWNER)
        }
    }

    private fun getCircuitBlocks(): ArrayList<Block> {
        return arrayListOf<Block>().apply {
            add(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE)
            add(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE)
            add(Blocks.STONE_PRESSURE_PLATE)
            add(Blocks.WOODEN_PRESSURE_PLATE)
            add(Blocks.STONE_BUTTON)
            add(Blocks.WOODEN_BUTTON)
            add(Blocks.LEVER)
            add(Blocks.COMMAND_BLOCK)
            add(Blocks.CHAIN_COMMAND_BLOCK)
            add(Blocks.REPEATING_COMMAND_BLOCK)
            add(Blocks.DAYLIGHT_DETECTOR)
            add(Blocks.DAYLIGHT_DETECTOR_INVERTED)
            add(Blocks.DISPENSER)
            add(Blocks.DROPPER)
            add(Blocks.HOPPER)
            add(Blocks.OBSERVER)
            add(Blocks.TRAPDOOR)
            add(Blocks.IRON_TRAPDOOR)
            add(Blocks.REDSTONE_BLOCK)
            add(Blocks.REDSTONE_LAMP)
            add(Blocks.REDSTONE_TORCH)
            add(Blocks.UNLIT_REDSTONE_TORCH)
            add(Blocks.REDSTONE_WIRE)
            add(Blocks.POWERED_REPEATER)
            add(Blocks.UNPOWERED_REPEATER)
            add(Blocks.POWERED_COMPARATOR)
            add(Blocks.UNPOWERED_COMPARATOR)
            add(Blocks.LIT_REDSTONE_LAMP)
            add(Blocks.REDSTONE_ORE)
            add(Blocks.LIT_REDSTONE_ORE)
            add(Blocks.ACACIA_DOOR)
            add(Blocks.DARK_OAK_DOOR)
            add(Blocks.BIRCH_DOOR)
            add(Blocks.JUNGLE_DOOR)
            add(Blocks.OAK_DOOR)
            add(Blocks.SPRUCE_DOOR)
            add(Blocks.DARK_OAK_DOOR)
            add(Blocks.IRON_DOOR)
            add(Blocks.OAK_FENCE)
            add(Blocks.SPRUCE_FENCE)
            add(Blocks.BIRCH_FENCE)
            add(Blocks.JUNGLE_FENCE)
            add(Blocks.DARK_OAK_FENCE)
            add(Blocks.ACACIA_FENCE)
            add(Blocks.OAK_FENCE_GATE)
            add(Blocks.SPRUCE_FENCE_GATE)
            add(Blocks.BIRCH_FENCE_GATE)
            add(Blocks.JUNGLE_FENCE_GATE)
            add(Blocks.DARK_OAK_FENCE_GATE)
            add(Blocks.ACACIA_FENCE_GATE)
            add(Blocks.JUKEBOX)
            add(Blocks.NOTEBLOCK)
            add(Blocks.PISTON)
            add(Blocks.PISTON_EXTENSION)
            add(Blocks.PISTON_HEAD)
            add(Blocks.STICKY_PISTON)
            add(Blocks.TNT)
            add(Blocks.SLIME_BLOCK)
            add(Blocks.TRIPWIRE)
            add(Blocks.TRIPWIRE_HOOK)
            add(Blocks.RAIL)
            add(Blocks.ACTIVATOR_RAIL)
            add(Blocks.DETECTOR_RAIL)
            add(Blocks.GOLDEN_RAIL)
        }
    }

    private var previousForgeLightPipelineEnabled = false
    private var oldAmbience = 0
    private var cachedOpacity = 0f

    private var previousTest = false

    init {
        safeListener<MinecartUpdateEvent> {
            reloadWorld()
        }
        safeListener<Render3DEvent> {
            if (cachedOpacity.toDouble() != opacity) {
                cachedOpacity = opacity.toFloat()
                reloadWorld()
            }
        }
        safeListener<ShouldSetupTerrainEvent> { event ->
            event.cancel()
        }

        safeListener<ComputeVisibilityEvent> { event ->
            event.cancel()
        }

        safeListener<SetOpaqueCubeEvent> { event ->
            event.cancel()
        }
        safeListener<RenderPutColorMultiplierEvent> { event ->
            event.opacity = opacity.toFloat()
            event.cancel()
        }

        safeListener<CanRenderInLayerEvent> { event ->
            if (!containsBlock(event.gettingBlock())) event.blockRenderLayer = BlockRenderLayer.TRANSLUCENT
        }


    }

    enum class Mode {
        Normal, Circuits
    }

    private fun doBypass() {
        val i = 16
        for (posX in -i until i) {
            for (posY in i downTo -i + 1) {
                for (posZ in -i until i) {
                    val z = mc.player.posX.toInt() + posX
                    val y = mc.player.posY.toInt() + posY
                    val x = mc.player.posZ.toInt() + posZ
                    val blockPos = BlockPos(x, y, z)

                    val block = blockPos.getBlockState()
                    if (block == Blocks.DIAMOND_ORE) mc.playerController.clickBlock(blockPos, EnumFacing.DOWN)
                }
            }
        }
    }

    override fun onEnable() {
        if (bypass) doBypass()

        previousForgeLightPipelineEnabled = ForgeModContainer.forgeLightPipelineEnabled
        oldAmbience = mc.gameSettings.ambientOcclusion

        try {
            val class_ = Class.forName("net.minecraftforge.common.ForgeModContainer", true, this.javaClass.classLoader)
            val field2 = class_.getDeclaredField("forgeLightPipelineEnabled")
            val isAccessible = field2.isAccessible
            field2.isAccessible = true
            previousTest = field2.getBoolean(null)
            field2[null] = false
            field2.isAccessible = isAccessible
        } catch (ignored: Exception) {
        }

        cachedOpacity = opacity.toFloat()
        ForgeModContainer.forgeLightPipelineEnabled = false

        mc.renderChunksMany = false
        mc.gameSettings.gammaSetting = 11F
        mc.gameSettings.ambientOcclusion = 0

        reloadWorld()
    }

    override fun onDisable() {

        try {
            val class_ = Class.forName("net.minecraftforge.common.ForgeModContainer", true, this.javaClass.classLoader)
            val field = (class_).getDeclaredField("forgeLightPipelineEnabled")
            val isAccessible = field.isAccessible
            field.isAccessible = true
            field[null] = previousTest
            field.isAccessible = isAccessible
        } catch (ignored: Exception) {
        }

        mc.renderChunksMany = true
        ForgeModContainer.forgeLightPipelineEnabled = true
        mc.gameSettings.ambientOcclusion = oldAmbience
        mc.gameSettings.gammaSetting = 1f

        reloadWorld()
    }

    private fun reloadWorld() {
        if (mc.renderGlobal == null) return

        if (softReload) {
            mc.addScheduledTask {
                val x = mc.player.posX.toInt()
                val y = mc.player.posY.toInt()
                val z = mc.player.posZ.toInt()
                val distance = mc.gameSettings.renderDistanceChunks * 16
                mc.renderGlobal.markBlockRangeForRenderUpdate(x - distance, y - distance, z - distance, x + distance, y + distance, z + distance)
            }

        } else {
            mc.renderGlobal.loadRenderers()
        }
    }

    fun containsBlock(block: Block?): Boolean {
        if (mode == Mode.Normal) return normalBlock.contains(block)
        return circuitBlock.contains(block)
    }

    fun processShouldSideBeRendered(block: Block, blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing, callback: CallbackInfoReturnable<Boolean>) {
        callback.returnValue = containsBlock(block)
    }

    fun processGetLightValue(block: Block, callback: CallbackInfoReturnable<Int>) {
        if (containsBlock(block)) callback.returnValue = 1
    }

    fun processGetAmbientOcclusionLightValue(block: Block, callback: CallbackInfoReturnable<Float>) {
        callback.returnValue = 1F
    }

}