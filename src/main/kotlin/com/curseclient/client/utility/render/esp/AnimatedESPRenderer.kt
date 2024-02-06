package com.curseclient.client.utility.render.esp

import com.curseclient.client.utility.render.animation.ease.EaseUtils
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.render.vector.Conversion.toVec3dCenter
import com.curseclient.client.utility.render.graphic.GLUtils
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class AnimatedESPRenderer(val colors: () -> Triple<Color, Color, Float>) {
    private var pos: BlockPos? = null
    private var renderPos: Vec3d = Vec3d.ZERO
    private var size = 1.0
    private val renderer = ESPRenderer()

    var maxSize = 1.0
    var animationSpeed = 1.0

    var showSpeed: Pair<Double, Double> = 1.0 to 1.0

    fun draw() {
        val p = pos
        if (p != null) {
            size = min(maxSize, size + GLUtils.deltaTimeDouble() * showSpeed.first * 1.5)
            renderPos = lerp(renderPos, p.toVec3dCenter(), GLUtils.deltaTimeDouble() * animationSpeed * 8.0)
        } else size = max(0.0, size - GLUtils.deltaTimeDouble() * showSpeed.second * 4.0)

        val s = EaseUtils.getEase(size, EaseUtils.EaseType.OutCubic)

        if (s < 0.05) return

        val pos1 = renderPos.subtract(s * 0.5, s * 0.5, s * 0.5)
        val pos2 = renderPos.add(s * 0.5, s * 0.5, s * 0.5)
        val box = ESPBox(pos1, pos2)

        val c = colors()

        renderer.thickness = c.third
        renderer.put(box, c.first, c.second)
        renderer.render()
    }

    fun reset() {
        size = 0.0
        pos = null
    }

    fun setPosition(blockPos: BlockPos?) {
        if (size < 0.05) blockPos?.let { renderPos = it.toVec3dCenter() }
        pos = blockPos
    }
}