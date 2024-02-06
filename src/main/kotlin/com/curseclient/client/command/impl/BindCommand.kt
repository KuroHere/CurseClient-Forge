package com.curseclient.client.command.impl

import com.curseclient.client.command.Command
import com.curseclient.client.manager.managers.ModuleManager
import com.curseclient.client.utility.player.ChatUtils.sendMessage
import org.lwjgl.input.Keyboard

object BindCommand: Command(
    "bind",
    "Allows you to bind a module",
    ".bind MODULE KEY"
) {
    override fun onExecute(args: ArrayList<String>) {
        if(args.size != 2) {
            onBadUsage()
            return
        }
        val module = ModuleManager.getModules().firstOrNull { it.name.equals(args[0], true) }
        val key = Keyboard.getKeyIndex(args[1].uppercase())

        if (module == null){
            sendMessage("${args[0]} is not a valid module.")
            return
        }

        if (key == Keyboard.KEY_NONE && !args[1].equals("none", true)) {
            sendMessage("${args[1]} is not a valid keybind.")
            return
        }

        module.key = key

        sendMessage("New ${module.name} bind: " + Keyboard.getKeyName(key))
    }
}