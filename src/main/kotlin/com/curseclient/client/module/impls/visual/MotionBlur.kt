package com.curseclient.client.module.impls.visual

import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.gui.api.elements.Element
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.misc.NotificationType
import com.curseclient.client.utility.misc.NotificationUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.client.event.RenderGameOverlayEvent
import java.awt.Color
import java.lang.reflect.Field

// TODO: fix minecraft game overlays not appeared when MotionBlur enable
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

    init {
        safeListener<RenderGameOverlayEvent.Pre> {
            if (it.type == RenderGameOverlayEvent.ElementType.HOTBAR) return@safeListener
        }
    }

    override fun onEnable() {
        if (shouldDisableModule()) {
            super.setEnabled(false)
            return
        }
    }

    private fun shouldDisableModule() = mc.player == null || mc.world == null || fastRenderCheck()

    private fun fastRenderCheck(): Boolean {
        if (isFastRenderEnabled()) {
            if (mc.player != null || mc.world != null) {
                NotificationUtils.notify("MotionBlur", "Motion Blur is not compatible with OptiFine's Fast Render.", NotificationType.ERROR, descriptionColor = Color.RED)
            }
            return true
        }
        return false
    }

    private fun isFastRenderEnabled() = cachedFastRender?.let {
        try {
            it.getBoolean(mc.gameSettings)
        } catch (ignored: Exception) {
            false
        }
    } ?: false
}
