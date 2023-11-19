package com.curseclient.client.setting.type

import com.curseclient.client.setting.Setting
import com.curseclient.client.utility.extension.settingName
import kotlin.reflect.KProperty

class EnumSetting<T : Enum<T>>(
    name: String,
    private var value: T,
    visibility: () -> Boolean = { true },
    description: String = ""

): Setting<Any?> (name, visibility, description) {
    operator fun invoke(valueIn: T): () -> Boolean {
        return { value == valueIn }
    }
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = getValue()
    operator fun setValue(thisRef: Any?, property: KProperty<*>, v: T) = setValue(v)

    private val enumClass = value.declaringJavaClass
    private val enumValues = enumClass.enumConstants
    val names get() = enumValues.map { it.settingName }

    fun next() {
        value = enumValues[((value.ordinal + 1) % enumValues.size)]

        listeners.forEach { it() }
    }

    fun getValue() = value

    fun setValue(valueIn: T) {
        value = valueIn
        listeners.forEach { it() }
    }

    fun setByName(nameIn: String) {
        enumValues.firstOrNull { it.settingName.equals(nameIn, true) }?.let { value = it }
    }

    val valueName get() = value.settingName
}