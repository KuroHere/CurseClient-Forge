package com.curseclient.mixin.misc;

import com.curseclient.client.module.impls.misc.SolidWeb;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {BlockWeb.class})
public class MixinBlockWeb extends Block {
    protected MixinBlockWeb() {
        super(Material.WEB);
    }

    @Inject(method = {"getCollisionBoundingBox"}, at = {@At("HEAD")}, cancellable = true)
    public void getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos, CallbackInfoReturnable<AxisAlignedBB> cir) {
        if (SolidWeb.INSTANCE.isEnabled()) {
            cir.setReturnValue(FULL_BLOCK_AABB);
        }
    }
}
