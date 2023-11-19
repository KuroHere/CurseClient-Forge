package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.events.ConnectionEvent
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.listener
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.math.MathUtils.toInt
import com.curseclient.client.utility.render.graphic.GLUtils

object SmoothCrouch : Module(
    "SmoothCrouch",
    "Throws exceptions",
    Category.VISUAL
) {
    private val speed by setting("Speed", 1.0, 0.1, 5.0, 0.1)
    private val height by setting("Height", 1.0, 0.5, 3.0, 0.1)

    private var crouchProgress = 0.0

    init {
        safeListener<Render3DEvent> {
            crouchProgress = lerp(crouchProgress, player.isSneaking.toInt().toDouble(), GLUtils.deltaTimeDouble() * 10.0 * speed)
        }

        listener<ConnectionEvent.Connect> {
            crouchProgress = 0.0
        }

        listener<ConnectionEvent.Disconnect> {
            crouchProgress = 0.0
        }
    }

    @JvmStatic
    fun getCrouchProgress(): Float {
        return (crouchProgress * height * 0.1).toFloat()
    }

    override fun onEnable() {
        crouchProgress = 0.0
    }
}