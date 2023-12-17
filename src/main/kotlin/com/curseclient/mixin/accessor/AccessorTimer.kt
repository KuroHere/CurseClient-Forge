package com.curseclient.mixin.accessor

import net.minecraft.util.Timer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(Timer::class)
interface AccessorTimer {
    @get:Accessor("tickLength")
    @set:Accessor("tickLength")
    var tickLength : Float
}