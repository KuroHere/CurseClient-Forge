package com.curseclient.client.module.impls.movement

import com.curseclient.client.event.events.MoveEvent
import com.curseclient.client.event.events.TimerEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.settingName

object FastFall: Module(
    "FastFall",
    "Allows you to control fall speed",
    Category.MOVEMENT
) {
    private val mode by setting("Mode", Mode.Motion)
    private val minDist by setting("Min Fall Distance", 3.0, 0.1, 10.0, 0.1)

    private val motionSpeed by setting("Motion Speed", 1.0, 0.01, 10.0, 0.01, visible = {mode == Mode.Motion})
    private val timerSpeed by setting("Timer Speed", 2.0, 0.1, 10.0, 0.1, visible = {mode == Mode.Timer})

    override fun getHudInfo() = mode.settingName

    private enum class Mode {
        Motion,
        Timer
    }

    init {
        safeListener<TimerEvent>(-51) {
            if (player.fallDistance < minDist || player.onGround || player.isInWater || player.isInLava) return@safeListener
            if (mode != Mode.Timer) return@safeListener

            it.speed = timerSpeed
        }

        safeListener<MoveEvent> {
            if (player.fallDistance < minDist || player.onGround || player.isInWater || player.isInLava) return@safeListener
            if (mode != Mode.Motion) return@safeListener

            player.motionY -= motionSpeed * 0.1
        }
    }

}