package com.curseclient.client.event.events

import com.curseclient.client.event.Event
import com.curseclient.client.module.HudModule
import com.curseclient.client.module.Module
import com.curseclient.client.utility.NotificationInfo

abstract class CurseClientEvent : Event {
    class NotificationEvent(val notification: NotificationInfo) : CurseClientEvent()
    class ModuleToggleEvent(val module: Module) : CurseClientEvent()
    class HudModuleToggleEvent(val module: HudModule): CurseClientEvent()

    abstract class LoadEvent : CurseClientEvent() {
        class PreInit : LoadEvent()
        class Init : LoadEvent()
        class PostInit : LoadEvent()
    }
}