package com.curseclient.client.utility.world

import baritone.api.utils.Helper.mc
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import kotlin.math.sqrt

object BlockUtils {

    private val resistantBlocks: List<Block> = listOf(
        Blocks.OBSIDIAN,
        Blocks.ANVIL,
        Blocks.ENCHANTING_TABLE,
        Blocks.ENDER_CHEST,
        Blocks.BEACON
    )

    // All blocks that are unbreakable with tools in survival mode
    private val unbreakableBlocks: List<Block> = listOf(
        Blocks.BEDROCK,
        Blocks.COMMAND_BLOCK,
        Blocks.CHAIN_COMMAND_BLOCK,
        Blocks.END_PORTAL_FRAME,
        Blocks.BARRIER,
        Blocks.PORTAL
    )

    /**
     * Finds the if a given position is breakable
     * @param position The position to check
     * @return Whether or not the given position is breakable
     */
    fun isBreakable(position: BlockPos): Boolean {
        return getResistance(position) != Resistance.UNBREAKABLE
    }

    private fun getResistance(position: BlockPos): Resistance {
        // the block at the given position

        val block = mc.world.getBlockState(position).block

        // idk why this would be null but it throws errors
        if (block != null) {
            // find resistance

            return if (resistantBlocks.contains(block)) {
                Resistance.RESISTANT
            } else if (unbreakableBlocks.contains(block)) {
                Resistance.UNBREAKABLE
            } else if (block.defaultState.material.isReplaceable) {
                Resistance.REPLACEABLE
            } else {
                Resistance.BREAKABLE
            }
        }

        return Resistance.NONE
    }

    fun getDistanceToCenter(player: EntityPlayer, inBlockPos: BlockPos): Double {
        // distances
        val dX = inBlockPos.x + 0.5 - player.posX
        val dY = inBlockPos.y + 0.5 - player.posY
        val dZ = inBlockPos.z + 0.5 - player.posZ

        // distance to center
        return sqrt((dX * dX) + (dY * dY) + (dZ * dZ))
    }

    // the resistance level of the block
    enum class Resistance {
        REPLACEABLE,
        BREAKABLE,
        RESISTANT,
        UNBREAKABLE,
        NONE
    }
}