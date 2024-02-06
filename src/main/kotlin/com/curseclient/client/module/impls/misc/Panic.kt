package com.curseclient.client.module.impls.misc

import com.curseclient.client.manager.managers.ModuleManager.getModules
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting

// For real 💀
object Panic : Module(
    "Panic",
    "Toggles on/off all impls at once. [ for real ?]",
    Category.MISC
) {
    private val reverse by setting("Reverse", false)

    override fun onEnable() {
        for (m in getModules()) {
            if (reverse)
                m.setEnabled(true)
            else
                m.setEnabled(false)
        }
    }

}