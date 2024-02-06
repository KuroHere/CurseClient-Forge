package com.curseclient.client.module.impls.hud

import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.RenderUtils2D

object Model: DraggableHudModule(
    "PlayerModel",
    "Show player model in hud",
    HudCategory.HUD
) {
    private val modelScale by setting("ModelScale", 0.8, 0.4, 2.0, 0.1)

    private const val w = 90
    private const val h = 150
    override fun onRender() {
        RenderUtils2D.drawPlayer(mc.player, modelScale.toFloat(), (pos.x + getWidth() * 0.5).toFloat(), (pos.y + getHeight()).toFloat())
    }

    override fun getWidth() = w * modelScale * 0.65
    override fun getHeight() = h * modelScale * 0.65
}