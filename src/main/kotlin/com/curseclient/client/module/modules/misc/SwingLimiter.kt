package com.curseclient.client.module.modules.misc

import com.curseclient.client.event.events.PacketEvent
import com.curseclient.client.event.listener.Nameable
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.util.EnumHand

object SwingLimiter: Module(
    "SwingLimiter",
    "Limits outgoing animation packets",
    Category.MISC
) {
    private val hand by setting("Hand", Hand.BOTH)
    private val cancelDelay by setting("Cancel Delay", 40.0, 5.0, 100.0, 1.0)

    private var lastPacket = 0L

    private enum class Hand(override val displayName: String): Nameable {
        BOTH("Both"),
        MAIN_HAND("Main Hand"),
        OFF_HAND("Off Hand")
    }

    init {
        safeListener<PacketEvent.Send>(-1000) { event ->
            if (event.packet !is CPacketAnimation) return@safeListener

            when(event.packet.hand) {
                EnumHand.MAIN_HAND -> if (hand != Hand.MAIN_HAND && hand != Hand.BOTH) return@safeListener
                EnumHand.OFF_HAND -> if (hand != Hand.OFF_HAND && hand != Hand.BOTH) return@safeListener
                null -> return@safeListener
            }

            val time = System.currentTimeMillis()

            if (time - lastPacket < cancelDelay) {
                event.cancel()
            } else {
                lastPacket = time
            }
        }
    }
}