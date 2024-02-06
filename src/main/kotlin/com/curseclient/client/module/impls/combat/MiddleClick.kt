package com.curseclient.client.module.impls.combat

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.PlayerHotbarSlotEvent
import com.curseclient.client.event.events.PlayerPacketEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.managers.HotbarManager
import com.curseclient.client.manager.managers.HotbarManager.sendSlotPacket
import com.curseclient.client.manager.managers.PacketManager
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.settingName
import com.curseclient.client.utility.items.firstItem
import com.curseclient.client.utility.items.hotbarSlots
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.EnumHand
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Mouse

object MiddleClick : Module(
    "MiddleClick",
    "Uses items by middle-clicking",
    Category.COMBAT
) {
    private val mode by setting("Mode", Mode.Pearl)
    private val spam by setting("Spam", false)

    private enum class Mode {
        Pearl,
        Exp
    }

    override fun getHudInfo() = mode.settingName

    init {
        safeListener<InputEvent.MouseInputEvent> {
            if (Mouse.getEventButton() != 2 || !Mouse.getEventButtonState() || mode != Mode.Pearl) return@safeListener
            val slot = player.hotbarSlots.firstItem(Items.ENDER_PEARL) ?: return@safeListener

            sendSlotPacket(slot.hotbarSlot)
            val packet = CPacketPlayerTryUseItem(EnumHand.MAIN_HAND)
            connection.sendPacket(packet)
            sendSlotPacket(player.inventory.currentItem)
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (checkExp() && (player.ticksExisted % 2 == 0 || spam)) throwExp()
        }

        safeListener<PlayerHotbarSlotEvent>(-199) { event ->
            if (!checkExp()) return@safeListener
            val slot = player.hotbarSlots.firstItem(Items.EXPERIENCE_BOTTLE) ?: return@safeListener
            event.slot = slot.hotbarSlot
        }

        safeListener<PlayerPacketEvent.Data>(-199){
            if (!checkExp()) return@safeListener
            player.hotbarSlots.firstItem(Items.EXPERIENCE_BOTTLE) ?: return@safeListener
            it.pitch = 89.6969f
        }
    }

    private fun checkExp(): Boolean =
        Mouse.isButtonDown(2) && mode == Mode.Exp

    private fun SafeClientEvent.throwExp() {
        val slot = player.hotbarSlots.firstItem(Items.EXPERIENCE_BOTTLE) ?: return
        if (HotbarManager.lastReportedSlot != slot.hotbarSlot) return
        if (PacketManager.lastReportedPitch < 85) return

        playerController.processRightClick(player, world, EnumHand.MAIN_HAND)
    }
}