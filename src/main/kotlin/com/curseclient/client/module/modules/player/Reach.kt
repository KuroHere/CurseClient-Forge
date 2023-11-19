package com.curseclient.client.module.modules.player

import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.gameevent.TickEvent

object Reach : Module(
    "Reach",
    "Increases reach distance",
    Category.PLAYER
) {

    private val amount by setting("Amount", 0.5, 0.1, 5.0, 0.1)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            mc.player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).removeModifier(mc.player.uniqueID)
            mc.player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).applyModifier(AttributeModifier(mc.player.uniqueID, "custom_reach", amount, 1))
        }
    }


    override fun onDisable() {
        mc.player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).removeModifier(mc.player.uniqueID)
    }
}