package com.curseclient.client.manager.managers.data.controllers.HudData

import com.curseclient.CurseClient
import com.curseclient.client.Client
import com.curseclient.client.manager.managers.ModuleManager
import com.curseclient.client.manager.managers.data.DataController
import com.curseclient.client.manager.managers.data.DataUtils.getHudModuleByName
import com.curseclient.client.module.HudModule
import java.io.File

object HudModuleDataController : DataController(
    "ModuleDataController",
    File(CurseClient.DIR + "/HUDModules.cr")
) {
    override fun onLoad() {
        if (!file.exists()) return
        val lines = file.readLines() as ArrayList<String>
        lines.removeIf { it.startsWith("//") }

        for(line in lines){
            try {
                val property = convertLineToData(line)
                if(property.settingName == "IsEnabled" && property.module.name != "HudEditor") property.module.setEnabled(property.settingValue.toBoolean())
            } catch (e: Exception) {
                //Error reading line. Skipping
            }
        }
    }

    override fun onWrite() {
        if (!file.exists()) file.createNewFile()

        var text = "//HUD MODULE CONFIG, VERSION: " + Client.VERSION + System.lineSeparator()

        for (g in getText()) {
            text += g + System.lineSeparator()
        }

        file.writeText(text)
    }

    private fun getText(): ArrayList<String>{
        val output = ArrayList<String>()
        for (m in ModuleManager.getHudModules()) {
            output.add(m.name + "|" + "IsEnabled" + "|" + m.isEnabled())
        }
        return output
    }

    class ModuleDataObject(var module: HudModule, var settingName: String, var settingValue: String)
    private fun convertLineToData(line: String): ModuleDataObject {
        return ModuleDataObject(
            module = getHudModuleByName(line.split("|")[0]),
            settingName = line.split("|")[1],
            settingValue = line.split("|")[2]
        )
    }
}