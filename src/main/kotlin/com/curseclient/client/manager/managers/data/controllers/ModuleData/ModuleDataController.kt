package com.curseclient.client.manager.managers.data.controllers.ModuleData

import com.curseclient.CurseClient
import com.curseclient.client.Client
import com.curseclient.client.manager.managers.ModuleManager
import com.curseclient.client.manager.managers.data.DataController
import com.curseclient.client.manager.managers.data.DataUtils.getModuleByName
import com.curseclient.client.module.Module
import org.lwjgl.input.Keyboard
import java.io.File

object ModuleDataController : DataController(
    "ModuleDataController",
    File(CurseClient.DIR + "/modules.cr")
) {
    override fun onLoad() {
        if (!file.exists()) return
        val lines = file.readLines() as ArrayList<String>
        lines.removeIf { it.startsWith("//") }

        for(line in lines){
            try {
                val property = convertLineToData(line)
                if(property.settingName == "IsEnabled" && property.module.name != "ClickGui") property.module.setEnabled(property.settingValue.toBoolean())
                if(property.settingName == "Bind") property.module.key = Keyboard.getKeyIndex(property.settingValue)
            } catch (e: Exception) {
                //Error reading line. Skipping
            }
        }
    }

    override fun onWrite() {
        if (!file.exists()) file.createNewFile()

        var text = "//MODULE CONFIG, VERSION: " + Client.VERSION + System.lineSeparator()

        for (g in getText()) {
            text += g + System.lineSeparator()
        }

        file.writeText(text)
    }

    private fun getText(): ArrayList<String>{
        val output = ArrayList<String>()
        for (m in ModuleManager.getModules()) {
            output.add(m.name + "|" + "IsEnabled" + "|" + m.isEnabled())
            output.add(m.name + "|" + "Bind" + "|" + Keyboard.getKeyName(m.key))
        }
        return output
    }

    class ModuleDataObject(var module: Module, var settingName: String, var settingValue: String)
    private fun convertLineToData(line: String): ModuleDataObject {
        return ModuleDataObject(
            module = getModuleByName(line.split("|")[0]),
            settingName = line.split("|")[1],
            settingValue = line.split("|")[2]
        )
    }
}