package com.curseclient.client.manager.managers

import com.curseclient.client.command.Command
import com.curseclient.client.command.impl.*

object CommandManager {
    fun getCommands(): ArrayList<Command>{
        return arrayListOf(
            BindCommand,
            FriendCommand,
            HClipCommand,
            HelpCommand,
            ToggleCommand,
            VClipCommand
        )
    }

    fun getCommandByNameOrNull(name: String): Command?{
        var command: Command? = null
        for(cmd in getCommands()){
            if(cmd.name.equals(name, true)){
                command = cmd
                break
            }
        }
        return command
    }
}