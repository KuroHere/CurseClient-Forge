package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.onMainThread
import com.curseclient.client.event.listener.runSafe
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.entity.flooredPosition
import com.curseclient.client.utility.render.ColorUtils.setAlphaD
import com.curseclient.client.utility.render.esp.ESPBox
import com.curseclient.client.utility.render.esp.ESPRenderer
import com.curseclient.client.utility.render.esp.toESPBox
import com.curseclient.client.utility.threads.loop.DelayedLoopThread
import com.curseclient.client.utility.world.HoleType
import com.curseclient.client.utility.world.HoleUtils.getHoleBaseBlockList
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import java.awt.Color


object HoleESP : Module(
    "HoleESP",
    "Draws esp for holes",
    Category.VISUAL
){
    private val mode by setting("Mode", Mode.Flat)
    private val depth by setting("Depth", false, {mode == Mode.Box})

    private val obsidianColor by setting("Obsidian Color", Color(160, 20, 255))
    private val bedrockColor by setting("Bedrock Color", Color(255, 20, 20))
    private val doubleColor by setting("Double Color", Color(160, 20, 255))
    private val quadColor by setting("Quad Color", Color(160, 20, 255))
    private val filledAlpha by setting("Filled Alpha", 0.25, 0.0, 1.0, 0.01)
    private val outlineAlpha by setting("Outline Alpha", 0.5, 0.0, 1.0, 0.01)
    private val horizontalRange by setting("Horizontal Range", 8.0, 4.0, 32.0, 1.0)
    private val verticalRange by setting("Vertical Range", 8.0, 4.0, 32.0, 1.0)
    private val outlineWidth by setting("Outline Width", 1.0, 1.0, 5.0, 0.1)
    private val updateDelay by setting("Update Delay", 300.0, 50.0, 1000.0, 5.0)

    private var toRender = emptyList<Pair<BlockPos, HoleType>>()
    private val renderer = ESPRenderer()

    enum class Mode {
        Flat,
        Box
    }

    private val tickThread = DelayedLoopThread("Hole ESP Thread", { isEnabled() && mc.world != null }, { updateDelay.toLong() }) {
        runSafe {
            val holes = getHoleBaseBlockList(player.flooredPosition, horizontalRange.toInt(), verticalRange.toInt())
            onMainThread {
                renderer.clear()
                holes.forEach { hole ->
                    put(getHoleBox(hole.first, hole.second), hole.second.getColor())
                }
            }
        }
    }

    init {
        tickThread.reload()
        safeListener<Render3DEvent> {
            renderer.thickness = outlineWidth.toFloat()
            renderer.render(false)
        }
    }

    override fun onEnable() {
        toRender = emptyList()
        tickThread.interrupt()
    }

    private fun HoleType.getColor(): Color {
        return when(this) {
            HoleType.OBSIDIAN -> obsidianColor
            HoleType.BEDROCK -> bedrockColor
            HoleType.DOUBLE_X -> doubleColor
            HoleType.DOUBLE_Z -> doubleColor
            HoleType.QUAD -> quadColor
        }
    }

    private fun getHoleBox(pos: BlockPos, type: HoleType): ESPBox {
        val x = pos.x.toDouble()
        val y = pos.y.toDouble()
        val z = pos.z.toDouble()
        val height = 1.0

        return when(type) {
            HoleType.DOUBLE_X -> {
                AxisAlignedBB(x, y, z, x + 2.0, y + height, z + 1.0)
            }
            HoleType.DOUBLE_Z -> {
                AxisAlignedBB(x, y, z, x + 1.0, y + height, z + 2.0)
            }
            HoleType.QUAD -> {
                AxisAlignedBB(x, y, z, x + 2.0, y + height, z + 2.0)
            }
            else -> {
                AxisAlignedBB(x, y, z, x + 1.0, y + height, z + 1.0)
            }
        }.toESPBox()
    }

    private fun put(box: ESPBox, color: Color) {
        val list = when (mode) {
            Mode.Flat -> listOf(EnumFacing.DOWN)
            Mode.Box -> if (depth)
                listOf(EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH)
            else
                listOf(EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH)
        }
        list.let { renderer.put(box, color.setAlphaD(filledAlpha), color.setAlphaD(outlineAlpha), it) }
    }
}