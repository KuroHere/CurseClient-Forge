package com.curseclient.client.module.modules.player

import com.curseclient.client.event.events.PlayerPacketEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module

object AntiHunger : Module(
    "AntiHunger",
    "Slows down the loss of hunger",
    Category.PLAYER
) {
    init {
        safeListener<PlayerPacketEvent.Misc> {
            it.isSprinting = false
        }
    }
}