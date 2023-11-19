package com.curseclient.client.command.commands

import com.curseclient.client.command.Command
import com.curseclient.client.utility.player.ChatUtils.sendMessage
import com.curseclient.client.utility.player.MovementUtils
import kotlin.math.cos
import kotlin.math.sin

object HClipCommand: Command(
    "hclip",
    "Changes your position",
    ".hclip DISTANCE"
) {
    override fun onExecute(args: ArrayList<String>) {
        if(args.size != 1){
            onBadUsage()
            return
        }

        val dist: Double
        val dir = MovementUtils.calcMoveRad()

        try {
            dist = args[0].toDouble()
        } catch (e: Exception){
            sendMessage("${args[0]} is not a valid number.")
            return
        }

        mc.player.setPosition(mc.player.posX + -sin(dir) * dist, mc.player.posY, mc.player.posZ + cos(dir) * dist)
    }
}