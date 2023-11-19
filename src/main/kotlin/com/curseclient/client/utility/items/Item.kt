package com.curseclient.client.utility.items

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack

val Item.id get() = Item.getIdFromItem(this)

val Item.block: Block get() = Block.getBlockFromItem(this)

val ItemFood.foodValue get() = this.getHealAmount(ItemStack.EMPTY)

val ItemFood.saturation get() = foodValue * this.getSaturationModifier(ItemStack.EMPTY) * 2f