package com.curseclient.client.manager.managers

import com.curseclient.CurseClient
import com.curseclient.client.manager.Manager
import com.sun.xml.internal.stream.Entity
import net.minecraft.entity.player.EntityPlayer
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap


object FriendManager: Manager("FriendManager") {
    private val file = File(CurseClient.DIR + "/friends.txt")
    private val friends = ArrayList<String>()
    private val players: Map<String, UUID> = ConcurrentHashMap()

    operator fun contains(player: Entity): Boolean {
        return if (player is EntityPlayer) {
            contains(player.getName())
        } else false
    }
    
    /**
     * Still here cause I'm too lazy to update plugins...
     */
    operator fun contains(player: EntityPlayer): Boolean {
        return contains(player.name)
    }

    operator fun contains(name: String): Boolean {
        return players.containsKey(name)
    }

    fun addFriend(name: String): Boolean{
        if (friends.all { !it.equals(name, true) }) {
            friends.add(name)
            saveConfig()
            return true
        }
        return false
    }

    fun removeFriend(name: String): Boolean{
        if(friends.contains(name)){
            friends.remove(name)
            saveConfig()
            return true
        }
        return false
    }

    fun getFriendList(): ArrayList<String> {
        return friends
    }

    fun EntityPlayer.isFriend(): Boolean {
        return friends.any { it.equals(this.displayNameString, true) }
    }

    fun loadConfig(){
        if (!file.exists()) return
        val lines = file.readLines() as ArrayList<String>
        friends.clear()
        for (l in lines) {
            if(l.length > 1) friends.add(l)
        }
    }

    private fun saveConfig(){
        if (!file.exists()) file.createNewFile()
        var text = ""
        for (f in friends) {
            text += f + System.lineSeparator()
        }
        file.writeText(text)
    }
}