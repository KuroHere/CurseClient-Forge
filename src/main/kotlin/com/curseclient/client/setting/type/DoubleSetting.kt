package com.curseclient.client.setting.type

import com.curseclient.client.setting.Setting
import kotlin.reflect.KProperty

class DoubleSetting(
    name: String,
    var value: Double,
    val min: Double,
    val max: Double,
    val step: Double,
    visibility: () -> Boolean = { true },
    description: String = ""

): Setting<Any?>(name, visibility, description){
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = value
    operator fun setValue(thisRef: Any?, property: KProperty<*>, v: Double) {
        value = v
        listeners.forEach { it() }
    }
}