package com.curseclient.client.gui.impl.mcgui.intro

import com.curseclient.client.gui.impl.mainmenu.MainMenu
import com.curseclient.client.utility.math.Timer
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.animation.animaions.Rise6Animation
import com.curseclient.client.utility.render.animation.ease.EaseUtils
import com.curseclient.client.utility.render.font.FontUtils.drawCentreString
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.shader.RoundedUtil.endBlend
import com.curseclient.client.utility.render.shader.RoundedUtil.startBlend
import com.curseclient.client.utility.render.shader.blur.KawaseBloom
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.sound.SoundUtils
import com.curseclient.client.utility.sound.SoundUtils.playSound
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Keyboard
import java.awt.Color

class PrereleaseDisclaimer : GuiScreen() {
    private val fadeAnimation = Rise6Animation(EaseUtils.EaseType.InOutCubic, 1000)
    private val stopwatch = Timer()

    private var isEnterKeyPressed = false
    private var enterKeyPressStartTime: Long = 0
    private var lastUpdateTime: Long = 0
    private var soundIsPlaying = false
    private var progressCircle = 0F

    override fun initGui() {
//        cape()
        fadeAnimation.reset()
        stopwatch.reset()
    }

//    private fun downloadFile(urlStr: String, file: String) {
//        val url = URL(urlStr)
//        val rbc = Channels.newChannel(url.openStream())
//        val fos = FileOutputStream(file)
//        fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
//        fos.close()
//        rbc.close()
//    }
//
//    // (￣y▽,￣)╭ Kuro is so bad can't even know how to set up cape url.
//    private fun cape() {
//        val capes = File(CurseClient.DIR + "/Capes")
//        if (!capes.isDirectory) capes.delete()
//        if (!capes.exists()) capes.mkdir()
//        val alreadyDownloadedWithFN: MutableMap<String, String> = HashMap()
//
//        try {
//            val httpClient: CloseableHttpClient = HttpClients.createDefault()
//            val httpGet = HttpGet("https://raw.githubusercontent.com/KuroHere/.../capes.txt")
//
//            val response = httpClient.execute(httpGet)
//
//            response.entity.content.reader().use { reader ->
//                reader.readLines().forEach { s1 ->
//                    if (s1.startsWith("#")) return@forEach // ignore comments
//                    CurseClient.LOG.info(s1)
//                    val split = s1.split(" +".toRegex()).toTypedArray() // split everything at a space
//                    if (split.size != 2) return@forEach // we only want "uuid capeUrl" format
//                    val uuid = split[0]
//                    val capeUrl = split[1]
//                    try {
//                        val u = UUID.fromString(uuid)
//                        if (alreadyDownloadedWithFN.containsKey(capeUrl)) {
//                            CurseClient.LOG.info("Skipping $uuid because already downloaded")
//                            CurseClient.capes = CurseClient.capes.plus(u to (alreadyDownloadedWithFN[capeUrl] ?: "")) as MutableMap<UUID, String>
//                            return@forEach
//                        }
//                        CurseClient.LOG.info("Downloading for $uuid")
//                        downloadFile(capeUrl, "Capes/$u.png")
//                        alreadyDownloadedWithFN[capeUrl] = "$u.png"
//                        CurseClient.capes[u] = "$u.png"
//                    } catch (ignored: Exception) {
//                        CurseClient.LOG.info("Invalid UUID entry \"$uuid\"")
//                    }
//                }
//            }
//            CurseClient.LOG.info("-- Cape mappings --")
//            CurseClient.capes.forEach { (uuid, s1) ->
//                CurseClient.LOG.info("  $uuid has cape at $s1")
//            }
//
//        } catch (e: Exception) {
//            CurseClient.LOG.info("Failed to download capes!")
//        }
//    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        Gui.drawRect(0, 0, mc.displayWidth, mc.displayHeight, Color.BLACK.rgb)
        fadeAnimation.run(if (stopwatch.getElapsedTime() > 4000) 0.0 else 255.0)
        if (stopwatch.getElapsedTime() < 4000) {
            playSound(SoundUtils.Sound.GAME_STAR, 0.2)
        }
        val sr = ScaledResolution(mc)
        startBlend()
        Fonts.DEFAULT.drawCentreString(
            "Note: This is public prerelease software",
            Vec2d(sr.scaledWidth / 2.0,
                sr.scaledHeight / 2.0 - 70),
            color = Color.WHITE.setAlpha(fadeAnimation.value.toInt()),
            scale = 1.5
        )
        Fonts.DEFAULT.drawCentreString(
            "Features, interfaces and sequences are final and won't be expected to change",
            Vec2d(sr.scaledWidth / 2.0,
                sr.scaledHeight / 2.0 - 50),
            color = Color.WHITE.setAlpha(fadeAnimation.value.toInt()),
            scale = 1.5
        )

        Fonts.DEFAULT.drawCentreString(
            "© CurseClient 2024. All Rights Reserved",
            Vec2d(sr.scaledWidth / 2.0,
                sr.scaledHeight / 2.0 + 70),
            color = Color.WHITE.setAlpha(fadeAnimation.value.toInt() / 2),
            scale = 1.0
        )
        if (stopwatch.finished(6000)) {
            Fonts.DEFAULT.drawCentreString(
                "Press  X  to entrance",
                Vec2d(sr.scaledWidth / 2.0,
                    sr.scaledHeight / 1.5),
                false,
                color = ColorUtils.pulseColor(Color.WHITE, 0, 255),
                scale = 2.0
            )
        }
        endBlend()

        if (stopwatch.finished(6000)) {
            if (isEnterKeyPressed) {
                if (enterKeyPressStartTime == 0L) {
                    enterKeyPressStartTime = System.currentTimeMillis()
                }

                val elapsedTime = System.currentTimeMillis() - enterKeyPressStartTime
                val timeRequired = 3000
                progressCircle = (elapsedTime.toFloat() / timeRequired) * 360F

                progressCircle = progressCircle.coerceIn(0F, 360F)
                startBlend()
                GlStateManager.pushMatrix()
                KawaseBloom.glBloom({
                    RenderUtils2D.drawCircle(sr.scaledWidth / 2F - 21.8F, sr.scaledHeight / 1.5F + 1, 0F, progressCircle, 6.3F, 2.3F, false, Color.WHITE.setAlpha(255).rgb)
                }, 5, 5)
                RenderUtils2D.drawCircle(sr.scaledWidth / 2F - 22F, sr.scaledHeight / 1.5F + 1, 0F, progressCircle, 6.3F, 3F, false, ColorUtils.pulseColor(Color.WHITE.setAlpha(255), 0, 255).rgb)
                endBlend()
                GlStateManager.popMatrix()

                if (elapsedTime >= timeRequired) {
                    mc.displayGuiScreen(MainMenu())
                    isEnterKeyPressed = false
                    enterKeyPressStartTime = 0
                    progressCircle = 0F
                }

                val timeDifference = System.currentTimeMillis() - lastUpdateTime
                if (timeDifference >= 1500 && !soundIsPlaying) {
                    soundIsPlaying = true
                    playSound(SoundUtils.Sound.CLICK_UI, 1.0)
                    lastUpdateTime = System.currentTimeMillis()
                } else if (timeDifference >= 1500) {
                    lastUpdateTime = System.currentTimeMillis()
                }
            } else {
                progressCircle = 0F
                soundIsPlaying = false
            }
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_X && Keyboard.isKeyDown(Keyboard.KEY_X)) {
            isEnterKeyPressed = true
        }
    }
}