package com.curseclient.mixin.accessor

import net.minecraft.client.Minecraft
import net.minecraft.util.Session
import net.minecraft.util.Timer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(Minecraft::class)
interface AccessorMinecraft {
    @get:Accessor("timer")
    val timer : Timer

    @get:Accessor("session")
    @set:Accessor("session")
    var session : Session

    @get:Accessor("rightClickDelayTimer")
    @set:Accessor("rightClickDelayTimer")
    var rightClickDelayTimer : Int

    @get:Accessor("renderPartialTicksPaused")
    val partialTicksPaused : Float
}