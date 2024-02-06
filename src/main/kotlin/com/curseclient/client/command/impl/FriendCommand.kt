package com.curseclient.client.command.impl

import com.curseclient.client.command.Command
import com.curseclient.client.manager.managers.FriendManager
import com.curseclient.client.utility.player.ChatUtils.sendMessage

object FriendCommand: Command(
    "friend",
    "Allows you to manage friends",
    ".friend add name / .friend remove name / .friend list"

) {
    override fun onExecute(args: ArrayList<String>) {
        if(!(args.size == 2 || args.size == 1)) {
            onBadUsage()
            return
        }

        when(args[0]){
            "add" -> {
                if(args.size != 2){
                    onBadUsage()
                    return
                }

                val command2 = args[1]
                if(FriendManager.addFriend(command2)){
                    sendMessage("New friend: $command2")
                } else {
                    sendMessage("$command2 is already your friend.")
                }
            }

            "remove" -> {
                if(args.size != 2){
                    onBadUsage()
                    return
                }

                val command2 = args[1]
                if(FriendManager.removeFriend(command2)){
                    sendMessage("Removed $command2 from friend list")
                } else {
                    sendMessage("$command2 is not your friend.")
                }
            }

            "list" -> {
                if(args.size != 1){
                    onBadUsage()
                    return
                }

                var text = "Friend list: "
                for(f in FriendManager.getFriendList()){
                    text += "$f, "
                }
                text.dropLast(2)
                sendMessage(text)
            }
        }


    }
}