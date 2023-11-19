package com.curseclient.client.event.events

import com.curseclient.client.event.Event
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos

class CollisionEvent(
    val pos: BlockPos,
    val block: Block,
    val entity: Entity?,
    val collidingBoxes: MutableList<AxisAlignedBB>
    ) : Event

class EntityDeathEvent(val entity: EntityLivingBase): Event

class TotemPopEvent(val player: EntityPlayer): Event
