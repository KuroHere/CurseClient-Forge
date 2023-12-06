package com.curseclient.client.utility.misc

import baritone.api.utils.Helper
import com.curseclient.client.Client
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.lwjgl.opengl.Display

class TitleUtils {
    private var ticks = 0
    private var bruh = 0
    private var breakTimer = 0
    private var title: String = "${Client.NAME} ${Client.VERSION} "
    private var qwerty = false

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        ticks++
        if (ticks % 17 == 0) {
            Display.setTitle("${title.substring(0, title.length - bruh)} | ${Helper.mc.session.username}")
            if ((bruh == title.length || bruh == 0) && breakTimer != 0) {
                breakTimer++
                return
            }
            breakTimer = 0
            if (bruh == title.length) {
                qwerty = true
            }
            bruh = if (qwerty) --bruh else ++bruh
            if (bruh == 0) {
                qwerty = false
            }
        }
    }
}