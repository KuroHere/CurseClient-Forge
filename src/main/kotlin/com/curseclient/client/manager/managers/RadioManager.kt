package com.curseclient.client.manager.managers

import com.curseclient.client.manager.Manager
import javazoom.jl.decoder.JavaLayerException
import javazoom.jl.player.Player
import java.io.InputStream
import java.util.*
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl
import javax.sound.sampled.Port

object RadioManager: Manager("RadioManager") {
    private var player: Player? = null
    private var thread: Thread? = null

    fun stop() {
        if (isRunning()) {
            thread?.interrupt()
            thread = null

            player?.close()
        }
    }

    fun setVolume(vol: Double) {
        try {
            val infos = AudioSystem.getMixerInfo()
            for (info in infos) {
                val mixer = AudioSystem.getMixer(info)
                if (mixer.isLineSupported(Port.Info.SPEAKER)) {
                    val port = mixer.getLine(Port.Info.SPEAKER) as Port
                    port.open()
                    if (port.isControlSupported(FloatControl.Type.VOLUME)) {
                        val volume = port.getControl(FloatControl.Type.VOLUME) as FloatControl
                        volume.value = (vol / 100).toFloat()
                    }
                    port.close()
                }
            }
        } catch (e: Exception) {
            // Handle exception
        }
    }

    fun isRunning(): Boolean {
        return thread != null
    }

    fun start() {
        try {
            player?.let { Objects.requireNonNull(it) }

            thread = Thread {
                try {
                    player?.play()
                } catch (e: JavaLayerException) {
                    try {
                        start()
                    } catch (e1: Exception) {
                        // Handle exception
                    }
                }
            }
            thread?.start()
        } catch (ignore: Exception) {
            // Handle exception or ignore
        }
    }

    fun setStream(inputStream: InputStream) {
        try {
            player = Player(inputStream)
        } catch (e: JavaLayerException) {
            e.printStackTrace()
        }
    }
}