package com.curseclient.client.gui.impl.mcgui

import com.curseclient.client.utility.render.font.FontRenderer
import com.curseclient.client.utility.render.font.Fonts
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.time.ZoneId

class DateTimeDisplay {

    private val date: Long = System.currentTimeMillis()
    private val zoneId: ZoneId = ZoneId.systemDefault()

    fun drawDateTime(fonts: FontRenderer, width: Int, shadow: Boolean, color: Color) {
        drawFormattedDate(fonts, width, shadow, color)
        drawZoneId(fonts, width, shadow, color)
    }

    private fun drawFormattedDate(fonts: FontRenderer, width: Int, shadow: Boolean, color: Color) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy")
        val formattedDate = dateFormat.format(Date(date))

        fonts.drawString(
            formattedDate,
            width - fonts.getStringWidth(formattedDate, Fonts.PROTOTYPE, 1F) - 5,
            20f,
            shadow,
            color,
            1F,
            Fonts.PROTOTYPE
        )
    }

    private fun drawZoneId(fonts: FontRenderer, width: Int, shadow: Boolean, color: Color) {
        fonts.drawString(
            zoneId.id,
            width - fonts.getStringWidth(zoneId.id, Fonts.PROTOTYPE, 1F) - 5,
            10f,
            shadow,
            color,
            1F,
            Fonts.PROTOTYPE
        )
    }
}