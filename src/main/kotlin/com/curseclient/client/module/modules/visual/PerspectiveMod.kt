package com.curseclient.client.module.modules.visual

import baritone.api.utils.Helper.mc
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.Display

object PerspectiveMod : Module(
    "PerspectiveMod",
    "Free your look",
    Category.VISUAL
) {


    private var previousPerspective = 0
    @JvmField var perspectiveToggled = false
    @JvmField var cameraPitch = 0.0f
    @JvmField var cameraYaw = 0F

    fun overrideMouse(): Boolean {
        if (mc.inGameHasFocus && Display.isActive()) {
            if (!perspectiveToggled) {
                return true
            }

            mc.mouseHelper.mouseXYChange()
            val f1 = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F
            val f2 = f1 * f1 * f1 * 8.0F
            val f3 = mc.mouseHelper.deltaX.toFloat() * f2
            val f4 = mc.mouseHelper.deltaY.toFloat() * f2

            cameraYaw += f3 * 0.15F
            cameraPitch -= f4 * 0.15F

            if (cameraPitch > 90) cameraPitch = 90F
            if (cameraPitch < -90) cameraPitch = -90F
        }
        return false
    }

    fun resetPerspective() {
        perspectiveToggled = false
        mc.gameSettings.thirdPersonView = previousPerspective
    }


    override fun onEnable() {
        perspectiveToggled = !perspectiveToggled
        cameraYaw = mc.player.rotationYaw
        cameraPitch = mc.player.rotationPitch
        if (perspectiveToggled) {
            previousPerspective = mc.gameSettings.thirdPersonView
            mc.gameSettings.thirdPersonView = 1
        } else {
            mc.gameSettings.thirdPersonView = previousPerspective
        }
    }


    override fun onDisable() {
        super.onDisable()
        resetPerspective()
    }
}