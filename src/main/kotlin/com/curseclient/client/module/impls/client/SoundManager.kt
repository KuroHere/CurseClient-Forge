package com.curseclient.client.module.impls.client

import com.curseclient.client.event.events.AttackEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.sound.SoundUtils
import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.gui.GuiErrorScreen
import net.minecraft.client.gui.GuiMemoryErrorScreen
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraftforge.client.event.GuiScreenEvent


// TODO: Will
object SoundManager: Module(
    "SoundManager",
    "Modifier client and module sound",
    Category.CLIENT,
    alwaysListenable = true,
    enabledByDefault = true,
) {
    val mcButtonSound by setting("McButtonSound", false)
    private val mcSoundVolume by setting("McButtonVolume", 0.7, 0.0, 2.0, 0.1, { enableSound })

    private val enableSound by setting("EnableSound", true)
    private val enableSoundVolume by setting("EnableVolume", 0.7, 0.0, 2.0, 0.1, { enableSound })

    private val disableSound by setting("DisableSound", true)
    private val disableSoundVolume by setting("DisableVolume", 0.7, 0.0, 2.0, 0.1, { disableSound })

    private val kickSound by setting("DisconnectSound", true)

    private val hitSound by setting("HitSound", HitSound.OFF)
    //private val soundPithMode by setting("PitchMode", Pitch.Random, { hitSound != HitSound.OFF })
    //private val hitSoundPith by setting("HitPitch", 1.0, 0.0, 2.0, 0.1, { hitSound != HitSound.OFF  && soundPithMode == Pitch.Custom})
    private val hitSoundVolume by setting("HitVolume", 1.0, 0.0, 2.0, 0.1, { hitSound != HitSound.OFF })

    private enum class Pitch {
        Random,
        Custom
    }

    private enum class HitSound {
        OFF,
        UWU,
        SKEET,
        KEYBOARD,
        MOAN,
        BLOOD
    }

    init {
        safeListener<GuiScreenEvent> { event ->
            if (kickSound && event.gui is GuiDisconnected || event.gui is GuiErrorScreen || event.gui is GuiMemoryErrorScreen)
                SoundUtils.playSound(0.9) { "kick.wav" }
        }

        safeListener<AttackEvent.Pre> { event ->
            if (event.entity !is EntityEnderCrystal) {
                val soundToPlay = when (hitSound.name) {
                    "UWU" -> "uwu.wav"
                    "SKEET" -> "skeet.wav"
                    "KEYBOARD" -> "keypress.wav"
                    "MOAN" -> {
                        val randomIndex = MathUtils.random(1.0, 4.0).toInt()
                        "moan$randomIndex.wav"
                    }
                    "BLOOD" -> {
                        val randomIndex = MathUtils.random(1.0, 3.0).toInt()
                        "blood$randomIndex.wav"
                    }
                    else -> null
                }

                /*val pitch = when(soundPithMode) {
                    Pitch.Custom -> hitSoundPith
                    Pitch.Random -> MathUtils.random(0.0, 2.0)
                }*/
                soundToPlay?.let {
                    SoundUtils.playSound(hitSoundVolume)/*, pitch*/ { it }
                }
            }
        }
    }

    fun playButton() {
        if (mcButtonSound)
            SoundUtils.playSound(SoundUtils.Sound.INTERFACE1, mcSoundVolume)
    }

    fun playEnable() {
        if (enableSound)
            SoundUtils.playSound(SoundUtils.Sound.ON_SOUND, enableSoundVolume)
    }


    fun playDisable() {
        if (disableSound)
            SoundUtils.playSound(SoundUtils.Sound.OFF_SOUND, disableSoundVolume)
    }

}