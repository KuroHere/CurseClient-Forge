package com.curseclient.client.module.modules.player

import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.utility.extension.mixins.rightClickDelayTimer
import net.minecraftforge.fml.common.gameevent.TickEvent

object FastUse: Module(
    "FastUse",
    "Increases item use speed",
    Category.PLAYER
) {
    init {
        safeListener<TickEvent.ClientTickEvent> {
            mc.rightClickDelayTimer = 0
        }
    }

    override fun onDisable() {
        mc.rightClickDelayTimer = 6
    }
}