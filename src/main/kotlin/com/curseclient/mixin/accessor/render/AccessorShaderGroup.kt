package com.curseclient.mixin.accessor.render

import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.Shader
import net.minecraft.client.shader.ShaderGroup
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(ShaderGroup::class)
interface AccessorShaderGroup {
    @get:Accessor("listShaders")
    val listShaders : List<Shader>

    @get:Accessor("mainFramebuffer")
    val mainFramebuffer : Framebuffer
}