package com.curseclient.client.setting.type

import com.curseclient.client.setting.Setting
import kotlin.reflect.KProperty

class UnitSetting(
    name: String,
    var block: () -> Unit,
    visibility: () -> Boolean = { true },
    description: String = ""
): Setting<Any?>(name, visibility, description){
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = block
    operator fun setValue(thisRef: Any?, property: KProperty<*>, v: () -> Unit) {
        block = v
        listeners.forEach { it() }
    }

    fun invokeBlock(){
        block.invoke()
    }

}