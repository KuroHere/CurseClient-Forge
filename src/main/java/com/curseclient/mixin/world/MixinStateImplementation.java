package com.curseclient.mixin.world;

import com.curseclient.client.event.EventBus;
import com.curseclient.client.event.events.CollisionEvent;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BlockStateContainer.StateImplementation.class)
public class MixinStateImplementation {
    @Shadow @Final private Block block;

    @Inject(method = "addCollisionBoxToList", at = @At("HEAD"))
    public void addCollisionBoxToList(World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState, CallbackInfo ci) {
        CollisionEvent event = new CollisionEvent(pos, block, entityIn, collidingBoxes);
        EventBus.INSTANCE.post(event);
    }
}
