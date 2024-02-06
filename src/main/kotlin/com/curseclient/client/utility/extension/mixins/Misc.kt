package com.curseclient.client.utility.extension.mixins

import com.curseclient.mixin.accessor.AccessorMinecraft
import com.curseclient.mixin.accessor.AccessorTimer
import com.curseclient.mixin.accessor.client.AccessorKeyBinding
import com.curseclient.mixin.accessor.entity.AccessorEntityLivingBase
import com.curseclient.mixin.accessor.entity.AccessorEntityPlayer
import com.curseclient.mixin.accessor.entity.AccessorPlayerControllerMP
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.PlayerControllerMP
import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.Session
import net.minecraft.util.Timer

var Timer.tickLength: Float
    get() = (this as AccessorTimer).tickLength
    set(value) {
        (this as AccessorTimer).tickLength = value
    }
val Minecraft.timer: Timer get() = (this as AccessorMinecraft).timer

var Minecraft.rightClickDelayTimer: Int
    get() = (this as AccessorMinecraft).rightClickDelayTimer
    set(value) {
        (this as AccessorMinecraft).rightClickDelayTimer = value
    }

var EntityLivingBase.jumpTicks: Int
    get() = (this as AccessorEntityLivingBase).jumpTicks
    set(value) {
        (this as AccessorEntityLivingBase).jumpTicks = value
    }

var EntityPlayer.speedInAir: Float
    get() = (this as AccessorEntityPlayer).speedInAir
    set(value) {
        (this as AccessorEntityPlayer).speedInAir = value
    }

var KeyBinding.pressed: Boolean
    get() = (this as AccessorKeyBinding).pressed
    set(value) {
        (this as AccessorKeyBinding).pressed = value
    }

var PlayerControllerMP.breakProgress: Double
    get() = (this as AccessorPlayerControllerMP).curBlockDamageMP.toDouble()
    set(value) {
        (this as AccessorPlayerControllerMP).curBlockDamageMP = value.toFloat()
    }