package com.curseclient.client.module.modules.hud

import com.curseclient.client.event.listener.runSafeR
import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.FPSCounter
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.threads.loop.DelayedLoopThread
import java.awt.Color

object FPS : DraggableHudModule(
    "FPS",
    "Shows your fps",
    HudCategory.HUD
) {
    private val size by setting("Size", 1.0, 0.5, 3.0, 0.05)
    private val averageTime by setting("Average time", 500.0, 100.0, 5000.0, 100.0)

    private const val margin = 4.0
    private const val text1 = "FPS: "
    private var text2 = "0"

    private var fpsList = ArrayList<Pair<Double, Long>>()

    private val tickThread = DelayedLoopThread("FPS Counter Thread", { isEnabled() }, { 25L }) {
        runSafeR {
            val fps = 1.0 / FPSCounter.deltaTime
            fpsList.add(fps to System.currentTimeMillis())
            fpsList.removeIf { System.currentTimeMillis() - it.second > averageTime }

            val sum = fpsList.sumOf { it.first }
            val count = fpsList.count()

            if (count != 0) text2 = (sum / count.toDouble()).toInt().toString()
        } ?: run {
            try {
                fpsList.clear()
                Thread.sleep(1000L)
            } catch (_: InterruptedException) { }
        }
    }

    init {
        tickThread.reload()
    }

    override fun onEnable() {
        tickThread.interrupt()
    }

    override fun onRender() {
        val c1 =  HUD.getColor(0)
        val c2 = Color(230, 230, 230)

        Fonts.DEFAULT.drawString(text1, Vec2d(pos.x + margin * size / 2.0, pos.y + getHeight() / 2.0), true, c1, size)
        Fonts.DEFAULT.drawString(text2, Vec2d(pos.x + margin * size / 2.0 + Fonts.DEFAULT.getStringWidth(text1, size), pos.y + getHeight() / 2.0), true, c2, size)
    }

    override fun getWidth() = Fonts.DEFAULT.getStringWidth(text1, size) + Fonts.DEFAULT.getStringWidth(text2, size) + margin * size
    override fun getHeight() = Fonts.DEFAULT.getHeight(size) + margin * size
}