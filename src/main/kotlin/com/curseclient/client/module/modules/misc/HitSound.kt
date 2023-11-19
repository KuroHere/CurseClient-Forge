package com.curseclient.client.module.modules.misc

import com.curseclient.client.event.events.AttackEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.SoundUtils
import com.curseclient.client.utility.math.MathUtils
import net.minecraft.entity.item.EntityEnderCrystal


object HitSound: Module(
    "HitSound",
    "UWU nyaaaa~ sounds.",
    Category.MISC
) {
    private val mode by setting("Sound", Mode.MOAN)
    val volume by setting("Volume", 1.0, 0.1, 10.0, 0.1)

    enum class Mode {
        UWU,
        SKEET,
        KEYBOARD,
        MOAN
    }

    init {
        safeListener<AttackEvent.Pre> { event ->
            if (event.entity !is EntityEnderCrystal) {
                when (mode.name) {
                    "UWU" -> SoundUtils.playSound(volume) { "uwu.wav" }
                    "SKEET" -> SoundUtils.playSound(volume) { "skeet.wav" }
                    "KEYBOARD" -> SoundUtils.playSound(volume) { "keypress.wav" }
                    "MOAN" -> {
                        when (MathUtils.random(1f, 3f).toInt()) {
                            1 -> SoundUtils.playSound(volume) { "moan1.wav" }
                            2 -> SoundUtils.playSound(volume) { "moan2.wav" }
                            3 -> SoundUtils.playSound(volume) { "moan3.wav" }
                            else -> SoundUtils.playSound(volume) { "moan4.wav" }

                        }
                    }
                }
            }
        }
    }
}

