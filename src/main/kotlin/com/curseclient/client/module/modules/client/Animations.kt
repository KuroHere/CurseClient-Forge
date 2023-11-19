package com.curseclient.client.module.modules.client

import com.curseclient.client.event.listener.Nameable
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting

object Animations: Module(
    "Animations",
    "Manage your animation",
    Category.CLIENT
) {
    // gui animations
    val guiAnimations by setting("Container-Animation", Mode.None)
    val vSlideValue by setting("Slide-Vertical", VMode.Downward, { guiAnimations.equals("slide") })
    val hSlideValue by setting("Slide-Horizontal",HMode.Right, { guiAnimations.equals("slide") })
    val animTimeValue by setting("Container-AnimTime", 750, 0, 3000, 1, { !guiAnimations.equals("none") })
    val tabAnimations by setting("Tab-Animation", TabMode.Zoom)

    enum class Mode(override val displayName: String): Nameable {
        None("None"),
        Zoom("Zoom"),
        Slide("Slide"),
        Smooth("Smooth")
    }
    enum class VMode(override val displayName: String): Nameable {
        None("None"),
        Upward("Upward"),
        Downward("Downward")
    }

    enum class HMode(override val displayName: String): Nameable {
        None("None"),
        Right("Right"),
        Left("Left")
    }

    enum class TabMode(override val displayName: String): Nameable {
        None("None"),
        Zoom("Zoom"),
        Slide("Slide")
    }
}