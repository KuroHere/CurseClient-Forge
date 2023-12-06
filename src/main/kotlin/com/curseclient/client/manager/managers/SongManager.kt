package com.curseclient.client.manager.managers

import baritone.api.utils.Helper.mc

import com.curseclient.client.manager.Manager
import com.curseclient.client.utility.misc.Song
import net.minecraft.client.audio.ISound

object SongManager : Manager("SongManager") {

    private val songs: ISound = Song.sound
    val menuSong: ISound = this.songs
    var isPaused = false

    fun playOrPause() {
        if (!isCurrentSongPlaying()) {
            mc.soundHandler.playSound(menuSong)
        } else if (isPaused) {
            mc.soundHandler.resumeSounds() // Resume all sounds
            isPaused = false
        } else {
            mc.soundHandler.pauseSounds() // Pause all sounds
            isPaused = true
        }
    }

    fun skip() {
        stop()
        // Logic for skipping to the next song
    }


    fun stop() {
        if (isCurrentSongPlaying()) {
            mc.soundHandler.stopSound(menuSong)
            isPaused = false
        }
    }

    private fun isCurrentSongPlaying(): Boolean {
        return mc.soundHandler.isSoundPlaying(menuSong)
    }

}