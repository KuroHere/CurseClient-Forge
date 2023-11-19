package com.curseclient.client.module.modules.player

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import net.minecraft.item.ItemPickaxe

object NoEntityTrace : Module(
    "NoEntityTrace",
    "NoEntityTrace",
    Category.PLAYER
) {
    private val checkPickaxe by setting("Check Pickaxe", true)


    @JvmStatic
    fun isActive(): Boolean {
        if (!isEnabled()) return false

        val holdingPickAxe = mc.player?.heldItemMainhand?.item is ItemPickaxe

        return !checkPickaxe || holdingPickAxe
    }
}