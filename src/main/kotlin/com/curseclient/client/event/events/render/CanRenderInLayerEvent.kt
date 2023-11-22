package com.curseclient.client.event.events.render

import net.minecraft.block.Block
import net.minecraft.util.BlockRenderLayer

// I HATE JVM
class CanRenderInLayerEvent(val block: Block) {
    var blockRenderLayer: BlockRenderLayer? = null

    fun gettingBlockRenderLayer(): BlockRenderLayer? {
        return blockRenderLayer
    }

    fun setUpBlockRenderLayer(blockRenderLayer: BlockRenderLayer) {
        this.blockRenderLayer = blockRenderLayer
    }

    fun gettingBlock(): Block {
        return block
    }
}
