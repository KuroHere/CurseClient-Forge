package com.curseclient.client.module.modules.movement

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.MoveEvent
import com.curseclient.client.event.listener.Nameable
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.settingName
import com.curseclient.client.utility.math.MathUtils.toInt
import com.curseclient.client.utility.player.MovementUtils
import com.curseclient.client.utility.player.MovementUtils.isInputting
import kotlin.math.cos
import kotlin.math.sin

object LongJump : Module(
    "LongJump",
    "Allows to jump farther",
    Category.MOVEMENT
) {
    private val mode by setting("Mode", Mode.Peek)

    private val peekSpeed by setting("Peek Speed", 1.2, 0.1, 3.0, 0.05, { mode == Mode.Peek })
    private val peekTick by setting("Peek Tick", 2.0, 1.0, 10.0, 1.0, { mode == Mode.Peek })

    private val boostSpeed by setting("Boost Speed", 1.0, 0.1, 5.0, 0.05, { mode == Mode.Boost })

    private var airTicks = 0
    private var groundTicks = 0
    private val dir get() = MovementUtils.calcMoveRad()

    private enum class Mode(override val displayName: String): Nameable {
        Peek("Peek"),
        Boost("Boost"),
    }

    override fun getHudInfo() = mode.settingName

    init {
        safeListener<MoveEvent> {
            airTicks = (airTicks + 1) * (!player.onGround).toInt()
            groundTicks = (groundTicks + 1) * (player.onGround && isInputting()).toInt()

            if (!isInputting()) return@safeListener
            if (player.onGround) {
                if (!mc.gameSettings.keyBindJump.isKeyDown) player.jump()
                return@safeListener
            }

            when (mode) {
                Mode.Boost -> boostMode()
                Mode.Peek -> peekMode()
            }
        }
    }

    private fun SafeClientEvent.boostMode() {
        if (player.motionY <= 0.0) return

        player.isSprinting = true

        player.motionX -= sin(dir) * 0.05 * boostSpeed
        player.motionZ += cos(dir) * 0.05 * boostSpeed
    }

    private fun SafeClientEvent.peekMode() {
        val tick = peekTick.toInt()

        if (airTicks == tick) {
            player.motionX = -sin(dir) * peekSpeed
            player.motionZ = cos(dir) * peekSpeed
        } else if (airTicks < tick) {
            player.motionX = -sin(dir) * 0.1
            player.motionZ = cos(dir) * 0.1
        }
    }
}