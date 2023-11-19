package com.curseclient.client.setting.type

import com.curseclient.client.setting.Setting
import kotlin.reflect.KProperty

class StringSetting(
    name: String,
    var value: String,
    visibility: () -> Boolean = { true },
    description: String = ""

): Setting<Any?>(name, visibility, description){
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = value
    operator fun setValue(thisRef: Any?, property: KProperty<*>, v: String) {
        value = v
        listeners.forEach { it() }
    }
}