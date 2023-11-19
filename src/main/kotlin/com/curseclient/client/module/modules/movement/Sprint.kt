package com.curseclient.client.module.modules.movement

import com.curseclient.client.event.events.MoveEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.utility.player.MovementUtils.isInputting
import org.lwjgl.input.Keyboard

object Sprint : Module(
    "Sprint",
    "Sprinting if possible",
    Category.MOVEMENT
) {

    init {
        safeListener<MoveEvent> {
            player.isSprinting = isInputting() && Keyboard.isKeyDown(mc.gameSettings.keyBindForward.keyCode) && !player.collidedHorizontally
        }
    }
}