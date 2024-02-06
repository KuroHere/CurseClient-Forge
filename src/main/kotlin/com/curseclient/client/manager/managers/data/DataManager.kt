package com.curseclient.client.manager.managers.data

import com.curseclient.CurseClient
import com.curseclient.client.event.events.ConnectionEvent
import com.curseclient.client.event.listener.listener
import com.curseclient.client.gui.impl.mainmenu.elements.alt.Account
import com.curseclient.client.manager.Manager
import com.curseclient.client.manager.managers.data.controllers.AltsDataController
import com.curseclient.client.manager.managers.data.controllers.HudData.HudModuleDataController
import com.curseclient.client.manager.managers.data.controllers.HudData.HudSettingsDataController
import com.curseclient.client.manager.managers.data.controllers.ModuleData.ModuleDataController
import com.curseclient.client.manager.managers.data.controllers.ModuleData.SettingsDataController
import java.io.File


object DataManager : Manager("DataManager") {
    private val controllers = ArrayList<DataController>()
    var alts: MutableList<Account> = ArrayList()

    init {
        listener<ConnectionEvent.Connect> {
            saveConfig()
        }

        listener<ConnectionEvent.Disconnect> {
            saveConfig()
        }

        controllers.add(HudModuleDataController)
        controllers.add(HudSettingsDataController)

        controllers.add(ModuleDataController)
        controllers.add(SettingsDataController)

        controllers.add(AltsDataController)
    }

    fun onClientLoad(){
        val dir = File(CurseClient.DIR)
        if (!dir.exists()) dir.mkdir()
        loadConfig()
    }

    fun saveConfig() {
        for(c in controllers){
            c.onWrite()
        }
    }

    private fun loadConfig() {
        for(c in controllers){
            c.onLoad()
        }
    }
}