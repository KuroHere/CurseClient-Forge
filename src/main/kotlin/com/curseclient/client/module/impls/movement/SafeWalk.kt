package com.curseclient.client.module.impls.movement

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.utility.Wrapper

object SafeWalk: Module(
    "SafeWalk",
    "Keeps you on the edge of the block",
    Category.MOVEMENT
) {
    @JvmStatic
    fun shouldSafewalk(entityID: Int) =
        (Wrapper.player?.let { !it.isSneaking && it.entityId == entityID } ?: false)
            && (isEnabled() || (Scaffold.isEnabled() && Scaffold.safeWalk))

    @JvmStatic
    fun setSneaking(state: Boolean) {
        Wrapper.player?.movementInput?.sneak = state
    }
}