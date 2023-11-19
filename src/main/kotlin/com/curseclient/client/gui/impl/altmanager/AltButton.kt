package com.curseclient.client.gui.impl.altmanager

import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.gui.Gui
import java.awt.Color

class AltButton(val email: String, val password: String, val altType: Alt.AltType, var x: Float, val y: Float, val width: Float, val height: Float) {
    
    fun drawScreen(mouseX: Int, mouseY: Int) {
        Gui.drawRect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt(), if (altType.equals(Alt.AltType.MICROSOFT)) Color.CYAN.rgb else Color.RED.rgb)
        Gui.drawRect((x + 0.5f).toInt(), (y + 0.5f).toInt(), (x + width - 0.5f).toInt(), (y + height - 0.5f).toInt(), Color(18, 18, 18).rgb)
        Fonts.DEFAULT.drawString(email, Vec2d((x + 5.0f).toDouble(), (y + 5.0f).toDouble()), color =  Color.WHITE)
        val string = StringBuilder()
        for (i in password.indices) {
            string.append("*")
        }
        Fonts.DEFAULT.drawString(string.toString(), Vec2d((x + 5.0f).toDouble(), y + 17.5), color =  Color.WHITE)
        Gui.drawRect((x + 5.0f).toInt(), (y + height - 17.5f).toInt(), (x + width / 2.0f - 5.0f).toInt(), (y + height - 2.5f).toInt(), Color(15, 15, 15).rgb)
        Fonts.DEFAULT.drawString("Login", Vec2d(x + 2.5 + (width / 2.0f - 5.0f) / 2.0 - Fonts.DEFAULT.getStringWidth("Login") / 2.0f, y + height - 7.5f - Fonts.DEFAULT.getHeight() / 2.0f), color =  Color.WHITE)
        if (mouseX > x + 5.0f && mouseX < x + width / 2.0f - 5.0f && mouseY > y + height - 17.5f && mouseY < y + height - 2.5f) {
            Gui.drawRect((x + 5.0f).toInt(), (y + height - 17.5f).toInt(), (x + width / 2.0f - 5.0f).toInt(), (y + height - 2.5f).toInt(), Color(0, 0, 0, 50).rgb)
        }
        Gui.drawRect((x + width / 2.0f + 2.5f).toInt(), (y + height - 17.5f).toInt(), (x + width - 5.0f).toInt(), (y + height - 2.5f).toInt(), Color(15, 15, 15).rgb)
        if (mouseX > x + width / 2.0f + 2.5f && mouseX < x + width - 5.0f && mouseY > y + height - 17.5f && mouseY < y + height - 2.5f) {
            Gui.drawRect((x + width / 2.0f + 2.5f).toInt(), (y + height - 17.5f).toInt(), (x + width - 5.0f).toInt(), (y + height - 2.5f).toInt(), Color(0, 0, 0, 50).rgb)
        }
        Fonts.DEFAULT.drawString("Delete", Vec2d(x + width / 2.0f + 2.5f + (width / 2.0f - 5.0f) / 2.0f - Fonts.DEFAULT.getStringWidth("Delete") / 2.0f, y + height - 7.5f - Fonts.DEFAULT.getHeight() / 2.0f), color = Color.RED)
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {

        if (mouseButton != 0) {
            return
        }
        if (mouseX > x + 5.0f && mouseX < x + width / 2.0f - 5.0f && mouseY > y + height - 17.5f && mouseY < y + height - 2.5f) {
            Alt(email, password, altType).login() //why no work?
        }
        if (mouseX > x + width / 2.0f + 2.5f && mouseX < x + width - 5.0f && mouseY > y + height - 17.5f && mouseY < y + height - 2.5f) {
            AltGui.altButtons.remove(this)
            AltGui.updateButtons()
        }
    }
}
