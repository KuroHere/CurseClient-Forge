package com.curseclient.client.module.modules.misc

import com.curseclient.client.event.events.RunGameLoopEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.TickTimer
import net.minecraft.entity.player.EnumPlayerModelParts

object SkinFlicker : Module(
    name = "SkinFlicker",
    description = "Toggles your skin layers rapidly",
    category = Category.MISC
) {
    private val mode by setting("Mode", FlickerMode.HORIZONTAL)
    private val delay by setting("Delay", 10.0, 0.0, 500.0, 5.0, description = "Skin layer toggle delay")

    private enum class FlickerMode {
        HORIZONTAL, VERTICAL, RANDOM
    }

    private val timer = TickTimer()
    private var lastIndex = 0

    init {
        safeListener<RunGameLoopEvent.Tick> {
            if (!timer.tick(delay.toLong())) return@safeListener

            val part = when (mode) {
                FlickerMode.RANDOM -> EnumPlayerModelParts.values().random()
                FlickerMode.VERTICAL -> verticalParts[lastIndex]
                FlickerMode.HORIZONTAL -> horizontalParts[lastIndex]
            }
            mc.gameSettings.switchModelPartEnabled(part)
            lastIndex = (lastIndex + 1) % 7
        }
    }

    override fun onDisable() {
        for (model in EnumPlayerModelParts.values()) {
            mc.gameSettings.setModelPartEnabled(model, true)
        }
    }


    private val horizontalParts = arrayOf(
        EnumPlayerModelParts.LEFT_SLEEVE,
        EnumPlayerModelParts.LEFT_PANTS_LEG,
        EnumPlayerModelParts.JACKET,
        EnumPlayerModelParts.HAT,
        EnumPlayerModelParts.CAPE,
        EnumPlayerModelParts.RIGHT_PANTS_LEG,
        EnumPlayerModelParts.RIGHT_SLEEVE
    )

    private val verticalParts = arrayOf(
        EnumPlayerModelParts.HAT,
        EnumPlayerModelParts.JACKET,
        EnumPlayerModelParts.CAPE,
        EnumPlayerModelParts.LEFT_SLEEVE,
        EnumPlayerModelParts.RIGHT_SLEEVE,
        EnumPlayerModelParts.LEFT_PANTS_LEG,
        EnumPlayerModelParts.RIGHT_PANTS_LEG
    )
}