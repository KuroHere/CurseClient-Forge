package com.curseclient.client.module.impls.misc

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting

object ChatMod: Module(
    "ChatMod",
    "Customize chat",
    Category.MISC
) {

    val barAnimation by setting("Bar Animation", false);
    val smooth by setting("Smooth", true);
    val smoothSpeed by setting("Smooth Speed", 4, 1, 10, 1);
    val transparent by setting("Transparent background", true);
}