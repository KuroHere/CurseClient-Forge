package com.curseclient.client.utility.render.esp

import com.curseclient.client.manager.managers.ESPManager
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import java.awt.Color

class ESPRenderInfo(val box: ESPBox, val filledColor: Color, val outlineColor: Color, val sides: List<EnumFacing>, val fullOutline: Boolean, val thickness: Float) {

    constructor(pos: BlockPos, filledColor: Color, outlineColor: Color, sides: List<EnumFacing>, fullOutline: Boolean, thickness: Float) : this(AxisAlignedBB(pos).toESPBox(), filledColor, outlineColor, sides, fullOutline, thickness)

    fun draw() =
        ESPManager.put(this)
}
