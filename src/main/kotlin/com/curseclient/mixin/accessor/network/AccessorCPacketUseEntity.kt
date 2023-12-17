package com.curseclient.mixin.accessor.network

import net.minecraft.network.play.client.CPacketUseEntity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(CPacketUseEntity::class)
interface AccessorCPacketUseEntity {
    @get:Accessor("entityId")
    @set:Accessor("entityId")
    var id : Int

    @get:Accessor("action")
    @set:Accessor("action")
    var action : CPacketUseEntity.Action
}