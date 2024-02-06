package com.curseclient.client.utility.sound

import baritone.api.utils.Helper.mc
import com.curseclient.client.utility.math.MathUtils
import net.minecraft.client.audio.ISound
import net.minecraft.client.audio.Sound
import net.minecraft.client.audio.SoundEventAccessor
import net.minecraft.client.audio.SoundHandler
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundCategory

class Song {

    companion object {
        val sound: ISound

        init {
            val loc = ResourceLocation("sounds/${getRandomSong()}.ogg")
            sound = object : ISound {
                private val pitch = 1
                private val volume = 1000000

                override fun getSoundLocation(): ResourceLocation {
                    return loc
                }

                override fun createAccessor(soundHandler: SoundHandler): SoundEventAccessor {
                    return SoundEventAccessor(loc, "CurseClient")
                }

                override fun getSound(): Sound {
                    return Sound(getRandomSong(), volume.toFloat(), pitch.toFloat(), 1, Sound.Type.SOUND_EVENT, false)
                }

                override fun getCategory(): SoundCategory {
                    return SoundCategory.VOICE
                }

                override fun canRepeat(): Boolean {
                    return true
                }

                override fun getRepeatDelay(): Int {
                    return 2
                }

                override fun getVolume(): Float {
                    return volume.toFloat()
                }

                override fun getPitch(): Float {
                    return pitch.toFloat()
                }

                override fun getXPosF(): Float {
                    return if (mc.player != null) mc.player.posX.toFloat() else 0f
                }

                override fun getYPosF(): Float {
                    return if (mc.player != null) mc.player.posY.toFloat() else 0f
                }

                override fun getZPosF(): Float {
                    return if (mc.player != null) mc.player.posZ.toFloat() else 0f
                }

                override fun getAttenuationType(): ISound.AttenuationType {
                    return ISound.AttenuationType.LINEAR
                }
            }
        }

        var song_name: String? = null

        private fun getRandomSong(): String {
            val songMap = mapOf(
                1 to "tiredofproblems",
                2 to "axolotl",
                3 to "cant-slow-me-down",
                4 to "morsmordre",
                5 to "aria-math",
                6 to "heathens"
            )

            val randomNumber = MathUtils.random(1.0, 7.0).toInt()
            song_name = songMap[randomNumber] ?: "heathens"

            return song_name!!
        }
    }
}