package com.curseclient.client.module.impls.misc

import com.curseclient.client.manager.managers.RadioManager
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import java.net.URL

object Radio: Module(
    "Radio",
    "Pip pop pip",
    Category.MISC
) {

    private val radioPlayer = RadioManager

    private val volume by setting("Volume", 50.0, 0.0, 100.0, 1.0)
    private val radios by setting("RadioChannels", RadioChannels.IloveRadio)

    override fun onEnable() {
        if (mc.world == null || mc.player == null) return
        this.radioPlayer.setVolume(volume)
        playMusic()
    }

    override fun onDisable() = radioPlayer.stop()

    private fun playMusic() {
        when (radios) {
            RadioChannels.IloveRadio -> {
                radioPlayer.stop()
                radioPlayer.setStream(URL("https://streams.ilovemusic.de/iloveradio1.mp3").openStream())
                radioPlayer.start()
            }

            RadioChannels.Ilove2Dance -> {
                radioPlayer.stop()
                radioPlayer.setStream(URL("https://streams.ilovemusic.de/iloveradio2.mp3").openStream())
                radioPlayer.start()
            }

            RadioChannels.IloveChillHop -> {
                radioPlayer.stop()
                radioPlayer.setStream(URL("https://streams.ilovemusic.de/iloveradio17.mp3").openStream())
                radioPlayer.start()
            }

            RadioChannels.IlDeutschrap -> {
                radioPlayer.stop()
                radioPlayer.setStream(URL("https://streams.ilovemusic.de/iloveradio6.mp3").openStream())
                radioPlayer.start()
            }

            RadioChannels.IlGreatestHits -> {
                radioPlayer.stop()
                radioPlayer.setStream(URL("https://streams.ilovemusic.de/iloveradio16.mp3").openStream())
                radioPlayer.start()
            }

            RadioChannels.IloveHardstyle -> {
                radioPlayer.stop()
                radioPlayer.setStream(URL("https://streams.ilovemusic.de/iloveradio21.mp3").openStream())
                radioPlayer.start()
            }

            RadioChannels.IloveHipHop -> {
                radioPlayer.stop()
                radioPlayer.setStream(URL("https://streams.ilovemusic.de/iloveradio3.mp3").openStream())
                radioPlayer.start()
            }

            RadioChannels.IloveMashup -> {
                radioPlayer.stop()
                radioPlayer.setStream(URL("https://streams.ilovemusic.de/iloveradio5.mp3").openStream())
                radioPlayer.start()
            }

            RadioChannels.IloveTheClub -> {
                radioPlayer.stop()
                radioPlayer.setStream(URL("https://streams.ilovemusic.de/iloveradio20.mp3").openStream())
                radioPlayer.start()
            }
        }
    }

    enum class RadioChannels {
        IloveRadio,
        Ilove2Dance,
        IloveChillHop,
        IlDeutschrap,
        IlGreatestHits,
        IloveHardstyle,
        IloveHipHop,
        IloveMashup,
        IloveTheClub
    }

}