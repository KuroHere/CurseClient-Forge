package com.curseclient.client.manager.managers.data

import com.curseclient.client.manager.managers.ModuleManager
import com.curseclient.client.module.HudModule
import com.curseclient.client.module.Module

object DataUtils {
    fun getModuleByName(name: String): Module {
        return ModuleManager.getModules().first { it.name.equals(name, true) }
    }
    fun getHudModuleByName(name: String): HudModule {
        return ModuleManager.getHudModules().first { it.name.equals(name, true)}
    }
}