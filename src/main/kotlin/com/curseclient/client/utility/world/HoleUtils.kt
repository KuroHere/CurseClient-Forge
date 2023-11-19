package com.curseclient.client.utility.world

import com.curseclient.client.event.SafeClientEvent
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos

object HoleUtils {
    val normalHole = arrayOf(
        BlockPos(0, -1, 0),
        BlockPos(0, 0, -1),
        BlockPos(1, 0, 0),
        BlockPos(0, 0, 1),
        BlockPos(-1, 0, 0)
    )

    private val doubleXHole = arrayOf(
        BlockPos(0, -1, 0),
        BlockPos(1, -1, 0),

        BlockPos(2, 0, 0),
        BlockPos(-1, 0, 0),
        BlockPos(1, 0, 1),
        BlockPos(1, 0, -1),
        BlockPos(0, 0, 1),
        BlockPos(0, 0, -1),
    )

    private val doubleZHole = arrayOf(
        BlockPos(0, -1, 0),
        BlockPos(0, -1, 1),

        BlockPos(0, 0, 2),
        BlockPos(0, 0, -1),
        BlockPos(1, 0, 1),
        BlockPos(-1, 0, 1),
        BlockPos(1, 0, 0),
        BlockPos(-1, 0, 0),
    )

    private val quadHole = arrayOf(
        BlockPos(0, -1, 0),
        BlockPos(1, -1, 0),
        BlockPos(1, -1, 1),
        BlockPos(0, -1, 1),

        BlockPos(-1, 0, 0),
        BlockPos(-1, 0, 1),
        BlockPos(0, 0, 2),
        BlockPos(1, 0, 2),
        BlockPos(2, 0, 0),
        BlockPos(2, 0, 1),
        BlockPos(0, 0, -1),
        BlockPos(1, 0, -1),
    )

    val surroundOffsetFull = arrayOf(
        BlockPos(0, -1, 0),
        BlockPos(0, 0, -1),
        BlockPos(1, 0, 0),
        BlockPos(0, 0, 1),
        BlockPos(-1, 0, 0),
        BlockPos(1, 0, 1),
        BlockPos(-1, 0, 1),
        BlockPos(1, 0, -1),
        BlockPos(-1, 0, -1),
    )

    val surroundOffsetTrap = arrayOf(
        BlockPos(0, -1, 0),
        BlockPos(0, 0, -1),
        BlockPos(1, 0, 0),
        BlockPos(0, 0, 1),
        BlockPos(-1, 0, 0),
        BlockPos(0, 1, -1),
        BlockPos(1, 1, 0),
        BlockPos(0, 1, 1),
        BlockPos(-1, 1, 0),
        BlockPos(0, 2, 0),
    )

    fun SafeClientEvent.getHoleType(pos: BlockPos) =
        getNormalHole(pos) ?: getDoubleXHole(pos) ?: getDoubleZHole(pos) ?: getQuadHole(pos)

    fun SafeClientEvent.getNormalHole(pos: BlockPos): HoleType? {
        if (!checkHeight(pos)) return null
        var type = HoleType.BEDROCK

        normalHole.forEach { offset ->
            val block = world.getBlockState(pos.add(offset)).block

            if (!checkBlock(block))
                return null

            if (block != Blocks.BEDROCK) type = HoleType.OBSIDIAN
        }

        return type
    }

    private fun SafeClientEvent.getDoubleXHole(pos: BlockPos): HoleType? {
        if (!checkHeight(pos) || !checkHeight(pos.add(1, 0, 0))) return null

        val isDoubleHole = doubleXHole.all { offset ->
            val block = world.getBlockState(pos.add(offset)).block
            checkBlock(block)
        }

        return if (isDoubleHole) HoleType.DOUBLE_X else null
    }

    private fun SafeClientEvent.getDoubleZHole(pos: BlockPos): HoleType? {
        if (!checkHeight(pos) || !checkHeight(pos.add(0, 0, 1))) return null

        val isDoubleHole = doubleZHole.all { offset ->
            val block = world.getBlockState(pos.add(offset)).block
            checkBlock(block)
        }

        return if (isDoubleHole) HoleType.DOUBLE_Z else null
    }

    private fun SafeClientEvent.getQuadHole(pos: BlockPos): HoleType? {
        if (!checkHeight(pos) ||
            !checkHeight(pos.add(1, 0, 0)) ||
            !checkHeight(pos.add(1, 0, 1)) ||
            !checkHeight(pos.add(0, 0, 1))
        ) return null

        val isQuadHole = quadHole.all { offset ->
            val block = world.getBlockState(pos.add(offset)).block
            checkBlock(block)
        }

        return if (isQuadHole) HoleType.QUAD else null
    }

    private fun SafeClientEvent.checkHeight(pos: BlockPos): Boolean =
        world.isAirBlock(pos) && world.isAirBlock(pos.up()) && world.isAirBlock(pos.up().up())

    private fun checkBlock(block: Block): Boolean =
        block == Blocks.BEDROCK || block == Blocks.OBSIDIAN || block == Blocks.ENDER_CHEST || block == Blocks.ANVIL

    fun SafeClientEvent.checkHole(entity: Entity) =
        getHoleType(BlockPos(entity.positionVector))

    fun SafeClientEvent.getHoleList(startPos: BlockPos, hRange: Int, vRange: Int): ArrayList<Pair<BlockPos, HoleType>> {
        return ArrayList<Pair<BlockPos, HoleType>>().apply {
            getHoleBaseBlockList(startPos, hRange, vRange).forEach {
                val pos = it.first
                val type = it.second

                add(pos to type)
                when(type) {
                    HoleType.DOUBLE_X -> {
                        add(pos.add(1, 0, 0) to type)
                    }
                    HoleType.DOUBLE_Z -> {
                        add(pos.add(0, 0, 1) to type)
                    }
                    HoleType.QUAD -> {
                        add(pos.add(1, 0, 0) to type)
                        add(pos.add(0, 0, 1) to type)
                        add(pos.add(1, 0, 1) to type)
                    }
                    else -> {}
                }
            }
        }

    }
    fun SafeClientEvent.getHoleBaseBlockList(startPos: BlockPos, hRange: Int, vRange: Int): ArrayList<Pair<BlockPos, HoleType>> {
        return ArrayList<Pair<BlockPos, HoleType>>().apply {
            val baseBlocks = getBlockSequence(startPos, hRange, vRange).mapNotNull { pos -> getHoleType(pos)?.let { pos to it } }
            baseBlocks.forEach {
                val pos = it.first
                val type = it.second

                add(pos to type)
            }
        }
    }
}

enum class HoleType {
    OBSIDIAN, BEDROCK, DOUBLE_X, DOUBLE_Z, QUAD
}

fun getBlockSequence(startPos: BlockPos, hRange: Int, vRange: Int): ArrayList<BlockPos> {
    val blocks = ArrayList<BlockPos>()
    for (x in startPos.x - hRange..startPos.x + hRange) {
        for (y in startPos.y - vRange..startPos.y + vRange) {
            for (z in startPos.z - hRange..startPos.z + hRange) {
                blocks.add(BlockPos(x, y, z))
            }
        }
    }

    return blocks
}