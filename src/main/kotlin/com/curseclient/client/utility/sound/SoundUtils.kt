package com.curseclient.client.utility.sound

import com.curseclient.client.event.listener.runTrying
import com.curseclient.client.utility.math.MathUtils.lerp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl


object SoundUtils {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playSound(sound: Sound, volume: Double) {
        val url = when (sound) {
            Sound.ON_SOUND -> "ui/enable.wav"
            Sound.OFF_SOUND -> "ui/disable.wav"
            Sound.CLICK_UI -> "ui/clickguiopen.wav"
            Sound.OPENING -> "ui/opening.wav"
            Sound.GAME_STAR -> "ui/start.wav"
            Sound.INTERFACE1 -> "ui/mc_interface.wav"
            Sound.INTERFACE2 -> "ui/menu_interface.wav"
        }
        try {
            playSound(volume) { url }
        } catch (ignored: Exception) {

        }
    }

    fun playSound(volume: Double = 1.0, url: () -> String) {
        scope.launch { runTrying { play(url(), volume.toFloat()) } }
    }

    private fun play(url: String, volume: Float) {
        val clip = AudioSystem.getClip()
        val audioSrc = this::class.java.getResourceAsStream("/assets/curseclient/sounds/$url") ?: return
        val bufferedIn = BufferedInputStream(audioSrc)
        val inputStream = AudioSystem.getAudioInputStream(bufferedIn)
        clip.open(inputStream)
        val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
        gainControl.value = lerp(-30f, 0f, volume)
        clip.start()
    }

    enum class Sound {
        ON_SOUND,
        OFF_SOUND,
        CLICK_UI,
        GAME_STAR,
        OPENING,
        INTERFACE1,
        INTERFACE2,
    }
}