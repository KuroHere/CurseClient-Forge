package com.curseclient.client.setting.type

import com.curseclient.client.setting.Setting
import kotlin.reflect.KProperty

class BooleanSetting(
    name: String,
    var value: Boolean,
    visibility: () -> Boolean = { true },
    description: String = ""
): Setting<Any?>(name, visibility, description){
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = value
    operator fun setValue(thisRef: Any?, property: KProperty<*>, v: Boolean) {
        value = v
        listeners.forEach { it() }
    }

    fun toggle(){
        value = !value
        listeners.forEach { it() }
    }
}