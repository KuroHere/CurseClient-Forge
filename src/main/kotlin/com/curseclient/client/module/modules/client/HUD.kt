package com.curseclient.client.module.modules.client

import baritone.api.utils.Helper
import com.curseclient.client.event.events.render.Render2DEvent
import com.curseclient.client.event.listener.runSafe
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.visual.BetterScreenshot
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.b
import com.curseclient.client.utility.render.ColorUtils.g
import com.curseclient.client.utility.render.ColorUtils.r
import com.curseclient.client.utility.render.ColorUtils.setAlphaD
import com.curseclient.client.utility.render.animation.AnimationFlag
import com.curseclient.client.utility.render.shader.BlurUtil
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.client.gui.ScaledResolution
import net.minecraftforge.client.event.RenderGameOverlayEvent
import java.awt.Color
import kotlin.math.ceil
import kotlin.math.floor


object HUD : Module(
    "HUD",
    "Global configuration for hud",
    Category.CLIENT,
    enabledByDefault = true
) {

    val page by setting("Page", Page.Client)

    //Client
    val color1 by setting("Color 1", Color(30, 170, 200), visible = { page == Page.Client })
    val color2 by setting("Color 2", Color(170, 30, 100), visible = { page == Page.Client })
    val bgColor by setting("Background Color", Color(25, 25, 25), visible = { page == Page.Client })

    //Game
    val containerBackground by setting("Container-Background", false, visible = { page == Page.Game })
    val guiButtonStyle by setting("Button-Style", ButtonStyle.Minecraft, visible = { page == Page.Game })

    // BLur
    val blur by setting("Blur", false, visible = { page == Page.Game })

    // How intense the blur is
    val intensity by setting("Intensity", 10f, 1f, 20f, 1f, visible = { page == Page.Game && blur })

    // The speed of the fade in animation
    val animationSpeed by setting("BlurSpeed", 200f, 0f, 500f, 5f, visible = { page == Page.Game && blur })

    // The easing type of the animation
    val easing by setting("Easing", Easing.LINEAR, visible = { page == Page.Game && blur })
    val fade = Animation({ animationSpeed.toFloat() }, false, { easing })

    val animHotbarValue by setting("AnimatedHotbar", true, visible = {page == Page.Game})
    val blackHotbarValue by setting("BlackHotbar", true, visible = { page == Page.Game })
    private val hotbarAnimation = AnimationFlag(com.curseclient.client.utility.render.animation.Easing.OUT_CUBIC, 200.0f)

    enum class Page {
        Client,
        Game
    }

    enum class ButtonStyle {
        Minecraft,
        CurseClient,
        Rounded,
        CurseClientPLus
    }


    init {
        safeListener<Render2DEvent> {
            fade.state = HUD.mc.currentScreen != null && HUD.mc.currentScreen != BetterScreenshot.GuiScreenshot
        }
        safeListener<RenderGameOverlayEvent.Post> { event: RenderGameOverlayEvent.Post ->
            if (blur && event.type == RenderGameOverlayEvent.ElementType.HOTBAR && fade.getAnimationFactor() > 0) {
                val scaledResolution = ScaledResolution(HUD.mc)

                BlurUtil.blur(0f, 0f, scaledResolution.scaledWidth.toFloat(), scaledResolution.scaledHeight.toFloat(), (intensity * fade.getAnimationFactor()).toInt().toFloat())
            }
        }
    }

    override fun onEnable() {
        runSafe {
            val currentPos = Helper.mc.player.inventory.currentItem.toFloat() * 20.0f
            hotbarAnimation.forceUpdate(currentPos, currentPos)
        }
    }

    @JvmStatic
    fun updateHotbar(): Float {
        val currentPos = mc.player?.let {
            it.inventory.currentItem * 20.0f
        } ?: 0.0f

        return hotbarAnimation.getAndUpdate(currentPos)
    }

    fun getColor(offset: Int = 0, b: Double = 1.0): Color {
        return getRainbow(offset * 200, b)
    }

    fun getColor(offset: Int = 0, s: Double = 1.0, b: Double = 1.0): Color {
        return getRainbow(offset * 200, s, b)
    }

    fun getColorByProgress(progress: Double, brightness: Double = 1.0): Color {
        val c = ColorUtils.lerp(color1.setAlphaD(1.0), color2.setAlphaD(1.0), progress)
        val b = clamp(brightness, 0.0, 1.0).toFloat()
        return Color(c.r * b, c.g * b, c.b * b, 1f)
    }

    fun getColorByProgress(progress: Double, saturation: Double = 1.0, brightness: Double = 1.0): Color {
        val c = ColorUtils.lerp(color1.setAlphaD(1.0), color2.setAlphaD(1.0), progress)
        return Color.getHSBColor(Color(c.r, c.g, c.b, 1f).rgb.toFloat(), saturation.toFloat(), brightness.toFloat())
    }

    private fun getRainbow(timeOffset: Int, saturation: Double = 1.0, brightness: Double): Color {
        var time = ceil((System.currentTimeMillis() - timeOffset) / 20.0)
        time %= 360.0
        val progressRaw = (time / 360.0f).toFloat()
        val progress = (if (progressRaw > 0.5) 1f - progressRaw else progressRaw) * 2.0
        return getColorByProgress(progress, saturation, brightness)
    }

    private fun getRainbow(timeOffset: Int, brightness: Double): Color {
        var time = ceil((System.currentTimeMillis() - timeOffset) / 20.0)
        time %= 360.0
        val progressRaw = (time / 360.0f).toFloat()
        val progress = (if (progressRaw > 0.5) 1f - progressRaw else progressRaw) * 2.0
        return getColorByProgress(progress, brightness)
    }

}