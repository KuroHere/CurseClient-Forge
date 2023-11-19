package com.curseclient.client.module.modules.movement

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.StepEvent
import com.curseclient.client.event.events.TimerEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.player.MovementUtils
import net.minecraft.network.play.client.CPacketPlayer

object Step : Module(
    "Step",
    "Changes the vanilla behavior for stepping up blocks",
    Category.MOVEMENT
) {
    private val mode by setting("Mode", Mode.Vanilla)
    private val heightSetting by setting("Height", 1.0, 1.0, 3.0, 0.1, { mode == Mode.Vanilla })
    private val timerSetting by setting("Timer Amount", 0.25, 0.1, 1.0, 0.05)

    private var playerY = -1.0
    private var timer = false

    private enum class Mode {
        Vanilla,
        NCP
    }

    init {
        safeListener<TimerEvent>(-100) {
            if (!timer) return@safeListener
            timer = false

            it.speed = timerSetting
        }

        safeListener<StepEvent.Pre> {
            if (!shouldStep()) return@safeListener

            playerY = player.entityBoundingBox.minY

            it.height = when(mode) {
                Mode.Vanilla -> heightSetting
                Mode.NCP -> 1.015
            }
        }

        safeListener<StepEvent.Post> {
            if (!shouldStep()) return@safeListener
            if (mode != Mode.NCP) return@safeListener

            val stepHeight = player.entityBoundingBox.minY - playerY
            if (stepHeight !in 0.6..1.05) return@safeListener

            listOf(0.42, 0.7532, 1.0).forEach {
                connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY + it * stepHeight, player.posZ, false))
            }

            timer = true
        }
    }

    private fun SafeClientEvent.shouldStep(): Boolean {
        return !player.isInWater
            && !player.isInLava
            && player.onGround
            && !player.isOnLadder
            && player.fallDistance < 0.1
            && !player.movementInput.sneak
            && !player.movementInput.jump
            && MovementUtils.isInputting()
            && player.collidedHorizontally
    }

    override fun onEnable() {
        timer = false
    }
}