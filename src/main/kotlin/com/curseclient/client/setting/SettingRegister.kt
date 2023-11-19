package com.curseclient.client.setting

import com.curseclient.client.module.HudModule
import com.curseclient.client.module.Module
import com.curseclient.client.setting.type.*
import java.awt.Color

/*SETTING REGISTERING*/

/*ENUM*/
fun <T: Enum<T>> Module.setting(
    name: String,
    value: T,
    visible: () -> Boolean = {true},
    description: String = ""
): EnumSetting<T> {
    val setting = EnumSetting(name, value, visibility = visible, description = description)
    settings.add(setting)
    return setting
}

/*DOUBLE*/
fun Module.setting(
    name: String,
    value: Double,
    min: Double,
    max: Double,
    step: Double,
    visible: () -> Boolean = {true},
    description: String = ""
): DoubleSetting {
    val setting = DoubleSetting(name, value, min, max, step, visibility = visible, description = description)
    settings.add(setting)
    return setting
}

/*Float*/
fun Module.setting(
    name: String,
    value: Float,
    min: Float,
    max: Float,
    step: Float,
    visible: () -> Boolean = {true},
    description: String = ""
): DoubleSetting {
    val setting = DoubleSetting(name, value.toDouble(), min.toDouble(), max.toDouble(), step.toDouble(), visibility = visible, description = description)
    settings.add(setting)
    return setting
}

/*Int*/
fun Module.setting(
    name: String,
    value: Int,
    min: Int,
    max: Int,
    step: Int,
    visible: () -> Boolean = {true},
    description: String = ""
): DoubleSetting {
    val setting = DoubleSetting(name, value.toDouble(), min.toDouble(), max.toDouble(), step.toDouble(), visibility = visible, description = description)
    settings.add(setting)
    return setting
}

/*Boolean*/
fun Module.setting(
    name: String,
    value: Boolean,
    visible: () -> Boolean = {true},
    description: String = ""
): BooleanSetting {
    val setting = BooleanSetting(name, value, visibility = visible, description = description)
    settings.add(setting)
    return setting
}

/*String*/
fun Module.setting(
    name: String,
    value: String,
    visible: () -> Boolean = {true},
    description: String = ""
): StringSetting {
    val setting = StringSetting(name, value, visibility = visible, description = description)
    settings.add(setting)
    return setting
}

/*Color*/
fun Module.setting(
    name: String,
    value: Color,
    visible: () -> Boolean = {true},
    description: String = ""
): ColorSetting {
    val setting = ColorSetting(name, value, visibility = visible, description = description)
    settings.add(setting)
    return setting
}

/*Unit*/
fun Module.setting(
    name: String,
    block: () -> Unit,
    visible: () -> Boolean = {true},
    description: String = ""
): UnitSetting {
    val setting = UnitSetting(name, block, visible, description = description)
    settings.add(setting)
    return setting
}

/*HUD SETTING REGISTERING*/

/*ENUM*/
fun <T: Enum<T>> HudModule.setting(
    name: String,
    value: T,
    visible: () -> Boolean = {true},
    description: String = ""
): EnumSetting<T> {
    val setting = EnumSetting(name, value, visibility = visible, description = description)
    settings.add(setting)
    return setting
}

/*DOUBLE*/
fun HudModule.setting(
    name: String,
    value: Double,
    min: Double,
    max: Double,
    step: Double,
    visible: () -> Boolean = {true},
    description: String = ""
): DoubleSetting {
    val setting = DoubleSetting(name, value, min, max, step, visibility = visible, description = description)
    settings.add(setting)
    return setting
}

/*Float*/
fun HudModule.setting(
    name: String,
    value: Float,
    min: Float,
    max: Float,
    step: Float,
    visible: () -> Boolean = {true},
    description: String = ""
): DoubleSetting {
    val setting = DoubleSetting(name, value.toDouble(), min.toDouble(), max.toDouble(), step.toDouble(), visibility = visible, description = description)
    settings.add(setting)
    return setting
}

/*Int*/
fun HudModule.setting(
    name: String,
    value: Int,
    min: Int,
    max: Int,
    step: Int,
    visible: () -> Boolean = {true},
    description: String = ""
): DoubleSetting {
    val setting = DoubleSetting(name, value.toDouble(), min.toDouble(), max.toDouble(), step.toDouble(), visibility = visible, description = description)
    settings.add(setting)
    return setting
}

/*Boolean*/
fun HudModule.setting(
    name: String,
    value: Boolean,
    visible: () -> Boolean = {true},
    description: String = ""
): BooleanSetting {
    val setting = BooleanSetting(name, value, visibility = visible, description = description)
    settings.add(setting)
    return setting
}

/*String*/
fun HudModule.setting(
    name: String,
    value: String,
    visible: () -> Boolean = {true},
    description: String = ""
): StringSetting {
    val setting = StringSetting(name, value, visibility = visible, description = description)
    settings.add(setting)
    return setting
}

/*Color*/
fun HudModule.setting(
    name: String,
    value: Color,
    visible: () -> Boolean = {true},
    description: String = ""
): ColorSetting {
    val setting = ColorSetting(name, value, visibility = visible, description = description)
    settings.add(setting)
    return setting
}

/*Unit*/
fun HudModule.setting(
    name: String,
    block: () -> Unit,
    visible: () -> Boolean = {true},
    description: String = ""
): UnitSetting {
    val setting = UnitSetting(name, block, visible, description = description)
    settings.add(setting)
    return setting
}

