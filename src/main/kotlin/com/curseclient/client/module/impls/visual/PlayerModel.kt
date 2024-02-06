package com.curseclient.client.module.impls.visual

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting

object PlayerModel: Module(
    "PlayerModel",
    "Manage player model",
    Category.VISUAL
) {
    val customSize by setting("CustomSize", false)
    val size by setting("Size",  1.0, 0.01, 5.0, 0.01)
    var sneak by setting("Sneak", false)
    var limbSwing by setting("LimbSwing", false)
    var rotationPitch by setting("RotationPitch", false)
    var rotationYaw by setting("RotationYaw", false)
    var rotationYawHead by setting("RotationYawHead", false)
    var swingProgress by setting("SwingProgress", false)
    var cameraPitch by setting("CameraPitch", false)
}