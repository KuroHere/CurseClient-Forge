package com.curseclient.client.module.modules.client

import com.curseclient.client.event.events.render.Render2DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.gui.ScaledResolution
import net.minecraftforge.event.world.WorldEvent
import org.lwjgl.input.Keyboard
import java.awt.Color

object Welcome: Module(
    "Welcome",
    "Fact about client, some credit, info,...",
    Category.CLIENT,
    alwaysListenable = true
) {
    private val show by setting("Don't show again", false)

    init {
        safeListener<Render2DEvent> {
            if (!show) {
                Welcome.setEnabled(true)
                val sr = ScaledResolution(mc)
                val rectTop = Vec2d(0, sr.scaledHeight / 3)
                val rectBottom = Vec2d(sr.scaledWidth, (sr.scaledHeight / 1.5).toInt())

                RectBuilder(rectTop, rectBottom).apply {
                    color(Color(35, 35, 35, 100))
                    draw()
                }

                val frb = Fonts.DEFAULT_BOLD
                val fr = Fonts.DEFAULT

                val welcome = "Welcome to CurseClient"
                val key = "Default ClickGui keybind is: " + Keyboard.getKeyName(ClickGui.key).toString()
                val prefix = "Prefix is . and its can't be changed [fk kuro]"

                val welcomePos = Vec2d(sr.scaledWidth / 2.0 - frb.getStringWidth(welcome, 2.0) / 2, sr.scaledHeight / 3 + 10.0)
                val keyPos = Vec2d(sr.scaledWidth / 2.0 - fr.getStringWidth(key, 1.0) / 2, sr.scaledHeight / 3 + frb.getHeight(2.0) + 4)
                val prefixPos = Vec2d(sr.scaledWidth / 2.0 - fr.getStringWidth(prefix, 1.0) / 2, sr.scaledHeight / 3 + frb.getHeight(2.0) + fr.getHeight(1.0) + 4)

                frb.drawString(welcome, welcomePos, scale = 2.0)
                fr.drawString(key, keyPos, color = Color(220, 220, 220, 230), scale = 1.0)
                fr.drawString(prefix, prefixPos, color = Color(220, 220, 220, 230), scale = 1.0)
            }
        }
    }
}