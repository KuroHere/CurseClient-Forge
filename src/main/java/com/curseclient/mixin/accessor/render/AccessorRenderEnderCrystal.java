package com.curseclient.mixin.accessor.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderEnderCrystal;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderEnderCrystal.class)
public interface AccessorRenderEnderCrystal {
    @Accessor("ENDER_CRYSTAL_TEXTURES")
    ResourceLocation getTextures();

    @Accessor("modelEnderCrystalNoBase")
    ModelBase getModel();
}
