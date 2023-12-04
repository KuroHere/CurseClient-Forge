package com.curseclient.client.module.modules.visual

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.hud.Notifications
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.NotificationType
import com.curseclient.client.utility.NotificationUtils
import java.awt.Color
import java.lang.reflect.Field


// TODO: fix minecraft game overlays not appeared when MotionBlur enable
// I hate mixin
object MotionBlur : Module(
    "MotionBlur",
    "Add motion to your visual",
    Category.VISUAL) {

    val amount by setting("Amount", 1.0, 1.0, 8.0, 1.0)

    private val cachedFastRender: Field? = try {
        mc.gameSettings.javaClass.getDeclaredField("ofFastRender")
    } catch (ignored: Exception) {
        null
    }

    override fun onEnable() {
        if (shouldDisableModule()) {
            super.setEnabled(false)
            return
        }
    }

    private fun shouldDisableModule(): Boolean {
        return mc.player == null || mc.world == null || fastRenderCheck()
    }

    private fun fastRenderCheck(): Boolean {
        if (isFastRenderEnabled()) {
            if (mc.player != null || mc.world != null) {
                NotificationUtils.notify("MotionBlur", "Motion Blur is not compatible with OptiFine's Fast Render.", NotificationType.ERROR, descriptionColor = Color.RED)
            }
            return true
        }
        return false
    }

    private fun isFastRenderEnabled(): Boolean {
        return cachedFastRender?.let {
            try {
                it.getBoolean(mc.gameSettings)
            } catch (ignored: Exception) {
                false
            }
        } ?: false
    }
}