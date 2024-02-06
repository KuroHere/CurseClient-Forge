package com.curseclient.client.module.impls.combat

import com.curseclient.client.event.events.ConnectionEvent
import com.curseclient.client.event.events.TotemPopEvent
import com.curseclient.client.event.listener.listener
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.managers.FriendManager.isFriend
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.player.ChatUtils.sendMessage
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.gameevent.TickEvent

object TotemPopCounter: Module(
    "TotemPopCounter",
    "Counts popped totems",
    Category.COMBAT
) {
    private val countFriends by setting("Count Friends", true)
    private val countSelf by setting("Count Self", true)

    private val popped = HashMap<EntityPlayer, Int>()

    init {
        safeListener<TotemPopEvent> {
            val isFriend = it.player.isFriend()
            val isSelf = it.player == player

            if (!countFriends && isFriend) return@safeListener
            if (!countSelf && isSelf) return@safeListener

            val count = (popped[it.player] ?: 0) + 1
            popped[it.player] = count

            sendMessage("${it.player.name} popped ${formatCount(count)}!")
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@safeListener

            if (!player.isEntityAlive) {
                popped.clear()
                return@safeListener
            }

            popped.entries.removeIf { (diedPlayer, popped) ->
                if (diedPlayer == player || diedPlayer.isEntityAlive) return@removeIf false

                sendMessage("${diedPlayer.name} died after popping ${formatCount(popped)}!")
                return@removeIf true
            }
        }

        listener<ConnectionEvent.Connect> {
            popped.clear()
        }

        listener<ConnectionEvent.Disconnect> {
            popped.clear()
        }
    }

    override fun onEnable() {
        popped.clear()
    }

    override fun onDisable() {
        popped.clear()
    }

    private fun formatCount(count: Int): String {
        return "$count " + if (count > 1) "totems" else "totem"
    }
}