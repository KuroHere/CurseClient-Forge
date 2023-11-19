package com.curseclient.client.module.modules.movement

import com.curseclient.client.event.listener.runSafe
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module

object KeepSprint: Module(
    "KeepSprint",
    "Keeps your sprint state after attacking an entity",
    Category.MOVEMENT
) {
    private var prev: Triple<Boolean, Double, Double>? = null // sprinting, motionX, motionY

    @JvmStatic
    fun onHitPre() {
        if (!isEnabled()) return
        runSafe {
            prev = Triple(player.isSprinting, player.motionX, player.motionZ)
        }
    }

    @JvmStatic
    fun onHitPost() {
        if (!isEnabled()) return
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