package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.events.EntityAttackedEvent
import com.curseclient.client.event.events.PacketEvent
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.runSafe
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.ColorExtend
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.extension.Timer
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.SPacketDestroyEntities
import net.minecraft.network.play.server.SPacketEntityStatus
import net.minecraft.network.play.server.SPacketExplosion
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random

object SuperHeroFX : Module(
    name = "SuperHeroFX",
    description = "Skid ZeroDay's SuperheroFX",
    category = Category.VISUAL) {

    private val popupStyle by setting("Popup Style", PopupStyle.Heaven)

    // The length of the popup animation
    private val length by setting("Length", 400f, 100f, 500f, 10f, visible = { popupStyle == PopupStyle.BanTumLum})

    private val explosionDelay by setting("Explosion Delay", 1.0, 0.0,5.0, 0.1, visible = { popupStyle == PopupStyle.Heaven})
    private val hitDelay by setting("Hit Delay", 1.0, 0.1,5.0, 0.1, visible = { popupStyle == PopupStyle.Heaven})
    private val scaling by setting("Scale", 3.0, 1.0,20.0, 0.1, visible = { popupStyle == PopupStyle.Heaven})
    private val duration by setting("Duration", 1.0, 0.1,10.0, .01, visible = { popupStyle == PopupStyle.Heaven})
    private val xRotate by setting("RotateX", true, visible = { popupStyle == PopupStyle.Heaven})
    private val zRotate by setting("RotateZ", true, visible = { popupStyle == PopupStyle.Heaven})

    val font by setting("Font", Font.Osaka)

    private val superHeroTextsBlowup = arrayOf("KABOOM", "BOOM", "POW", "KAPOW")
    private val superHeroTextsDamageTaken = arrayOf("OUCH", "ZAP", "BAM", "WOW", "POW", "SLAP")
    private val popTexts = CopyOnWriteArrayList<PopupText>()
    private val popups = CopyOnWriteArrayList<Popup>()
    private val hitTimer = Timer()
    private val explosionTimer = Timer()

    enum class PopupStyle {
        Heaven,
        BanTumLum //idk what should I call this

    }

    init {

        safeListener<Render3DEvent> {
            popups.forEach {
                it.render()
            }

            popups.removeIf { it.animation.getAnimationFactor() == 0.0 && !it.animation.state }
        }

        safeListener<EntityAttackedEvent> { event ->
            if (popupStyle.equals(PopupStyle.BanTumLum)) {
                for (i in 0 until ThreadLocalRandom.current().nextInt(4)) {
                    val offsetX = Random.nextFloat() * 2
                    val offsetY = Random.nextFloat() * 2
                    val offsetZ = Random.nextFloat() * 2
                    val text = superHeroTextsDamageTaken[Random.nextInt(superHeroTextsDamageTaken.size)]

                    popups.add(
                        Popup(
                            event.entity.positionVector.add(
                                offsetX - 1.0,
                                event.entity.height + offsetY - 1.0,
                                offsetZ - 1.0
                            ), text, Color(Color.HSBtoRGB(Random.nextFloat(), 1f, 1f))
                        )
                    )
                }
            }
        }

        safeListener<TickEvent.ClientTickEvent> {

            popTexts.removeIf { it.isMarked }
            popTexts.forEach { it.update() }
        }

        safeListener<Render3DEvent> { event ->
            if (popupStyle.equals(PopupStyle.Heaven)) {
                val fontRenderer = when (font) {
                    Font.Client -> Fonts.DEFAULT
                    Font.Client_Bold -> Fonts.DEFAULT_BOLD
                    Font.Osaka -> Fonts.OSAKACHIPS
                    Font.Knight -> Fonts.KNIGHT
                    Font.Badaboom -> Fonts.BADABOOM
                }
                popTexts.forEach { pop ->
                    val entity2 = mc.renderViewEntity

                    if (entity2 != null) {
                        var pos = MathUtils.getInterpolateVec3dPos(pop.pos, event.partialTicks)
                        val n = pos.x
                        var distance = pos.y + 0.65
                        val n2 = pos.z
                        val n3 = distance
                        pos = MathUtils.getInterpolateEntityClose(entity2, event.partialTicks)
                        val posX = entity2.posX
                        val posY = entity2.posY
                        val posZ = entity2.posZ
                        entity2.posX = pos.x
                        entity2.posY = pos.y
                        entity2.posZ = pos.z
                        distance = entity2.getDistance(n, distance, n2)
                        var scale = 0.04
                        if (distance > 0.0) {
                            scale = 0.02 + (scaling / 1000.0f) * distance
                        }
                        GlStateManager.pushMatrix()
                        GlStateManager.doPolygonOffset(1.0f, -1500000.0f)
                        GlStateManager.translate(n.toFloat(), n3.toFloat() + 1.4f, n2.toFloat())
                        val n7 = -mc.renderManager.playerViewY

                        GlStateManager.rotate(n7, pop.xIncrease, 1.0f, pop.zIncrease)
                        GlStateManager.rotate(mc.renderManager.playerViewX, if (mc.gameSettings.thirdPersonView == 2) -1.0f else 1.0f, 0.0f, 0.0f)
                        GlStateManager.scale(-scale, -scale, scale)
                        val nameTag = pop.displayName
                        val width = fontRenderer.getStringWidth(nameTag, 1.0) / 2f
                        val height = fontRenderer.getHeight(1.0)
                        fontRenderer.drawString(nameTag, Vec2d(-width + 1.0f, -height + 3.0f), true, pop.colorExtend.toHSB(), 1.0)
                        GlStateManager.disablePolygonOffset()
                        GlStateManager.doPolygonOffset(1.0f, 1500000.0f)
                        GlStateManager.popMatrix()
                        entity2.posX = posX
                        entity2.posY = posY
                        entity2.posZ = posZ
                    }
                }
            }
        }

        safeListener<PacketEvent.Receive> { event ->
            runSafe {
                if (event.packet !is SPacketExplosion) {
                    if (event.packet !is SPacketEntityStatus) {
                        if (event.packet is SPacketDestroyEntities) {
                            val packet = event.packet
                            for (id in packet.entityIDs) {
                                val e = mc.world.getEntityByID(id) ?: return@runSafe
                                if (e.isDead && mc.player.getDistance(e) < 20.0f && e != mc.player) {
                                    if (e is EntityPlayer) {
                                        val pos = Vec3d(e.posX + Random.nextDouble(), e.posY + Random.nextDouble() - 2.0, e.posZ + Random.nextDouble())
                                        popTexts.add(PopupText("EZ", pos))
                                    }
                                }
                            }
                        }
                    } else {
                        val packet2 = event.packet
                        if (mc.world == null) {
                            return@runSafe
                        }
                        val e2 = packet2.getEntity(mc.world) ?: return@runSafe
                        if (packet2.opCode.toInt() != 35) {
                            if (mc.player.getDistance(e2) < 20.0f && e2 != mc.player) {
                                val pos2 = Vec3d(e2.posX + Random.nextDouble(), e2.posY + Random.nextDouble() - 2.0, e2.posZ + Random.nextDouble())
                                if (hitTimer.passed((hitDelay * 1000.0f))) {
                                    hitTimer.reset()
                                    popTexts.add(PopupText(superHeroTextsDamageTaken[Random.nextInt(superHeroTextsBlowup.size)], pos2))
                                }
                            }
                        } else if (mc.player.getDistance(e2) < 20.0f) {
                            popTexts.add(PopupText("POP", e2.positionVector.add((Random.nextInt(2) / 2).toDouble(), 1.0, (Random.nextInt(2) / 2).toDouble())))
                        }
                    }
                } else {
                    val packet3 = event.packet
                    val pos3 = Vec3d(packet3.x + Random.nextDouble(), packet3.y + Random.nextDouble() - 2.0, packet3.z + Random.nextDouble())
                    if (mc.player.getDistance(pos3.x, pos3.y, pos3.z) < 10.0 && explosionTimer.passed((explosionDelay * 1000.0f))) {
                        explosionTimer.reset()
                        popTexts.add(PopupText(superHeroTextsBlowup[Random.nextInt(superHeroTextsBlowup.size)], pos3))
                    }
                }
            }
        }
    }

    private class Popup(val vec: Vec3d, val text: String, val colour: Color) {
        val fontRenderer = when (font) {
            Font.Client -> Fonts.DEFAULT
            Font.Client_Bold -> Fonts.DEFAULT_BOLD
            Font.Osaka -> Fonts.OSAKACHIPS
            Font.Knight -> Fonts.KNIGHT
            Font.Badaboom -> Fonts.BADABOOM
        }
        val animation = Animation({ length.toFloat() }, false, Easing.CUBIC_IN_OUT)

        init {
            animation.state = true
        }

        fun render() {
            if (animation.state && animation.getAnimationFactor() == 1.0) {
                animation.state = false
            }

            RenderUtils2D.drawNametag(vec, false) {
                val width = fontRenderer.getStringWidth(text).toFloat()

                RenderUtils2D.scaleTo(width / 2f, (fontRenderer.getHeight() / 2).toFloat(), 0f, animation.getAnimationFactor(), animation.getAnimationFactor(), 0.0) {
                    fontRenderer.drawString(
                        text,
                        Vec2d(0, 0),
                        true,
                        colour
                    )
                }
            }
        }
    }

    class PopupText(val displayName: String, var pos: Vec3d) {
        private val timer = Timer()
        private val startTime = System.currentTimeMillis()
        private var yIncrease = Random.nextDouble()
        var xIncrease = Random.nextFloat()
        var zIncrease = Random.nextFloat()
        private val duration = 1000.0 * SuperHeroFX.duration
        var isMarked = false
        var colorExtend: ColorExtend

        fun update() {
            pos = pos.add(0.0, yIncrease, 0.0)
            val presentA = 1.0 - ((System.currentTimeMillis() - startTime) / duration).coerceAtMost(1.0).coerceAtLeast(0.0)
            colorExtend = colorExtend.alpha((presentA * 255.0).toInt())
            if (timer.passed(duration)) {
                isMarked = true
            }
        }

        init {
            val hue = floatArrayOf(System.currentTimeMillis() % (360 * 32) / (360f * 32) * 6)
            colorExtend = ColorUtils.hsbToRGB(hue[0], 1.0f, 1.0f)
            while (yIncrease > 0.025 || yIncrease < 0.011) {
                yIncrease = Random.nextDouble()
            }
            if (!xRotate) xIncrease = 0f
            if (!zRotate) zIncrease = 0f
            timer.reset()
        }
    }


    enum class Font {
        Osaka, Knight, Badaboom, Client, Client_Bold
    }

}