package com.curseclient.client.module.impls.hud

import baritone.api.utils.Helper
import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.StencilUtil
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.shader.RoundedUtil
import com.curseclient.client.utility.render.shader.blur.GaussianBlur
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import java.awt.Color

object PvpResources : DraggableHudModule(
    "PvpResources",
    "Show count of items for cpvp",
    HudCategory.HUD
) {
    private val mode by setting("Mode", Mode.Horizon)
    private val textColor by setting("TextColor", Color(255, 255, 255, 255))
    private val background by setting("BackGround", Color(35, 35, 35, 50))
    private val bgBlur by setting("Blur", false)
    private val bRadius by setting("BlurRadius", 20, 5, 50, 1, { bgBlur })
    private val compression by setting("Compression", 2, 1, 5, 1, { bgBlur })

    private val radius by setting("Radius", 1.0, 0.0, 5.0, 0.1)

    var w = 0.0
    var h = 0.0

    enum class Mode { Horizon, Vertical, Quad }

    override fun onRender() {
        val horizontal = mode == Mode.Horizon
        val vertical = mode == Mode.Vertical
        val quad = mode == Mode.Quad
        val dimension = if (horizontal) Vec2d(80.0, 20.0) else if (vertical) Vec2d(20.0, 80.0) else Vec2d(40.0, 40.0)

        GlStateManager.pushMatrix()
        if (bgBlur) {
            GaussianBlur.glBlur({
                RoundedUtil.drawGradientRound(pos.x.toFloat(), pos.y.toFloat(), dimension.x.toFloat(), dimension.y.toFloat(), radius.toFloat(), Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE)
            }, bRadius, compression)
        }
        GlStateManager.popMatrix()

        StencilUtil.initStencilToWrite()
        RectBuilder(pos, pos.plus(dimension)).apply {
            color(background)
            radius(radius)
            draw()
        }
        StencilUtil.readStencilBuffer(1)
        StencilUtil.uninitStencilBuffer()

        GlStateManager.pushMatrix()

        RectBuilder(pos, pos.plus(dimension)).apply {
            shadow(pos.x, pos.y, dimension.x, dimension.y, 5, background)
            color(background)
            radius(radius)
            draw()
        }

        val itemTypes = listOf(Items.TOTEM_OF_UNDYING, Items.EXPERIENCE_BOTTLE, Items.END_CRYSTAL, Items.GOLDEN_APPLE)
        val list = itemTypes.filter { getItemCount(it) > 0 }.map { ItemStack(it, getItemCount(it)) }

        val n6 = list.size
        for (j in 0 until n6) {
            val itemStack = list[j]
            val n7 = if (quad) (j % 2 * 20).toDouble() else if (horizontal) (j % 4 * 20).toDouble() else 0.0
            val n8 = if (quad) (j / 2 * 20).toDouble() else if (horizontal) (j / 4 * 20).toDouble() else (j * 20).toDouble()
            val indexPos = Vec2d(pos.x + n7 + 2, pos.y + n8 + 2)
            drawItem(itemStack, indexPos)
        }
        GlStateManager.popMatrix()


        w = dimension.x
        h = dimension.y
    }

    private fun drawItem(item: ItemStack, pos: Vec2d) {
        if (item.isEmpty) return
        RenderUtils2D.drawItem(item, pos.x, pos.y, drawOverlay = true, color = textColor)
    }

    private fun getItemCount(item: Item): Int {
        if (Helper.mc.player == null) return 0
        var n = 0
        val n2 = 44
        for (i in 0..n2) {
            val itemStack = Helper.mc.player.inventory.getStackInSlot(i)
            if (itemStack.item !== item) continue
            n += itemStack.count
        }
        return n
    }

    override fun getWidth() = w
    override fun getHeight() = h
}
