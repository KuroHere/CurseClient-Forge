package com.curseclient.client.module.impls.hud

import com.curseclient.client.Client
import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.PingeUtils
import com.curseclient.client.utility.render.ColorUtils.glColor
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.RenderUtils2D.createFrameBuffer
import com.curseclient.client.utility.render.font.FontRenderer
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.graphic.GLUtils.draw
import com.curseclient.client.utility.render.shader.gradient.GradientUtil
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.shader.RoundedUtil.setAlphaLimit
import com.curseclient.client.utility.render.shader.blur.KawaseBloom.renderBlur
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.client.shader.Framebuffer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL14
import java.awt.Color

//TODO: Done this ...
object Watermark: DraggableHudModule(
    "Watermark",
    "Yes watermark",
    HudCategory.HUD,
) {
    private val mode by setting("Mode", W_Mode.Modern)
    private val size by setting("Size", 1.0, 0.5, 3.0, 0.1, { mode != W_Mode.CSGO })

    // Text
    private val version by setting("Version", true, visible = { mode == W_Mode.Text })
    private val clientName by setting("CustomWatermark", "Curse", visible = { mode == W_Mode.Text })

    // Modern
    private val radius by setting("Radius", 3.0, 0.0, 10.0, 0.5, visible = { mode == W_Mode.Modern})
    private val lodBiasSetting by setting("Smoothing", 0.0, -10.0, 10.0, 0.5, visible = { mode == W_Mode.Modern})

    private const val w = 135.0
    private const val h = 20.0
    private const val margin = 4.0

    private var lod: Float? = null
    private var stencilFramebuffer = Framebuffer(1, 1, false)

    enum class W_Mode {
        Text,
        Modern,
        CSGO,
        Logo
    }

    private fun getPositionWithOffset(): Pair<Vec2d, Vec2d> {
        val pos1 = pos
        val pos2 = pos.plus(getWidth(), getHeight())
        return Pair(pos1, pos2)
    }

    private fun getColorsForMode() =
         when (mode) {
             W_Mode.Logo -> Pair(HUD.getColor(0), HUD.getColor(5))
             W_Mode.CSGO -> Pair(HUD.getColor(0), HUD.getColor(10))
             W_Mode.Text -> Pair(HUD.getColor(0), HUD.getColor(10))
             W_Mode.Modern -> Pair(HUD.getColor(0), HUD.getColor(5))
         }

    override fun onRender() {
        val (pos1, pos2) = getPositionWithOffset()
        val (c1, c2) = getColorsForMode()

        when (mode) {
            W_Mode.Logo -> renderLogo(pos1, c1, c2)
            W_Mode.CSGO -> renderCSGO(pos1, c1, c2)
            W_Mode.Text -> renderText(pos1, c1, c2)
            W_Mode.Modern -> renderModern(pos1, pos2, c1, c2)
        }
    }

    private fun renderLogo(
        pos1: Vec2d,
        c1: Color,
        c2: Color
    ) {
        val imageSizeLimit = 3.5
        val imageScaleLimit = 1.5
        val adjustedSize = size.coerceIn(1.5, imageSizeLimit)
        val adjustedScale = (adjustedSize / imageSizeLimit) * imageScaleLimit

        val WH = adjustedScale * 256 / 4.5f
        stencilFramebuffer = createFrameBuffer(stencilFramebuffer)
        stencilFramebuffer.framebufferClear()
        stencilFramebuffer.bindFramebuffer(false)

        GradientUtil.applyGradientCornerRL(
            pos1.x.toFloat(), pos1.y.toFloat(),
            (pos1.x + getWidth()).toFloat(), (pos1.y + getHeight()).toFloat(),
            1f, c1, c2
        ) {
            mc.textureManager.bindTexture(ResourceLocation("textures/icons/logo/logo.png"))
            drawModalRectWithCustomSizedTexture(
                pos1.x.toInt(), pos1.y.toInt(), 0F, 0F, WH.toInt(), WH.toInt(), WH.toFloat(), WH.toFloat())
        }
        stencilFramebuffer.unbindFramebuffer()
        renderBlur(stencilFramebuffer.framebufferTexture, 2, 2)

        GradientUtil.applyGradientCornerRL(
            pos1.x.toFloat(), pos1.y.toFloat(),
            (pos1.x + getWidth()).toFloat(), (pos1.y + getHeight()).toFloat(),
            1f, c1, c2
        ) {
            mc.textureManager.bindTexture(ResourceLocation("textures/icons/logo/logo.png"))
            drawModalRectWithCustomSizedTexture(
                pos1.x.toInt(), pos1.y.toInt(), 0F, 0F, WH.toInt(), WH.toInt(), WH.toFloat(), WH.toFloat())
        }
    }

    private fun renderCSGO(
        pos1: Vec2d,
        c1: Color,
        c2: Color
    ) {
        val curse = "Curse"
        val text = "sense - ${mc.session.username} - ${if (mc.isSingleplayer) "singleplayer" else mc.currentServerData!!.serverIP} - ${PingeUtils.getPing()}ms"
        val x = pos1.x.plus(getWidth() / 4.6)
        val y = pos1.y.plus(getHeight() / 3.2)

        val textWidth = FontRenderer.getStringWidth(text, Fonts.DEFAULT) + FontRenderer.getStringWidth(curse, Fonts.DEFAULT_BOLD, 1.2f)
        val textHeight = FontRenderer.getFontHeight(Fonts.DEFAULT).toDouble()

        RenderUtils2D.drawBlurredRect(Vec2d(x - 1.7f, y + 4.5f), Vec2d(x + textWidth + 1.8f, y + textHeight + 10), 10, c1.darker())
        RectBuilder(Vec2d(x - 1.7f, y + 4.5f), Vec2d(x + textWidth + 1.8f, y + textHeight + 10)).draw {
            outlineColor(HUD.bgColor.brighter(), c1.darker().darker(), c2.darker().darker(), HUD.bgColor.brighter())
            width(2.0)
            color(HUD.bgColor)
        }

        GradientUtil.drawGradientLR((x - .3f).toFloat(), (y + 16f).toFloat(), textWidth - .3f, 1f, 1f, c1, c2)
        resetColor()
        GradientUtil.applyGradientHorizontal(x.toFloat(), y.toFloat(), textWidth + 3, 19F, 1f, c1, c2) {
            setAlphaLimit(0f)
            FontRenderer.drawString(text, (x + 0.5f + FontRenderer.getStringWidth(curse, Fonts.DEFAULT_BOLD, 1.2f)).toFloat(), (y + 5.5f).toFloat(), false, Color.WHITE, 1f, Fonts.DEFAULT)
        }

        FontRenderer.drawString(curse, (x).toFloat(), (y + 4f).toFloat(), false, Color.WHITE, 1.2f, Fonts.DEFAULT_BOLD)
    }

    private fun renderText(
        pos1: Vec2d,
        c1: Color,
        c2: Color
    ) {
        val xVal = pos1.x + 6f
        val yVal = pos1.y + 6f

        val textSizeLimit = 3.5
        val textScaleLimit = 1.5
        val adjustedTextSize = size.coerceIn(1.5, textSizeLimit)
        val adjustedTextScale = (adjustedTextSize / textSizeLimit) * textScaleLimit

        val versionWidth = FontRenderer.getStringWidth(Client.VERSION, Fonts.DEFAULT, adjustedTextScale.toFloat() * size.toFloat())
        val versionX = xVal + FontRenderer.getStringWidth(clientName, Fonts.DEFAULT, adjustedTextScale.toFloat() * size.toFloat())
        val width = if (version) {
            (versionX + versionWidth) - xVal
        } else {
            FontRenderer.getStringWidth(Client.NAME, Fonts.DEFAULT, adjustedTextScale.toFloat() * size.toFloat())
        }

        stencilFramebuffer = createFrameBuffer(stencilFramebuffer)
        stencilFramebuffer.framebufferClear()
        stencilFramebuffer.bindFramebuffer(false)
        GradientUtil.applyGradientHorizontal(xVal.toFloat(), yVal.toFloat(), width.toFloat(), 20F, 1F, c1, c2) {
            setAlphaLimit(0f)
            FontRenderer.drawString(clientName, xVal.toFloat(), yVal.toFloat(), false, Color.WHITE, adjustedTextScale.toFloat() * size.toFloat(), Fonts.DEFAULT_BOLD)
            if (version) {
                FontRenderer.drawString(Client.VERSION, versionX.toFloat(), yVal.toFloat(), false, Color.WHITE, (adjustedTextScale / 2.3).toFloat()  * size.toFloat(), Fonts.DEFAULT_BOLD)
            }
        }
        stencilFramebuffer.unbindFramebuffer()
        renderBlur(stencilFramebuffer.framebufferTexture, 2, 2)

        resetColor()
        GradientUtil.applyGradientHorizontal(xVal.toFloat(), yVal.toFloat(), width.toFloat(), 20F, 1F, c1, c2) {
            setAlphaLimit(0f)
            FontRenderer.drawString(clientName, xVal.toFloat(), yVal.toFloat(), false, Color.WHITE, adjustedTextScale.toFloat() * size.toFloat(), Fonts.DEFAULT_BOLD)
            if (version) {
                FontRenderer.drawString(Client.VERSION, versionX.toFloat(), yVal.toFloat(), false, Color.WHITE, (adjustedTextScale / 2.3).toFloat()  * size.toFloat(), Fonts.DEFAULT_BOLD)
            }
        }
    }

    private fun renderModern(
        pos1: Vec2d,
        pos2: Vec2d,
        c1: Color,
        c2: Color
    ) {
        // background
        RectBuilder(pos1, pos2).draw {
            shadow(
                pos1.x,
                pos1.y,
                getWidth(),
                getHeight(),
                10,
                Color.BLACK
            )
            color(HUD.bgColor)
            radius(radius)
        }

        // Logo
        resetColor()
        GradientUtil.applyGradientHorizontal(pos.x.toFloat(), pos.y.toFloat(), pos.x.toFloat() + getWidth().toFloat(), pos.y.toFloat() + getHeight().toFloat(), 1F, c1, c2) {
            setAlphaLimit(0f)
            drawTexture(pos1.plus(margin, 2.0), pos2.minus(margin, 2.0), Color.WHITE, Color.WHITE)
        }
    }


    private fun drawTexture(
        pos1: Vec2d,
        pos2: Vec2d,
        color1: Color,
        color2: Color
    ) {
        val texture = ResourceLocation("textures/icons/logo/modern.png")
        mc.textureManager.bindTexture(texture)
        GlStateManager.disableOutlineMode()
        GlStateManager.enableTexture2D()
        GlStateManager.disableAlpha()
        GlStateManager.enableBlend()

        glDisable(GL_ALPHA_TEST)

        resetColor()

        val l = lodBiasSetting.toFloat() * 0.25f - 0.5f
        if (lod != l) {
            glTexParameterf(GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, l)
            lod = l
        }

        draw(GL_QUADS) {
            color1.glColor()
            glTexCoord2d(0.0, 0.0)
            glVertex3d(pos1.x, pos1.y, 0.0)

            glTexCoord2d(0.0, 1.0)
            glVertex3d(pos1.x, pos2.y, 0.0)

            color2.glColor()
            glTexCoord2d(1.0, 1.0)
            glVertex3d(pos2.x, pos2.y, 0.0)

            glTexCoord2d(1.0, 0.0)
            glVertex3d(pos2.x, pos1.y,0.0)
        }

        GlStateManager.enableAlpha()
        resetColor()
    }

    override fun getWidth(): Double {
        val scaleFactor = when (mode) {
            W_Mode.Text -> {
                FontRenderer.getStringWidth(Client.NAME + if (version) Client.VERSION else 0, Fonts.DEFAULT, size.toFloat()) * 0.65f
            }
            W_Mode.Logo -> {
                h.toFloat() * size.times(1.3f)
            }
            else -> {
                w.toFloat() * size.times(0.65f)
            }
        }
        return scaleFactor.toDouble()
    }

    override fun getHeight() = h.toFloat() * size.times(if (mode == W_Mode.Logo) 1.3f else 0.65f)

}