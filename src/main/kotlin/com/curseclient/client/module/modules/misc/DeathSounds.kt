package com.curseclient.client.module.modules.misc

import com.curseclient.client.event.events.EntityDeathEvent
import com.curseclient.client.module.Module
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.misc.SoundUtils
import com.curseclient.client.utility.extension.settingName
import net.minecraft.entity.player.EntityPlayer

object DeathSounds : Module(
    "DeathSounds",
    "Beep",
    Category.MISC
) {
    private val mode by setting("Mode", Mode.Beep)
    private val volumeSetting by setting("Volume", 1.0, 0.1, 1.0, 0.05)
    private val onlyPlayers by setting("Only Players", false)

    private enum class Mode {
        Beep,
        Nostalgia
    }

    override fun getHudInfo() = mode.settingName

    init {
        safeListener<EntityDeathEvent> { event ->
            if (event.entity !is EntityPlayer && onlyPlayers) return@safeListener
            if (event.entity.entityId == player.entityId) return@safeListener
            if (event.entity.getDistance(player) > 10.0) return@safeListener

            val sounds = when (mode) {
                Mode.Beep -> listOf(
                    "beep1.ogg",
                    "beep2.ogg",
                    "beep3.ogg",
                    "beep4.ogg"
                )
                Mode.Nostalgia -> listOf(
                    "nostalgia1.wav",
                    "nostalgia2.wav"
                )
            }

            SoundUtils.playSound(volumeSetting) { "death/${ sounds.random() }" }
        }
    }

    /*private class SoundImplementation(val name: String): ISound {
        private val location = ResourceLocation("curseclient", "sounds/death/$name")

        override fun getSoundLocation() = location
        override fun createAccessor(handler: SoundHandler) = SoundEventAccessor(location, name)
        override fun getSound() = Sound(name, 100000f, 1f, 1, Sound.Type.SOUND_EVENT, false)
        override fun getCategory() = SoundCategory.MASTER
        override fun canRepeat() = false
        override fun getRepeatDelay() = 0
        override fun getVolume() = volumeSetting.toFloat()
        override fun getPitch() = (Random.nextFloat() * 0.1f) + 0.95f
        override fun getXPosF() = 1f
        override fun getYPosF() = 0f
        override fun getZPosF() = 0f
        override fun getAttenuationType() = ISound.AttenuationType.LINEAR
    }*/
}