package com.curseclient.client.event.events.block

import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.eventhandler.Event

class LeftClickBlockEvent(private val blockPos: BlockPos, private val blockFace: EnumFacing) : Event() {

    fun getPos(): BlockPos {
        return blockPos
    }

    fun getFace(): EnumFacing {
        return blockFace
    }
}