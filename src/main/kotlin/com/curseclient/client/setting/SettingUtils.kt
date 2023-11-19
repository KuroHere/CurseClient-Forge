package com.curseclient.client.setting

import com.curseclient.client.module.HudModule
import com.curseclient.client.module.Module

inline fun <reified T : Setting<*>> Module.getSetting(name: String): T? {
    return settings.filterIsInstance<T>().firstOrNull { it.name.equals(name, ignoreCase = true) }
}

inline fun <reified T : Setting<*>> Module.getSettingNotNull(name: String): T {
    return settings.filterIsInstance<T>().firstOrNull { it.name.equals(name, ignoreCase = true) }!!
}

inline fun <reified T : Setting<*>> HudModule.getHudSetting(name: String): T? {
    return settings.filterIsInstance<T>().firstOrNull { it.name.equals(name, ignoreCase = true) }
}

inline fun <reified T : Setting<*>> HudModule.getHudSettingNotNull(name: String): T {
    return settings.filterIsInstance<T>().firstOrNull { it.name.equals(name, ignoreCase = true) }!!
}