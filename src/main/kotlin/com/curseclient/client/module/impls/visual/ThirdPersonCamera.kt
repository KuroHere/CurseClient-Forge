package com.curseclient.client.module.impls.visual

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.render.RenderTessellator
import com.curseclient.client.utility.render.graphic.GLUtils
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.InputEvent
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object ThirdPersonCamera : Module(
    "ThirdPersonCamera",
    "Cumera",
    Category.VISUAL
) {
    val noClip by setting("No Clip", false)
    private val distanceSetting by setting("Distance", 4.0, 1.0, 10.0, 0.25)
    private val animationSpeed by setting("Animation Speed", 1.0, 0.1, 5.0, 0.1)
    var distance = 1.0

    @Suppress("unused")
    private enum class PerspectiveMode(val state: Int) {
        BACK(1),
        FRONT(2)
    }

    init {
        safeListener<InputEvent.KeyInputEvent> {
            if (!mc.gameSettings.keyBindTogglePerspective.isKeyDown) return@safeListener

            when(mc.gameSettings.thirdPersonView) {
                0 -> {}
                1 -> distance = getCameraDistance() + 1.0
                2 -> distance = 1.0
            }
        }

        safeListener<Render3DEvent> {
            distance = if (mc.gameSettings.thirdPersonView == 0) 1.0
            else lerp(distance, getCameraDistance(), GLUtils.deltaTimeDouble() * 5.0 * animationSpeed)
        }
    }

    private fun SafeClientEvent.getCameraDistance(): Double {
        val pos = player.getPositionEyes(RenderTessellator.partialTicks)

        var distance = 100.0

        if (!noClip) {
            val yaw = player.rotationYaw
            var pitch = player.rotationPitch
            if (mc.gameSettings.thirdPersonView == 2) pitch += 180f

            val rotateDir = Vec3d(
                -sin(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble())) * distance,
                -sin(Math.toRadians(pitch.toDouble())) * distance,
                cos(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble())) * distance
            )

            for (i in 0..7) {
                val shift = Vec3d(
                    ((i and 1) * 2 - 1).toDouble() + ((i shr 2 and 1) * 2 - 1).toDouble(),
                    ((i shr 1 and 1) * 2 - 1).toDouble(),
                    ((i shr 2 and 1) * 2 - 1).toDouble()
                ).scale(0.1)

                world.rayTraceBlocks(pos.add(shift), pos.subtract(rotateDir).add(shift))?.let {
                    val dist = it.hitVec.distanceTo(pos)
                    distance = min(dist, distance)
                }
            }
        }

        return min(distanceSetting, distance)
    }
}