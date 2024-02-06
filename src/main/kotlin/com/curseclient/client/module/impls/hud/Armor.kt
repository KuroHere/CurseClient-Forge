package com.curseclient.client.module.impls.hud

import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.StencilUtil.initStencilToWrite
import com.curseclient.client.utility.render.StencilUtil.readStencilBuffer
import com.curseclient.client.utility.render.StencilUtil.uninitStencilBuffer
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.shader.RoundedUtil
import com.curseclient.client.utility.render.shader.blur.GaussianBlur
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack
import java.awt.Color
import kotlin.math.round

object Armor: DraggableHudModule(
    "Armor",
    "Draws armor slots into gui",
    HudCategory.HUD
) {

    private val mode by setting("Mode", Mode.Horizon)
    private val background by setting("BackGround", Color(35, 35, 35, 50))
    private val radius by setting("Radius", 1.0, 0.0, 5.0, 0.1)
    private val bgBlur by setting("Blur", false)
    private val bRadius by setting("BlurRadius", 20, 5, 50, 1, { bgBlur })
    private val compression by setting("Compression", 2, 1, 5, 1, { bgBlur })
    private val flip by setting("Flip", false, { mode == Mode.Vertical})

    var w = 0.0
    var h = 0.0

    enum class Mode {
        Horizon,
        Vertical,
    }

    override fun onRender() {
        val horizontal = mode == Mode.Horizon
        val startingPos = if (horizontal) Vec2d(20.0, 0.0) else Vec2d(0.0, 20.0)
        val dimension = if (horizontal) Vec2d(76.0, 22.0) else Vec2d(16.0, 76.0)

        GlStateManager.pushMatrix()
        if (bgBlur) {
            GaussianBlur.startBlur()
            RoundedUtil.drawGradientRound(pos.x.toFloat(), pos.y.toFloat(), dimension.x.toFloat(), dimension.y.toFloat(), radius.toFloat(), background, background, background, background)
            GaussianBlur.endBlur(bRadius, compression)
        }

        GlStateManager.pushMatrix()
        initStencilToWrite()
        RectBuilder(pos, pos.plus(dimension)).apply {
            color(background)
            radius(radius)
            draw()
        }
        readStencilBuffer(1)
        uninitStencilBuffer()
        GlStateManager.popMatrix()

        RectBuilder(pos, pos.plus(dimension)).apply {
            shadow(pos.x, pos.y, dimension.x, dimension.y, 5, background)
            color(background)
            radius(radius)
            draw()
        }
        mc.player.armorInventoryList.reversed().forEachIndexed { index, item ->
            drawItem(item, pos.plus(startingPos * index.toDouble()))
            w = dimension.x
            h = dimension.y
        }
        GlStateManager.popMatrix()
    }

    private fun drawItem(item: ItemStack, pos: Vec2d) {
        if (item.isEmpty) return

        RenderUtils2D.drawItem(item, pos.x, pos.y, drawOverlay = false)

        val dura = item.maxDamage - item.itemDamage
        val duraMultiplier = dura / item.maxDamage.toFloat()
        val hue = duraMultiplier * 0.3f

        val color = Color.getHSBColor(hue, 1f, 1f)
        val text = "${round(duraMultiplier * 100.0)}%"

        val textScale = if (mode == Mode.Vertical) 1.0 else 0.75
        val xOffset = if (mode == Mode.Vertical) {
            if (flip) -12.0 - Fonts.DEFAULT.getStringWidth(text, 1.0) / 2.0
            else 8.0 + Fonts.DEFAULT.getStringWidth(text, 1.0) / 2.0
        } else 8.0 - Fonts.DEFAULT.getStringWidth(text, 0.75) / 2.0

        val yOffset = if (mode == Mode.Vertical) 3.0 + Fonts.DEFAULT.getHeight(1.0) / 2
        else 18.0

        Fonts.DEFAULT.drawString(
            text,
            Vec2d(pos.x + xOffset, pos.y + yOffset),
            color = color,
            scale = textScale
        )
    }

    override fun getWidth() = w
    override fun getHeight() = h
}