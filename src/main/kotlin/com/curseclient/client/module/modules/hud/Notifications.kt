package com.curseclient.client.module.modules.hud

import com.curseclient.client.event.events.CurseClientEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.NotificationInfo
import com.curseclient.client.utility.SoundUtils
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.player.ChatUtils
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.Screen
import com.curseclient.client.utility.render.animation.NewEaseType
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.shader.GradientUtil
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.render.shader.RoundedUtil
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color
import kotlin.math.max


object Notifications : DraggableHudModule(
    "Notifications",
    "Shows notifications in hud/chat",
    HudCategory.HUD
) {
    private val mode by setting("Mode", Mode.Transparent)
    private val widthSetting by setting("Width", 110.0, 80.0, 150.0, 1.0)
    private val limitNotification by setting("MaxNotifyList", 3.0, 3.0, 10.0, 1.0)
    private val sound by setting("Sound", true)
    private val volume by setting("Volume", 0.5, 0.1, 2.0, 0.1, visible = { sound })

    val notificationList = ArrayList<Notification>()

    private enum class Mode {
        Classic,
        Transparent,
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
        // I'm too lazy to remake it😼
        while (notificationList.size > limitNotification) {
            notificationList.removeAt(0)
        }
        var y = pos.y
        notificationList.forEach {
            it.draw(Vec2d(pos.x, y))
            y -= it.getProgress() * (getHeight() + 5.0)
        }
    }

    class Notification(val info: NotificationInfo, private val spawnTime: Long) {
        private val timeExisted get() = System.currentTimeMillis() - spawnTime

        fun spawn() {
            notificationList.add(this)
            if (sound)
                SoundUtils.playSound(volume) { "pop.wav" }
        }

        fun draw(position: Vec2d) {

            val c1 = HUD.getColor(0)
            val c2 = HUD.getColor(10)
            val xShift = max(0.0, getProgress() * 10.0 - 10.0) * 10.0
            val pos1 = Vec2d(lerp(Screen.scaledWidth, position.x, getProgress()) - xShift, position.y + 20)
            val pos2 = pos1.plus(getWidth() + info.description.length / 1.5, getHeight())

            val x:Float = (lerp(Screen.scaledWidth, position.x, getProgress()) - xShift).toFloat()
            val y2:Float = (position.y + 20).toFloat()

            when(mode) {
                Mode.Classic -> {
                    RenderUtils2D.drawBlurredShadow(
                        x,
                        y2,
                        (getWidth() + info.description.length / 1.5).toFloat(),
                        getHeight().toFloat(),
                        5,
                        Color(25, 25, 25)
                    )

                    RectBuilder(pos1, pos2).color(Color(25, 25, 25)).radius(3.0).draw()
                    RenderUtils2D.drawBlurredShadow(
                        x,
                        y2.minus(0.5).toFloat(),
                        7f,
                        getHeight().toFloat(),
                        5,
                        c1
                    )
                    RectBuilder(pos1.minus(0.5), pos1.plus(5.0, getHeight())).color(c1, c1, c2, c2).radius(3.5).draw()

                    val y = lerp(pos1.y, pos2.y, 0.5)
                    val space = 10.0
                    GlStateManager.resetColor()
                    GradientUtil.applyGradientCornerLR(pos1.x.toFloat(), (y - 5).toFloat(), (pos1.x.toFloat() + Fonts.DEFAULT_BOLD.getStringWidth(info.text, 1.3)).toFloat(), (y + Fonts.DEFAULT_BOLD.getHeight(1.3)).toFloat(), 1f, c1, c2) {
                        RoundedUtil.setAlphaLimit(0f)
                        Fonts.DEFAULT_BOLD.drawString(info.text, Vec2d(pos1.x + space, y - 5), color = Color.WHITE, shadow = false, scale = 1.3)
                    }
                    Fonts.DEFAULT.drawString(info.description, Vec2d(pos1.x + space, y + 8), color = info.descriptionColor)

                }
                Mode.Transparent -> {
                    RenderUtils2D.drawBlurredShadow(
                        x,
                        y2,
                        (getWidth() + info.description.length / 1.5).toFloat(),
                        getHeight().toFloat(),
                        5,
                        Color(0, 0,  0, 50)
                    )

                    RectBuilder(pos1, pos2).color(Color(0, 0,  0, 80)).radius(5.0).draw()
                    RenderUtils2D.drawBlurredShadow(
                        x,
                        y2.minus(0.5).toFloat(),
                        7f,
                        getHeight().toFloat(),
                        5,
                        c1
                    )
                    RectBuilder(pos1.minus(0.5), pos1.plus(5.0, getHeight())).color(c1, c1, c2, c2).radius(3.5).draw()

                    val y = lerp(pos1.y, pos2.y, 0.5)
                    val space = 10.0

                    GlStateManager.resetColor()
                    GradientUtil.applyGradientCornerLR(pos1.x.toFloat(), (y - 5).toFloat(), (pos1.x.toFloat() + Fonts.DEFAULT_BOLD.getStringWidth(info.text, 1.3)).toFloat(), (y + Fonts.DEFAULT_BOLD.getHeight(1.3)).toFloat(), 1f, c1, c2) {
                        RoundedUtil.setAlphaLimit(0f)
                        Fonts.DEFAULT_BOLD.drawString(info.text, Vec2d(pos1.x + space, y - 5), color = Color.WHITE, shadow = false, scale = 1.3)
                    }
                    Fonts.DEFAULT.drawString(info.description, Vec2d(pos1.x + space, y + 8), color = info.descriptionColor)
                }

                Mode.Chat -> {}
            }
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