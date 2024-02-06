package com.curseclient.client.module.impls.hud

import com.curseclient.client.event.events.CurseClientEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.module.impls.client.ClickGui
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.misc.NotificationInfo
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.player.ChatUtils
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.Screen
import com.curseclient.client.utility.render.StencilUtil
import com.curseclient.client.utility.render.animation.ease.NewEaseType
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.shader.gradient.GradientUtil
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.render.shader.RoundedUtil.endBlend
import com.curseclient.client.utility.render.shader.RoundedUtil.setAlphaLimit
import com.curseclient.client.utility.render.shader.RoundedUtil.startBlend
import com.curseclient.client.utility.render.shader.blur.KawaseBloom
import net.minecraft.client.renderer.GlStateManager.*
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.max


object Notifications : DraggableHudModule(
    "Notifications",
    "Shows notifications in hud/chat",
    HudCategory.HUD
) {
    private val mode by setting("Mode", Mode.Classic)
    private val alpha by setting("Alpha", 255, 0, 255, 1, visible = { mode == Mode.Classic})

    private val widthSetting by setting("Width", 110.0, 80.0, 150.0, 1.0)
    private val limitNotification by setting("MaxNotifyList", 3.0, 3.0, 10.0, 1.0)

    val notificationList = ArrayList<Notification>()

    private enum class Mode {
        Classic,
        Chat,
    }

    private const val keepTime = 2000L
    private const val showTime = 300L
    private const val hideTime = 500L

    init {
        safeListener<CurseClientEvent.NotificationEvent> { event ->
            if (mode == Mode.Chat) {
                ChatUtils.sendMessage("${event.notification.text} ${event.notification.description}")
                return@safeListener
            }

            Notification(event.notification, System.currentTimeMillis()).spawn()
        }
    }

    override fun onRender() {
        notificationList.removeIf { it.shouldRemove() }
        while (notificationList.size > limitNotification) {
            notificationList.removeAt(0)
        }
        var y = pos.y
        notificationList.forEach {
            it.draw(Vec2d(pos.x, y))
            y -= it.getProgress() * (getHeight() + 5.0)
        }
    }

    class Notification(
        val info: NotificationInfo,
        private val spawnTime: Long
    ) {
        private val timeExisted get() = System.currentTimeMillis() - spawnTime

        fun spawn() {
            notificationList.add(this)
        }

        fun draw(position: Vec2d) {
            val c1 = if (ClickGui.colorMode == ClickGui.ColorMode.Client)
                HUD.getColor(0)
            else if (ClickGui.pulse) ColorUtils.pulseColor(ClickGui.buttonColor1, 0, 1) else ClickGui.buttonColor1

            val c2 = when (ClickGui.colorMode) {
                ClickGui.ColorMode.Client -> HUD.getColor(5)
                ClickGui.ColorMode.Static -> if (ClickGui.pulse) ColorUtils.pulseColor(ClickGui.buttonColor1, 0, 1) else ClickGui.buttonColor1
                else -> ClickGui.buttonColor2
            }

            val xShift = max(0.0, getProgress() * 10.0 - 10.0) * 10.0
            val pos1 = Vec2d(lerp(Screen.scaledWidth, position.x, getProgress()) - xShift, position.y)
            val pos2 = pos1.plus(getWidth() + info.description.length / 1.5, getHeight())

            when(mode) {
                Mode.Classic -> {
                    KawaseBloom.glBloom({
                        RectBuilder(pos1, pos2).apply {
                            color(Color(25, 25, 25, 255))
                            radius(3.0)
                            draw()
                        }
                    }, 2, 2)
                    RectBuilder(pos1, pos2).apply {
                        color(Color(25, 25, 25, alpha.toInt()))
                        radius(3.0)
                        draw()
                    }
                    textAndDetails(pos1, pos2, c1, c2)
                }
                Mode.Chat -> {}
            }
        }

        private fun textAndDetails(
            pos1: Vec2d,
            pos2: Vec2d,
            c1: Color,
            c2: Color
        ) {
            RectBuilder(pos1.minus(0.5, 0.0), pos1.plus(5.0, getHeight())).apply {
                color(c1, c1, c2, c2)
                radius(3.5)
                draw()
            }
            RectBuilder(pos1.plus(2.5, 0.0), pos1.plus(5.5, getHeight())).apply {
                color(c1, c1, c2, c2)
                draw()
            }
            val y = lerp(pos1.y, pos2.y, 0.5)
            val space = 10.0

            resetColor()
            GradientUtil.applyGradientHorizontal((pos1.x + space).toFloat(), (y - 5).toFloat(), Fonts.DEFAULT_BOLD.getStringWidth(info.text, 1.3).toFloat(), Fonts.DEFAULT_BOLD.getHeight(1.3).toFloat(), 1f, c1, c2) {
                setAlphaLimit(0f)
                Fonts.DEFAULT_BOLD.drawString(info.text, Vec2d(pos1.x + space, y - 5), color = Color.WHITE, shadow = false, scale = 1.3)
            }
            Fonts.DEFAULT.drawString(info.description, Vec2d(pos1.x + space, y + 8), color = info.descriptionColor)
        }

        fun getProgress(): Double {
            var p = 0.0

            if (timeExisted <= showTime) p = (timeExisted.toDouble() / showTime.toDouble())
            if (timeExisted > showTime) p = 1.0
            if (timeExisted > showTime + keepTime) p = 1.0 - ((timeExisted - showTime - keepTime).toDouble() / hideTime.toDouble())

            return NewEaseType.OutBack.getValue(clamp(p, 0.0, 1.0))
        }

        fun shouldRemove() =
            timeExisted > showTime + keepTime + hideTime + 100L
    }


    override fun getWidth() = widthSetting
    override fun getHeight() = 30.0
}