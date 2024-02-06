package com.curseclient.client.module.impls.player

import com.curseclient.client.event.events.ConnectionEvent
import com.curseclient.client.event.events.PacketEvent
import com.curseclient.client.event.listener.listener
import com.curseclient.client.event.listener.runSafe
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.mojang.authlib.GameProfile
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.init.Items
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.world.GameType
import java.util.*
import kotlin.collections.ArrayList

object Blink : Module(
    "Blink",
    "Holds player packets",
    Category.PLAYER
) {
    private val packets = ArrayList<CPacketPlayer>()
    private var fakePlayer: EntityOtherPlayerMP? = null

    init {
        safeListener<PacketEvent.Send> {
            if (it.packet !is CPacketPlayer) return@safeListener
            it.cancel()
            packets.add(it.packet)
        }

        listener<ConnectionEvent.Connect> { packets.clear() }
        listener<ConnectionEvent.Disconnect> { packets.clear() }
    }

    override fun onEnable() {
        runSafe {
            val gameProfile = GameProfile(UUID.randomUUID(), mc.player.name)

            fakePlayer = EntityOtherPlayerMP(world, gameProfile)

            fakePlayer?.let { fp ->
                fp.entityId = -1882
                fp.copyLocationAndAnglesFrom(player)
                fp.rotationYawHead = player.rotationYawHead
                fp.setGameType(GameType.CREATIVE)
                fp.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack(Items.TOTEM_OF_UNDYING))

                world.addEntityToWorld(fp.entityId, fp)
            }
        }
        packets.clear()
    }

    override fun onDisable() {
        runSafe {
            fakePlayer?.let { world.removeEntityFromWorld(it.entityId) }
            packets.forEach { connection.sendPacket(it) }
        }
        packets.clear()
    }
}