package com.curseclient.client.manager.managers

import com.curseclient.client.event.EventBus
import com.curseclient.client.event.events.TimerEvent
import com.curseclient.client.event.listener.listener
import com.curseclient.client.event.listener.runSafe
import com.curseclient.client.manager.Manager
import com.curseclient.client.utility.extension.mixins.tickLength
import com.curseclient.client.utility.extension.mixins.timer
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.gameevent.TickEvent

object TimerManager : Manager("TimerManager") {
    init {
        listener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@listener
            val timer = Minecraft.getMinecraft().timer

            TimerEvent().apply {
                runSafe {
                    EventBus.post(this@apply)
                }

                timer.tickLength = 50.0f / speed.toFloat()
            }
        }
    }

    val timerSpeed get() = 50.0 / Minecraft.getMinecraft().timer.tickLength
}