package com.curseclient.client.command

import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.managers.CommandManager
import com.curseclient.client.utility.player.ChatUtils
import net.minecraftforge.client.event.ClientChatEvent

object ChatRedirector {
    init {
        safeListener<ClientChatEvent> {
            if(it.message.startsWith(".")){
                it.isCanceled = true
                mc.ingameGUI.chatGUI.addToSentMessages(it.message)

                val messageProcessed = it.message.drop(1)
                if (messageProcessed == "") {
                    ChatUtils.sendMessage("Bad usage, try .command arg1 arg2...")
                    return@safeListener
                }

                val messageSlit = messageProcessed.split(" ")

                var isCommandValid = false

                for (cmd in CommandManager.getCommands()) {
                    if(cmd.name.equals(messageSlit[0], true)){
                        val args = ArrayList<String>()
                        for(l in messageSlit.drop(1)){
                            args.add(l)
                        }
                        cmd.onExecute(args)

                        isCommandValid = true
                        break
                    }
                }

                if(!isCommandValid) ChatUtils.sendMessage("Command ${messageSlit[0]} does not exist. Type .help")
            }
        }
    }
}