package com.curseclient.mixin.accessor

import net.minecraft.client.renderer.entity.RenderManager
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(RenderManager::class)
interface AccessorRenderManager {
    @get:Accessor("renderPosX")
    val renderPosX : Double

    @get:Accessor("renderPosY")
    val renderPosY : Double

    @get:Accessor("renderPosZ")
    val renderPosZ : Double

    @get:Accessor("renderOutlines")
    val renderOutlines : Boolean
}