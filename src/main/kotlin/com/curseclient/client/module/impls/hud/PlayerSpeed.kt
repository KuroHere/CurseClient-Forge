package com.curseclient.client.module.impls.hud

import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.MathUtils.roundToPlaces
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.shader.RectBuilder
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Color
import kotlin.math.hypot

object PlayerSpeed: DraggableHudModule(
    "PlayerSpeed",
    "Shows your speed",
    HudCategory.HUD
){
    private val size by setting("Size", 1.0, 0.5, 3.0, 0.05)
    private val background by setting("BackGround", Color(35, 35, 35, 50))
    private val radius by setting("Radius", 1.0, 0.0, 5.0, 0.1)

    private val averageTicks by setting("Average ticks", 5.0, 1.0, 50.0, 1.0)
    private val places by setting("Round Places", 1.0, 1.0, 5.0, 1.0)

    private var speedList = arrayListOf(0.0)

    private const val margin = 4.0

    override fun onRender() {
        val c1 = HUD.getColor(0)
        val c2 = Color(230, 230, 230)

        val totalWidth = margin * size / 2 + Fonts.DEFAULT.getStringWidth("${text1} ${text2}", size)
        val totalHeight = Fonts.DEFAULT.getHeight(size) + getHeight() / 3

        RectBuilder(pos, pos.plus(totalWidth, totalHeight)).apply {
            shadow(pos.x, pos.y, totalWidth, totalHeight, 5, background)
            color(background)
            radius(radius)
            draw()
        }

        Fonts.DEFAULT.drawString(text1, Vec2d(pos.x + margin * size / 2.0, pos.y + getHeight() / 2.0), true, c1, size)
        Fonts.DEFAULT.drawString(text2, Vec2d(pos.x + margin * size / 2.0 + Fonts.DEFAULT.getStringWidth(text1, size), pos.y + getHeight() / 2.0), true, c2, size)

    }

    override fun getWidth() = Fonts.DEFAULT.getStringWidth(text1, size) + Fonts.DEFAULT.getStringWidth(text2, size) + margin * size
    override fun getHeight() = Fonts.DEFAULT.getHeight(size) + margin * size

    private val text1 get() =
        (speedList.takeLast(averageTicks.toInt()).sum() / averageTicks).roundToPlaces(places.toInt()).toString()

    private const val text2 = " BPS"

    init {
        for (i in 0..50) {
            speedList.add(0.0)
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@safeListener

            val speed = hypot(player.posX - player.prevPosX, player.posZ - player.prevPosZ) * 20.0
            speedList.add(speed)
            speedList.dropWhile { speedList.count() > averageTicks }
        }
    }
}