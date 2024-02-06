package com.curseclient.client.manager

import com.curseclient.client.event.EventBus
import com.curseclient.client.manager.managers.*
import com.curseclient.client.manager.managers.data.DataManager

object ManagerLoader {

    fun load(){
        EventBus.subscribe(RotationManager)
        EventBus.subscribe(RadioManager)
        EventBus.subscribe(ScreenManager)
        EventBus.subscribe(DataManager)
        EventBus.subscribe(CommandManager)
        EventBus.subscribe(SongManager)
        EventBus.subscribe(FriendManager)
        EventBus.subscribe(ModuleManager)
        EventBus.subscribe(PacketManager)
        EventBus.subscribe(TimerManager)
        EventBus.subscribe(HotbarManager)
    }
}