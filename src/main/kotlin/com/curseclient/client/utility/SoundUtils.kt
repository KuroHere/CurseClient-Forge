package com.curseclient.client.utility

import baritone.api.utils.Helper.mc
import com.curseclient.client.event.listener.runTrying
import com.curseclient.client.utility.math.MathUtils.lerp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import java.io.BufferedInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl


object SoundUtils {
    private val scope = CoroutineScope(Dispatchers.Default)

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
}