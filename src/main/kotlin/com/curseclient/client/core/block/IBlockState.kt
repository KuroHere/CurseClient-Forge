package com.curseclient.client.core.block

import baritone.api.utils.Helper.mc
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos


val BlockPos.state: IBlockState get() = mc.world.getBlockState(this)
val BlockPos.block: Block get() = this.state.block