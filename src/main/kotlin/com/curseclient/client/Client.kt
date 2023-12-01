package com.curseclient.client

object Client {
    const val NAME = "CurseClient"
    const val ID = "curseclient"
    const val VERSION = "0.2.1"

    const val displayName = "$NAME $VERSION"
    val chatName = "[ ${animatedRainbowText("CurseClient")}§f ]"

    // Will make better chat ...
    private fun animatedRainbowText(text: String): String {
        val rainbowColors = arrayOf("§c", "§6", "§e", "§a", "§b", "§9")
        val delay = 200 // Thay đổi độ mịn của rainbow bằng cách điều chỉnh độ trễ

        val currentMillis = System.currentTimeMillis()
        val index = ((currentMillis / delay) % text.length).toInt()

        var rainbowText = ""
        for ((i, char) in text.withIndex()) {
            val color = rainbowColors[(i + index) % rainbowColors.size]
            rainbowText += "$color$char"
        }

        return rainbowText
    }
}