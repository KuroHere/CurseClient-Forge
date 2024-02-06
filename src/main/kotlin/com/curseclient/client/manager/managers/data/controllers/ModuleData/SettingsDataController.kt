package com.curseclient.client.manager.managers.data.controllers.ModuleData

import com.curseclient.CurseClient
import com.curseclient.client.Client
import com.curseclient.client.event.listener.runTrying
import com.curseclient.client.manager.managers.ModuleManager
import com.curseclient.client.manager.managers.data.DataController
import com.curseclient.client.manager.managers.data.DataUtils.getModuleByName
import com.curseclient.client.module.Module
import com.curseclient.client.setting.getSetting
import com.curseclient.client.setting.type.*
import java.awt.Color
import java.io.File

object SettingsDataController : DataController(
    "SettingsDataController",
    File(CurseClient.DIR + "/settings.cr")
) {
    override fun onLoad() {
        if (!file.exists()) return
        val lines = file.readLines() as ArrayList<String>
        lines.removeIf { it.startsWith("//") }

        for(line in lines){
            runTrying {
                val property = convertLineToData(line)
                when(property.type) {
                    SettingType.ENUM -> property.module.getSetting<EnumSetting<*>>(property.settingName)?.setByName(property.settingValue)
                    SettingType.DOUBLE -> property.module.getSetting<DoubleSetting>(property.settingName)?.value = property.settingValue.toDouble()
                    SettingType.BOOLEAN -> property.module.getSetting<BooleanSetting>(property.settingName)?.value = property.settingValue.toBoolean()
                    SettingType.STRING -> property.module.getSetting<StringSetting>(property.settingName)?.value = property.settingValue
                    SettingType.COLOR -> property.module.getSetting<ColorSetting>(property.settingName)?.setColor(Color(property.settingValue.toInt(), true))
                }
            }
        }
    }

    override fun onWrite() {
        if (!file.exists()) file.createNewFile()

        var text = "//SETTINGS CONFIG, VERSION: " + Client.VERSION + System.lineSeparator()

        for (g in getText()) {
            text += g + System.lineSeparator()
        }

        file.writeText(text)
    }

    private fun getText(): ArrayList<String>{
        val output = ArrayList<String>()
        for (m in ModuleManager.getModules()) {
            for (s in m.settings) {
                if (s is EnumSetting<*>) output.add(m.name + "|" + "EnumSetting" + "|" + s.name + "|" + s.valueName)
                if (s is DoubleSetting) output.add(m.name + "|" + "DoubleSetting" + "|" + s.name + "|" + s.value.toString())
                if (s is BooleanSetting) output.add(m.name + "|" + "BooleanSetting" + "|" + s.name + "|" + (s.value.toString()))
                if (s is StringSetting) output.add(m.name + "|" + "StringSetting" + "|" + s.name + "|" + (s.value))
                if (s is ColorSetting) output.add(m.name + "|" + "ColorSetting" + "|" + s.name + "|" + s.getColor().hashCode().toString())
            }
        }
        return output
    }

    enum class SettingType { ENUM, DOUBLE, BOOLEAN, STRING, COLOR }

    class SettingDataObject(var module: Module, var type: SettingType, var settingName: String, var settingValue: String)
    private fun convertLineToData(line: String): SettingDataObject {
        val dataType =
            when(line.split("|")[1]){
                "EnumSetting" -> SettingType.ENUM
                "DoubleSetting" -> SettingType.DOUBLE
                "StringSetting" -> SettingType.STRING
                "ColorSetting" -> SettingType.COLOR
                else -> SettingType.BOOLEAN
            }
        return SettingDataObject(
            module = getModuleByName(line.split("|")[0]),
            type = dataType,
            settingName = line.split("|")[2],
            settingValue = line.split("|")[3]
        )
    }


}