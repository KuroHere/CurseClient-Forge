package com.curseclient.client.module.impls.player

import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.mixins.pressed
import net.minecraft.item.ItemAppleGold
import net.minecraft.item.ItemFood
import net.minecraftforge.fml.common.gameevent.TickEvent

object AutoEat : Module(
    "AutoEat",
    "Automatically ate food",
    Category.PLAYER
) {
    private val health by setting("Health Threshold", 16.0, 1.0, 20.0, 1.0)
    //private val hunger by setting("Hunger Threshold", 0.0, 1.0, 20.0, 1.0)
    private val offhandOnly by setting("Offhand Only", false)
    private val goldenAppleOnly by setting("Golden Apple Only", false)
    val antiReset by setting("Anti Reset", true)
    var active = false; private set

    override fun onEnable() {
        active = false
    }

    override fun onDisable() {
        mc.gameSettings.keyBindUseItem.pressed = false
    }

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if(mc.player.health <= health && checkItem()){
                active = true
                mc.gameSettings.keyBindUseItem.pressed = true
            } else if(active){
                mc.gameSettings.keyBindUseItem.pressed = false
                active = false
            }
        }
    }

    private fun checkItem():Boolean {
        if (goldenAppleOnly){
            if (mc.player.heldItemOffhand.item is ItemAppleGold) return true
            if (mc.player.heldItemMainhand.item is ItemAppleGold && !offhandOnly) return true
        } else {
            if (mc.player.heldItemOffhand.item is ItemFood) return true
            if (mc.player.heldItemMainhand.item is ItemFood && !offhandOnly) return true
        }

        return false
    }
}