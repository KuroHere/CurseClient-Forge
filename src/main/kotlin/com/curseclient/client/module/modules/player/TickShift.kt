package com.curseclient.client.module.modules.player

import com.curseclient.client.event.events.PacketEvent
import com.curseclient.client.event.events.TimerEvent
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.utility.player.MovementUtils.isInputting
import com.curseclient.client.utility.threads.loop.DelayedLoopThread
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketPlayerPosLook
import kotlin.math.max
import kotlin.math.min

object TickShift : Module(
    "TickShift",
    "Smart timer -_^",
    Category.PLAYER
) {
    private val speed by setting("Speed", 1.5, 1.0, 10.0, 0.05)
    private val maxTicks by setting("Max Ticks", 20.0, 20.0, 100.0, 1.0)
    private val nativeDelay by setting("Native Delay", 46.0, 45.0, 50.0, 1.0)

    private val thread = DelayedLoopThread("Tick Shift Thread", { isEnabled() && mc.world != null }, { nativeDelay.toLong() }) {
        ticks = min(ticks + 1.0, maxTicks)
        if (ticks >= maxTicks - 1) ready = true
    }

    private var ticks = 0.0
    private var ready = false

    override fun getHudInfo() = ticks.toInt().toString()

    init {
        thread.reload()

        safeListener<TimerEvent>(-50) {
            if (!ready || !isInputting()) return@safeListener
            it.speed = speed
        }

        safeListener<PacketEvent.PostSend> {
            if (it.packet !is CPacketPlayer) return@safeListener
            ticks = max(0.0, ticks - 1.0)
            if (ticks < 2) ready = false
        }

        safeListener<PacketEvent.PostReceive> {
            if (it.packet !is SPacketPlayerPosLook) return@safeListener
            ticks = 0.0
        }
    }

    override fun onEnable() {
        ready = false
        thread.interrupt()
        ticks = 0.0
    }
}