package com.curseclient.client

import com.curseclient.client.command.ChatRedirector
import com.curseclient.client.event.EventBus
import com.curseclient.client.event.EventProcessor
import com.curseclient.client.event.events.CurseClientEvent
import com.curseclient.client.gui.GuiUtils
import com.curseclient.client.manager.ManagerLoader
import com.curseclient.client.manager.managers.FriendManager
import com.curseclient.client.manager.managers.ModuleManager
import com.curseclient.client.manager.managers.data.DataManager
import com.curseclient.client.utility.render.font.FontRenderer
import net.minecraftforge.common.MinecraftForge

object Loader {
    fun onPreLoad() {
        ModuleManager.load() // load modules
        ModuleManager.load2() // load hud modules

        ManagerLoader.load() // subscribe managers

        DataManager.onClientLoad() // load config
        FriendManager.loadConfig() // load friend list

        EventBus.post(CurseClientEvent.LoadEvent.PreInit())
    }

    fun onLoad() {
        MinecraftForge.EVENT_BUS.register(EventProcessor) // handle forge events
        EventBus.subscribe(ChatRedirector) // load commands

        EventBus.post(CurseClientEvent.LoadEvent.Init())
    }

    fun onPostLoad() {
        // load fonts
        FontRenderer.reloadFonts()

        GuiUtils.bootstrap() // load guis

        EventBus.post(CurseClientEvent.LoadEvent.PostInit())
    }
}