package com.curseclient.client.module.impls.visual

import com.curseclient.client.event.events.entity.EntityHighlightOnHitEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.ColorUtils.a
import com.curseclient.client.utility.render.ColorUtils.b
import com.curseclient.client.utility.render.ColorUtils.g
import com.curseclient.client.utility.render.ColorUtils.r
import java.awt.Color

object HitColour : Module(
    "DamageTint",
    "Change the colour entities are rendered in when hit",
    Category.VISUAL
) {

    init {
        val colour by setting("Colour", Color(0, 100, 255, 85), description = "The highlight colour")
        safeListener<EntityHighlightOnHitEvent> {
            it.cancel()
            it.colour = Color(colour.r, colour.g, colour.b, colour.a)
        }
    }
}