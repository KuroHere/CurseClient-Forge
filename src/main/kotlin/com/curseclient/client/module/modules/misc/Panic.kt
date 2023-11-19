package com.curseclient.client.module.modules.misc

import com.curseclient.client.manager.managers.ModuleManager.getModules
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module


object Panic : Module(
    "Panic",
    "Toggles off all modules at once.",
    Category.MISC
) {

    override fun onEnable() {
        super.onEnable()
        for (m in getModules()) {
            m.setEnabled(false)
        }
    }

}