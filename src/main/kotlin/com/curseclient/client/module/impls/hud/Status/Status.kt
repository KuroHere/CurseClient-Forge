package com.curseclient.client.module.impls.hud.Status

import baritone.api.utils.Helper
import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.animation.animaions.AstolfoAnimation
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.EntityLivingBase
import net.minecraft.stats.StatList
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// Shit design - ᓚᘏᗢ
object Status: DraggableHudModule(
    "Status",
    "Draw your ingame status kill, dead, playtime,...(Not done yet)",
    HudCategory.HUD
) {

    private var astolfo = AstolfoAnimation
    private val tatusWidth by setting("Width", 160, 150, 200, 1)
    private val circleWidth by setting("CircleWidth", 3.0, 0.0, 5.0, 0.1)
    private val colorType by setting("ColorType", Mode.State)
    private val circleColor by setting("CircleColor", Color.WHITE, { colorType == Mode.Custom })

    private var info = StatusInfo.SELF
    val sr: ScaledResolution = ScaledResolution(mc)

    private var scrollingTextPos = 0.0
    private const val serverText = "Server: "
    private var isScrolling = false

    var startTime = System.currentTimeMillis()
    var endTime:Long = -1
    var gamesPlayed = 0

    enum class Mode {
        State,
        Custom,
        Astolfo
    }

    override fun onRender() {
        super.onRender()

        val fr: Fonts = Fonts.DEFAULT_BOLD

        val pos1 = Vec2d(pos.x, pos.y)

        renderCard()

        info.entity?.let {
            info = StatusInfo(info.name, info.entity)
        }

        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        RenderUtils2D.glScissor(pos.x.toFloat() + 5, pos.y.toFloat() + 5, (pos.x + this.getWidth() - 5).toFloat(), (pos.y + this.getHeight() - 60).toFloat(), sr)

        renderPlayerHead()

        val textPos = Vec2d(pos1.x + 35, pos1.y + fr.getHeight() + 7)
        fr.drawString(info.name, textPos, true, Color.WHITE, 2.0)

        GL11.glDisable(GL11.GL_SCISSOR_TEST)

        GL11.glPushMatrix()
        drawCircle(pos1.x + getWidth() / 1.3, pos1.y + getHeight() / 1.5, 2.0, circleWidth.toFloat(), getProgress())
        GL11.glPopMatrix()

        renderPlayerInfo()
        renderOnlineTime()
        renderServerInfo()
    }

    private fun getProgress(): Double {
        val runtime = Runtime.getRuntime()
        //return (runtime.totalMemory() - runtime.freeMemory()) * 100L / runtime.maxMemory()
        val total = runtime.totalMemory()
        val free = runtime.freeMemory()
        val delta = total - free
        return delta / runtime.maxMemory().toDouble()
    }

    private fun renderCard() {
        val pos1 = Vec2d(pos.x, pos.y)
        val pos2 = Vec2d(pos.x + getWidth(), pos.y + getHeight())

        val c1 = HUD.getColor(0)
        val c2 = HUD.getColor(10)

        RenderUtils2D.drawBlurredShadow(
            pos.x.toFloat(),
            pos.y.toFloat(),
            getWidth().toFloat(),
            getHeight().toFloat(),
            10,
            ColorUtils.interpolateColorC(c1, c2, 50f)
        )

        RectBuilder(pos1, pos2).apply {
            outlineColor(c1.brighter(),c2.setAlpha(0), c2.setAlpha(0), c1.brighter())
            width(2.0)
            color(c1, c2, c1, c2)
            radius(6.0)
            draw()
        }

        RectBuilder(pos1.plus(5.0, 5.0), pos2.minus(5.0, 60.0)).apply {
            color(c1.darker(), c2.darker(), c2.darker(), c1.darker())
            radius(4.0)
            draw()
        }

        RectBuilder(Vec2d(pos1.x + getWidth() / 2, pos1.y + 35), Vec2d((pos1.x + getWidth() / 2) + 1, pos1.y + getHeight() - 5)).apply {
            outlineColor(Color.WHITE)
            draw()
        }
    }

    private fun renderPlayerHead() {
        val pos1 = Vec2d(pos.x, pos.y)
        val locationSkin = getPlayerLocationSkin() ?: return
        RenderUtils2D.drawBlurredShadow((pos1.x + 8).toFloat(), (pos1.y + 8).toFloat(), 20f, 20f, 10, Color.BLACK)
        info.drawHead(locationSkin, (pos1.x + 8).toInt(), (pos1.y + 8).toInt())
    }

    private fun getPlayerLocationSkin(): ResourceLocation? {
        val connection = mc.connection ?: return null
        val player = mc.player ?: return null
        val playerInfo = connection.getPlayerInfo(player.uniqueID) ?: return null
        return playerInfo.getLocationSkin()
    }

    private fun renderPlayerInfo() {
        val fr: Fonts = Fonts.DEFAULT_BOLD
        val pos1 = Vec2d(pos.x, pos.y)
        fr.drawString("Kills: " + info.getPlayerKills(), pos1.plus(8.0, (fr.getHeight() * 4) + 2))
        fr.drawString("K/D: " + info.getPlayerKD(), pos1.plus(8.0, (fr.getHeight() * 5) + 4))
    }

    private fun renderOnlineTime() {
        val fr: Fonts = Fonts.DEFAULT_BOLD
        val pos1 = Vec2d(pos.x, pos.y)
        fr.drawString(
            "OnlineTime: ",
            pos1.plus(8.0, (fr.getHeight() * 6) + 6),
            true,
            Color.WHITE
        )
        val playTimeActual: IntArray = info.getPlayTime()
        info.drawAnimatedPlaytime(
            (pos1.x.toFloat() + 8 + fr.getStringWidth("OnlineTime:  ")).toFloat(),
            (pos1.y.toFloat() + fr.getHeight() * 6 + 6).toFloat(),
            playTimeActual
        )
    }

    private fun renderServerInfo() {
        val fr: Fonts = Fonts.DEFAULT_BOLD
        val pos1 = Vec2d(pos.x, pos.y)
        val screenWidth = (pos1.x + getWidth() / 2).toFloat()
        val serverTextWidth = fr.getStringWidth(serverText)

        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        RenderUtils2D.glScissor(
            pos1.x.toFloat(),
            pos1.y.toFloat(),
            (pos1.x + (getWidth() / 2) - serverTextWidth / 2).toFloat(), // Scissor width adjustment
            (pos1.y + getHeight()).toFloat(),
            sr
        )

        fr.drawString(serverText, pos1.plus(8.0, (fr.getHeight() * 7) + 8))

        val serverIP = info.getServerIP()
        val stringWidth = pos1.x + fr.getStringWidth(serverIP)

        val scrollSpeed = 0.01

        if (stringWidth > screenWidth - serverTextWidth) {
            isScrolling = true
        } else {
            isScrolling = false
            scrollingTextPos = 0.0 // Reset scroll position when stringWidth <= scissor width
        }

        if (isScrolling) {
            scrollingTextPos += scrollSpeed
            if (scrollingTextPos > stringWidth + screenWidth - serverTextWidth) {
                scrollingTextPos = 0.0
            }

            val textToShow = "$serverIP    "
            val textLength = textToShow.length
            val scrollStartPosition = (scrollingTextPos / 2) % textLength
            val endPosition = min(scrollStartPosition.toFloat() + screenWidth, textLength.toFloat()) // Ensure end position doesn't exceed text length
            val visibleText = textToShow.substring(scrollStartPosition.toInt(), endPosition.toInt())
            fr.drawString(visibleText, pos1.plus(8.0 + serverTextWidth, (fr.getHeight() * 7) + 8))
        } else {
            fr.drawString(serverIP, pos1.plus(8.0 + serverTextWidth, (fr.getHeight() * 7) + 8))
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST)
    }

    class StatusInfo(
        val name: String,
        val entity: EntityLivingBase?
    ) {
        companion object {
            val SELF = StatusInfo(mc.session.username, null)
        }

        fun drawHead(
            skin: ResourceLocation,
            width: Int,
            height: Int
        ) {
            GL11.glColor4f(1f, 1f, 1f, 1f)
            Helper.mc.textureManager.bindTexture(skin)
            Gui.drawScaledCustomSizeModalRect(width, height, 8f, 8f, 8, 8, 20, 20, 64f, 64f)
        }

        fun getPlayerKills() =Minecraft.getMinecraft().player.statFileWriter.readStat(StatList.PLAYER_KILLS)
        fun getPlayerDeaths() = Minecraft.getMinecraft().player.statFileWriter.readStat(StatList.DEATHS)

        fun getPlayerKD(): Double {
            val kills = getPlayerKills()
            val deaths = getPlayerDeaths()

            return if (deaths == 0) {
                if (kills == 0) {
                    0.0
                } else {
                    Double.POSITIVE_INFINITY
                }
            } else {
                val kdRatio = kills.toDouble() / deaths.toDouble()
                MathUtils.round(kdRatio, 2)
            }
        }

        fun getServerIP(): String {
            val ip: String =
                if (Helper.mc.currentServerData != null) {
                    Helper.mc.currentServerData!!.serverIP
            } else {
                "SinglePlayer"
            }
            return ip
        }

        fun drawAnimatedPlaytime(
            x: Float,
            y: Float,
            playTime: IntArray
        ) : Unit? {
            val seconds = (if (playTime[2] < 10) "0" else "") + playTime[2]
            val minutes = (if (playTime[1] < 10) "0" else "") + playTime[1]
            val sb = StringBuilder(seconds)
            if (playTime[1] > 0 || playTime[0] > 0) {
                sb.insert(0, "$minutes:")
            }
            if (playTime[0] > 0) {
                sb.insert(0, playTime[0].toString() + ":")
            }
            Fonts.DEFAULT_BOLD.drawString(sb.toString(), Vec2d(x - 1.5, y.toDouble()), true,Color.WHITE)
            return null
        }

        fun getPlayTime(): IntArray {
            val diff = getTimeDiff()
            var diffSeconds: Long = 0
            var diffMinutes: Long = 0
            var diffHours: Long = 0
            if (diff > 0) {
                diffSeconds = diff / 1000 % 60
                diffMinutes = diff / (60 * 1000) % 60
                diffHours = diff / (60 * 60 * 1000) % 24
            }

            return intArrayOf(diffHours.toInt(), diffMinutes.toInt(), diffSeconds.toInt())
        }

        private fun getTimeDiff(): Long {
            return (if (endTime.toInt() == -1) System.currentTimeMillis() else endTime) - startTime
        }

    }

    private fun drawCircle(
        x: Double,
        y: Double,
        scale: Double,
        width: Float,
        offset: Double
    ) {
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        val oldState = GL11.glIsEnabled(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glShadeModel(GL11.GL_SMOOTH)
        GL11.glLineWidth(2 + width)
        GL11.glColor4f(0.1f, 0.1f, 0.1f, 0.5f)
        GL11.glPushMatrix()
        GL11.glTranslated(x, y, 1.0)
        GL11.glScaled(scale, scale, scale)

        GL11.glBegin(GL11.GL_LINE_STRIP)
        for (i in 0 until 360) {
            val x = cos(Math.toRadians(i.toDouble())) * 11
            val z = sin(Math.toRadians(i.toDouble())) * 11
            GL11.glVertex2d(x, z)
        }
        GL11.glEnd()
        GL11.glBegin(GL11.GL_LINE_STRIP)
        for (i in -90 until (-90 + (360 * offset)).toInt()) {
            var red = circleColor.red
            var green = circleColor.green
            var blue = circleColor.blue
            when (colorType.name) {
                "State" -> {
                    val buffer = getRG(offset.toInt())
                    red = buffer[0]
                    green = buffer[1]
                    blue = buffer[2]
                }
                "Astolfo" -> {
                    val stage = (i + 90) / 360.0
                    val clr = astolfo.getColor(stage)
                    red = ((clr shr 16) and 255)
                    green = ((clr shr 8) and 255)
                    blue = (clr and 255)
                }
            }
            GL11.glColor4f(red / 255f, green / 255f, blue / 255f, 1f)
            val x = cos(Math.toRadians(i.toDouble())) * 11
            val z = sin(Math.toRadians(i.toDouble())) * 11
            GL11.glVertex2d(x, z)
        }
        GL11.glEnd()
        GL11.glPopMatrix()

        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        if (!oldState)
            GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glColor4f(1f, 1f, 1f, 1f)
        Fonts.DEFAULT.drawString((offset * 100).toInt().toString() + "%", Vec2d(x + 4.5 - Fonts.DEFAULT.getStringWidth((offset * 100).toInt().toString() + "%", 0.8), y - 0.8), color = Color(200, 200, 200, 255), scale = 1.0)
        //Fonts.DEFAULT.drawString(name, Vec2d(x - Fonts.DEFAULT.getStringWidth(name, 1.0), y - 20.0), color = Color.WHITE, scale = 1.0)
    }

    private fun getRG(input: Int): IntArray {
        return intArrayOf(255 - 255 * input, 255 * input, 100 * input)
    }

    fun reset() {
        startTime = System.currentTimeMillis()
        endTime = -1
        gamesPlayed = 0
    }

    override fun getWidth() = tatusWidth
    override fun getHeight() = 90.0
}