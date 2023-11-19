package com.curseclient.client.module.modules.hud

import baritone.api.utils.Helper.mc
import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
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
    override fun onRender() {
        mc.player.armorInventoryList.reversed().forEachIndexed { index, item ->
            drawItem(item, pos.plus(20.0 * index, 0.0))
        }
    }

    private fun drawItem(item: ItemStack, p: Vec2d) {
        if (item.isEmpty) return

        RenderUtils2D.drawItem(item, p.x, p.y, drawOverlay = false)

        val dura = item.maxDamage - item.itemDamage
        val duraMultiplier = dura / item.maxDamage.toFloat()
        val hue = duraMultiplier * 0.3f

        val color = Color.getHSBColor(hue, 1f, 1f)
        val text = "${round(duraMultiplier * 100.0)}%"

        Fonts.DEFAULT.drawString(
            text,
            Vec2d(p.x + 8.0 - Fonts.DEFAULT.getStringWidth(text, 0.75) / 2.0, p.y + 18.0),
            color = color,
            scale = 0.75
        )
    }

    override fun getWidth() = 76.0
    override fun getHeight() = 22.0
}