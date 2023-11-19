package com.curseclient.client.utility.extension.mixins

import com.curseclient.mixin.accessor.AccessorRenderManager
import com.curseclient.mixin.accessor.render.AccessorItemRenderer
import net.minecraft.client.renderer.ItemRenderer
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.item.ItemStack

val RenderManager.renderPosX: Double get() = (this as AccessorRenderManager).renderPosX
val RenderManager.renderPosY: Double get() = (this as AccessorRenderManager).renderPosY
val RenderManager.renderPosZ: Double get() = (this as AccessorRenderManager).renderPosZ

var ItemRenderer.equippedProgressMainHand: Float
    get() = (this as AccessorItemRenderer).equippedProgressMainHand
    set(value) {
        (this as AccessorItemRenderer).equippedProgressMainHand = value
    }
var ItemRenderer.prevEquippedProgressMainHand: Float
    get() = (this as AccessorItemRenderer).prevEquippedProgressMainHand
    set(value) {
        (this as AccessorItemRenderer).prevEquippedProgressMainHand = value
    }

var ItemRenderer.equippedProgressOffHand: Float
    get() = (this as AccessorItemRenderer).equippedProgressOffHand
    set(value) {
        (this as AccessorItemRenderer).equippedProgressOffHand = value
    }
var ItemRenderer.prevEquippedProgressOffHand: Float
    get() = (this as AccessorItemRenderer).prevEquippedProgressOffHand
    set(value) {
        (this as AccessorItemRenderer).prevEquippedProgressOffHand = value
    }

var ItemRenderer.itemStackMainHand: ItemStack
    get() = (this as AccessorItemRenderer).itemStackMainHand
    set(item) {
        (this as AccessorItemRenderer).itemStackMainHand = item
    }

var ItemRenderer.itemStackOffHand: ItemStack
    get() = (this as AccessorItemRenderer).itemStackOffHand
    set(item) {
        (this as AccessorItemRenderer).itemStackOffHand = item
    }