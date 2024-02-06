package com.curseclient.client.module.impls.movement

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.MoveEvent
import com.curseclient.client.event.listener.Nameable
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.managers.HotbarManager
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.settingName
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemPotion
import net.minecraft.item.ItemShield
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraftforge.client.event.InputUpdateEvent

object NoSlow : Module(
    "NoSlow",
    "Prevents being slowed down",
    Category.MOVEMENT
){
    private val mode by setting("Mode", Mode.Vanilla)
    private val speed by setting("Speed", 1.0, 0.0, 1.0, 0.01)

    private val food by setting("Food", true)
    private val bow by setting("Bow", true)
    private val potion by setting("Potion", true)
    private val shield by setting("Shield", true)
    private val sneak by setting("Sneak", true)

    private enum class Mode(override val displayName: String): Nameable {
        Vanilla("Vanilla"),
        Matrix1("Matrix 1"),
        Matrix2("Matrix 2"),
        Grim("Grim")
    }

    override fun getHudInfo() = mode.settingName

    init {
        safeListener<InputUpdateEvent> {
            if (!check()) return@safeListener

            it.movementInput.moveStrafe *= speed.toFloat() * 5f
            it.movementInput.moveForward *= speed.toFloat() * 5f
        }

        safeListener<MoveEvent> {
            if (!check()) return@safeListener

            when(mode) {
                Mode.Matrix1 -> {
                    if (player.onGround &&
                        !player.movementInput.jump &&
                        player.ticksExisted % 2 == 0
                    ) motion(0.35)

                    if (player.fallDistance > 0.2)
                        motion(0.91)
                }

                Mode.Matrix2 -> {
                    if (player.onGround &&
                        !player.movementInput.jump &&
                        player.ticksExisted % 2 == 0
                    ) motion(0.48)

                    if (player.fallDistance > 1.5)
                        motion(0.8)
                    else if (player.fallDistance > 0.7)
                        motion(0.91)
                    else if (player.fallDistance > 0.2)
                        motion(0.97)
                }

                Mode.Grim -> {
                    val slot = (0..8).firstOrNull { it != HotbarManager.lastReportedSlot }
                    slot?.let {
                        connection.sendPacket(CPacketHeldItemChange(it))
                        connection.sendPacket(CPacketHeldItemChange(HotbarManager.lastReportedSlot))
                    }
                }

                Mode.Vanilla -> {}
            }

        }
    }

    private fun SafeClientEvent.motion(modifier: Double) {
        player.motionX *= modifier
        player.motionZ *= modifier
    }

    private fun SafeClientEvent.check(): Boolean {
        val item = player.activeItemStack.item

        if (!player.isHandActive) return false
        if (player.isRiding) return false
        if (sneak && player.isSneaking) return true

        return food && item is ItemFood
            || bow && item is ItemBow
            || potion && item is ItemPotion
            || shield && item is ItemShield
    }
}

