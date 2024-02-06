package com.curseclient.mixin.world;

import com.curseclient.client.module.impls.visual.WallHack;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockLiquid.class)
public abstract class MixinBlockLiquid extends MixinBlock {

    @Inject(method = "shouldSideBeRendered", at = @At(value = "HEAD"), cancellable = true)
    private void onShouldSideBeRenderedPre(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        if (WallHack.INSTANCE.isEnabled()) {
            cir.setReturnValue(blockAccess.getBlockState(pos.offset(side)).getMaterial() != material);
        }
    }
}

