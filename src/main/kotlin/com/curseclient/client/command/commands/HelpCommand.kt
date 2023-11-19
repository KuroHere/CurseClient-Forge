package com.curseclient.client.command.commands

import com.curseclient.client.command.Command
import com.curseclient.client.manager.managers.CommandManager
import com.curseclient.client.manager.managers.CommandManager.getCommandByNameOrNull
import com.curseclient.client.utility.player.ChatUtils.sendMessage

object HelpCommand: Command(
    "help",
    "Shows help",
    ".help / .help command"
) {
    override fun onExecute(args: ArrayList<String>) {
        if(args.size > 1) {
            onBadUsage()
            return
        }

        if(args.isEmpty()){
            for (cmd in CommandManager.getCommands()){
                sendMessage("${cmd.name.uppercase()} - ${cmd.description}")
            }
            return
        }
        if(args.size == 1){
            val cmd = getCommandByNameOrNull(args[0])

            if(cmd == null){
                sendMessage("${args[0]} is not a valid command.")
                return
            }

            sendMessage("${cmd.name.uppercase()}:  ${cmd.usage}")
        }
    }
}