package com.curseclient.client.module.modules.hud.Status

import baritone.api.utils.Helper
import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.shader.RoundedUtil.endBlend
import com.curseclient.client.utility.render.shader.RoundedUtil.startBlend
import com.curseclient.client.utility.render.shader.ShaderUtils
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.EntityLivingBase
import net.minecraft.stats.StatList
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.min
import kotlin.math.roundToInt


object Status: DraggableHudModule(
    "Status",
    "Draw your ingame status kill, dead, playtime,...(Not done yet)",
    HudCategory.HUD
) {

    val Swidth by setting("Width", 160, 150, 200, 1)

    private var info = StatusInfo.SELF
    val sr: ScaledResolution = ScaledResolution(mc)

    private var scrollingTextPos = 0.0
    private val serverText = "Server: "
    private var isScrolling = false

    private const val progress = 0.0f

    var startTime = System.currentTimeMillis()
    var endTime:Long = -1
    var gamesPlayed = 0

    val circleShader: ShaderUtils = ShaderUtils("shaders/client/circle-arc.frag")

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

        updateMemoryUsage()
        renderPlayerInfo()
        renderOnlineTime()
        renderServerInfo()
    }

    private fun renderCard() {
        val pos1 = Vec2d(pos.x, pos.y)
        val pos2 = Vec2d(pos.x + getWidth(), pos.y + getHeight())

        val c1 = HUD.getColor(0)
        val c2 = HUD.getColor(5)

        RenderUtils2D.drawBlurredShadow(
            pos.x.toFloat(),
            pos.y.toFloat(),
            getWidth().toFloat(),
            getHeight().toFloat(),
            10,
            ColorUtils.interpolateColorC(c1, c2, 50f)
        )

        RectBuilder(pos1, pos2).apply {
            outlineColor(c1.brighter(),c2.setAlpha(0), c1.setAlpha(0), c2.brighter())
            width(2.0)
            color(c1, c2, c2, c1)
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

    fun updateMemoryUsage(): Float {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val maxMemory = runtime.maxMemory()

        val memoryUsage = ((totalMemory - freeMemory).toFloat() / maxMemory.toFloat()) * 100.0f

        val convertedProgress = memoryUsage / 100.0f

        drawCircleWithMemoryUsage(convertedProgress)

        return convertedProgress
    }


    private fun drawCircleWithMemoryUsage(memoryUsage: Float) {
        // Gọi hàm vẽ vòng cung (drawCircle) với giá trị progress mới
        // Bạn cần chuyển đổi giá trị memoryUsage sao cho phù hợp với range của shader trước khi truyền vào hàm drawCircle
        // Ví dụ: Chuyển đổi từ 0-1 sang 0-255 để đồng bộ với màu sắc (RGB)
        val convertedValue = (memoryUsage * 255).roundToInt()

        // Gọi hàm drawCircle với giá trị progress được cập nhật
        drawCircle(pos.x.toFloat(), pos.y.toFloat(),30f, convertedValue.toFloat(), 10, Color.WHITE, 10f)
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
        val screenWidth = (pos.x + getWidth() / 2).toFloat()
        val serverTextWidth = fr.getStringWidth(serverText)

        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        RenderUtils2D.glScissor(
            pos1.x.toFloat(),
            pos1.y.toFloat(),
            (pos1.x + screenWidth - serverTextWidth / 2).toFloat(), // Scissor width adjustment
            (pos1.y + getHeight()).toFloat(),
            sr
        )

        fr.drawString(serverText, pos1.plus(8.0, (fr.getHeight() * 7) + 8))

        val serverIP = info.getServerIP()
        val stringWidth = fr.getStringWidth(serverIP)

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

    class StatusInfo(val name: String, val entity: EntityLivingBase?) {
        companion object {
            val SELF = StatusInfo(mc.session.username, null)
        }

        fun drawHead(skin: ResourceLocation, width: Int, height: Int) {
            GL11.glColor4f(1f, 1f, 1f, 1f)
            Helper.mc.textureManager.bindTexture(skin)
            Gui.drawScaledCustomSizeModalRect(width, height, 8f, 8f, 8, 8, 20, 20, 64f, 64f)
        }

        fun getPlayerKills(): Int {
            return Minecraft.getMinecraft().player.statFileWriter.readStat(StatList.PLAYER_KILLS)
        }

        fun getPlayerDeaths(): Int {
            return Minecraft.getMinecraft().player.statFileWriter.readStat(StatList.DEATHS)
        }

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

        fun drawAnimatedPlaytime(x: Float, y: Float, playTime: IntArray) : Unit? {
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
            return (if (endTime.toInt() === -1) System.currentTimeMillis() else endTime) - startTime
        }

    }

    fun drawCircle(x: Float, y: Float, radius: Float, progress: Float, change: Int, color: Color, smoothness: Float) {
        startBlend()
        val borderThickness = 1f
        circleShader.init()
        circleShader.setUniformf("radialSmoothness", smoothness)
        circleShader.setUniformf("radius", radius)
        circleShader.setUniformf("borderThickness", borderThickness)
        circleShader.setUniformf("progress", progress)
        circleShader.setUniformi("change", change)
        circleShader.setUniformf("color", color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
        val wh = radius + 10
        val sr = ScaledResolution(Helper.mc)
        circleShader.setUniformf("pos", (x + (wh / 2f - (radius + borderThickness) / 2f)) * sr.scaleFactor,
            Minecraft.getMinecraft().displayHeight - (radius + borderThickness) * sr.scaleFactor - (y + (wh / 2f - (radius + borderThickness) / 2f)) * sr.scaleFactor)
        ShaderUtils.drawQuads(x, y, wh, wh)
        circleShader.unload()
        endBlend()
    }

    fun reset() {
        startTime = System.currentTimeMillis()
        endTime = -1
        gamesPlayed = 0
    }

    override fun getWidth() = Swidth
    override fun getHeight() = 90.0
}