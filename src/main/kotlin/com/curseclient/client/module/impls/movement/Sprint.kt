package com.curseclient.client.module.impls.movement

import com.curseclient.client.event.events.MoveEvent
import com.curseclient.client.event.listener.runSafe
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.player.MovementUtils.isInputting
import org.lwjgl.input.Keyboard

object Sprint : Module(
    "Sprint",
    "Sprinting if possible",
    Category.MOVEMENT
) {

    private val keepSprint by setting("KeepSprint", false,
        description = "Keeps your sprint state after attacking an entity")

    /**
     * @param Boolean is sprinting
     * @param Double is motionX
     * @param Double is motionY
     */
    private var prev: Triple<Boolean, Double, Double>? = null

    @JvmStatic
    fun onHitPre() {
        if (!isEnabled()) return
        if (keepSprint) {
            runSafe {
                prev = Triple(player.isSprinting, player.motionX, player.motionZ)
            }
        }
    }

    @JvmStatic
    fun onHitPost() {
        if (!isEnabled()) return
        if (keepSprint) {
            runSafe {
                prev?.let {
                    player.apply {
                        isSprinting = it.first
                        motionX = it.second
                        motionZ = it.third
                    }
                }
            }
        }
    }

    init {
        safeListener<MoveEvent> {
            player.isSprinting = isInputting() && Keyboard.isKeyDown(mc.gameSettings.keyBindForward.keyCode) && !player.collidedHorizontally
        }
    }
}