package com.curseclient.client.module.modules.hud

import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import net.minecraft.item.ItemStack
import java.awt.Color
import kotlin.math.round

object Armor: DraggableHudModule(
    "Armor",
    "Draws armor slots into gui",
    HudCategory.HUD
) {

    private val mode by setting("Mode", Mode.Horizon)

    var w = 0.0
    var h = 0.0

    enum class Mode {
        Horizon,
        Vertical,
    }

    override fun onRender() {
        when (mode.name) {
            "Horizon" -> mc.player.armorInventoryList.reversed().forEachIndexed { index, item ->
                drawItem(item, pos.plus(20.0 * index, 0.0))
                w = 76.0
                h = 22.0
            }
            "Vertical" -> mc.player.armorInventoryList.reversed().forEachIndexed { index, item ->
                drawItem(item, pos.plus(0.0, 20.0 * index))
                w = 20.0
                h = 81.0
            }
        }
    }

    private fun drawItem(item: ItemStack, pos: Vec2d) {
        if (item.isEmpty) return

        RenderUtils2D.drawItem(item, pos.x, pos.y, drawOverlay = false)

        val dura = item.maxDamage - item.itemDamage
        val duraMultiplier = dura / item.maxDamage.toFloat()
        val hue = duraMultiplier * 0.3f

        val color = Color.getHSBColor(hue, 1f, 1f)
        val text = "${round(duraMultiplier * 100.0)}%"

        Fonts.DEFAULT.drawString(
            text,
            Vec2d(pos.x + 8.0 - Fonts.DEFAULT.getStringWidth(text, 0.75) / 2.0, pos.y + 18.0),
            color = color,
            scale = 0.75
        )
    }

    override fun getWidth() = w
    override fun getHeight() = h
}