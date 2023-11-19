package com.curseclient.client.module.modules.combat

import com.curseclient.client.event.events.ConnectionEvent
import com.curseclient.client.event.listener.listener
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.misc.FakePlayer
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.settingName
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.nio.charset.StandardCharsets
import java.util.*

object AntiBot: Module(
    "AntiBot",
    "Removes bots created by anti cheat",
    Category.COMBAT
) {
    private val mode by setting("Mode", Mode.Ignore)

    private val botList: ArrayList<Entity> = ArrayList()

    private enum class Mode {
        Ignore,
        Remove
    }

    override fun getHudInfo() = mode.settingName

    init {
        safeListener<ClientTickEvent> { event ->
            if (event.phase != TickEvent.Phase.START) return@safeListener

            world.playerEntities.forEach { entity ->
                if (player.getDistance(entity) > 10.0) return@forEach
                if (entity.uniqueID == UUID.nameUUIDFromBytes(("OfflinePlayer:" + entity.name).toByteArray(StandardCharsets.UTF_8)) || entity !is EntityOtherPlayerMP) return@forEach
                botList.add(entity)
            }

            if (mode == Mode.Remove) botList.forEach {
                if (FakePlayer.isEnabled())
                    FakePlayer.fakePlayer?.let { fp ->
                        if (it == fp) return@forEach
                    }

                world.removeEntity(it)
            }
        }

        listener<ConnectionEvent.Connect> {
            botList.clear()
        }

        listener<ConnectionEvent.Disconnect> {
            botList.clear()
        }
    }

    fun Entity.isBot(): Boolean{
        if (!isEnabled()) return false
        if (mode != Mode.Ignore) return false
        if (FakePlayer.isEnabled())
            FakePlayer.fakePlayer?.let {
                if (this == it) return false
            }

        return botList.contains(this)
    }
}