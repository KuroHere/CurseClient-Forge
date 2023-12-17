package com.curseclient.mixin.accessor.entity

import net.minecraft.client.multiplayer.PlayerControllerMP
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(PlayerControllerMP::class)
interface AccessorPlayerControllerMP {
    @get:Accessor("curBlockDamageMP")
    @set:Accessor("curBlockDamageMP")
    var curBlockDamageMP : Float
}