package com.curseclient.client.module.impls.client

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import java.awt.GraphicsEnvironment

object FontSettings : Module(
    "FontSettings",
    "Configuration for fonts",
    Category.CLIENT,
    enabledByDefault = true
) {

    val shadow by setting("Shadow", true)
    val shadowShift by setting("Shadow Shift", 4.5, 1.0, 10.0, 0.05, { shadow })
    private val gapSetting by setting("Gap", 1.5, -10.0, 10.0, 0.5)
    private val baselineOffsetSetting by setting("Vertical Offset", -3.0, -10.0, 10.0, 0.5)
    private val lodBiasSetting by setting("Smoothing", -1.0, -10.0, 10.0, 0.5)

    val size get() = 1.0f * 0.12f
    val gap get() = gapSetting * 0.5f - 0.8f
    val lineSpace get() = size * (0.8 * 0.05f + 0.77f)
    val lodBias get() = lodBiasSetting * 0.25f - 0.5f
    val baselineOffset get() = baselineOffsetSetting * 2.0f - 4.5f

    var availableFonts: Map<String, String> = emptyMap()

    fun updateSystemFonts() {
        availableFonts = HashMap<String, String>().apply {
            val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()

            environment.availableFontFamilyNames.forEach {
                this[it.lowercase()] = it
            }

            environment.allFonts.forEach {
                this[it.name.lowercase()] = it.family
            }
        }
    }
}