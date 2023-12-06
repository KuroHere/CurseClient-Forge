package com.curseclient.client.module.modules.misc

import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.utility.misc.SoundUtils.playSound
import net.minecraft.client.gui.GuiDisconnected
import net.minecraftforge.client.event.GuiOpenEvent


object KickSound: Module(
    "KickSound",
    "Play sound when get kicked",
    Category.MISC
) {

    init {
        safeListener<GuiOpenEvent> { event ->
            if (event.gui is GuiDisconnected)
                playSound(1.0) { "kick.wav" }
        }
    }
}