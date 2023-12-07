package com.curseclient.client.module

import com.curseclient.client.gui.GuiUtils
import com.curseclient.client.gui.api.other.MouseAction
import com.curseclient.client.module.modules.client.HudEditor
import com.curseclient.client.setting.getHudSetting
import com.curseclient.client.setting.getHudSettingNotNull
import com.curseclient.client.setting.setting
import com.curseclient.client.setting.type.DoubleSetting
import com.curseclient.client.setting.type.EnumSetting
import com.curseclient.client.setting.type.UnitSetting
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.ColorUtils.setAlphaD
import com.curseclient.client.utility.render.ColorUtils.toColor
import com.curseclient.client.utility.render.DockingH
import com.curseclient.client.utility.render.DockingV
import com.curseclient.client.utility.render.HoverUtils.isHovered
import com.curseclient.client.utility.render.Screen
import com.curseclient.client.utility.render.animation.SimpleAnimation
import com.curseclient.client.utility.render.font.BonIcon
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import java.awt.Color

abstract class DraggableHudModule(
    name: String,
    description: String,
    category: HudCategory,
    alwaysListenable: Boolean = false,
) : HudModule(name, description, category, alwaysListenable) {
    var isDragging = false
    var dragX = 0.0
    var dragY = 0.0
    private val animation = SimpleAnimation(0.0f)

    init {
        settings.add(UnitSetting("Reset Position", {
            getHudSetting<EnumSetting<*>>("DockingH")?.setByName("Left")
            getHudSetting<EnumSetting<*>>("DockingV")?.setByName("Top")

            x = getDefaultPos().x
            y = getDefaultPos().y
        }))

        settings.add(DoubleSetting("X", 5.0, 0.0, 0.0, 1.0, { false }))
        settings.add(DoubleSetting("Y", 5.0, 0.0, 0.0, 1.0, { false }))


        val h = setting("DockingH", DockingH.LEFT)
        val v = setting("DockingV", DockingV.TOP)
        h.listeners.add { updatePosByDocking() }
        v.listeners.add { updatePosByDocking() }
    }

    private var x by getHudSettingNotNull<DoubleSetting>("X")
    private var y by getHudSettingNotNull<DoubleSetting>("Y")

    var dockingH by getHudSettingNotNull<EnumSetting<DockingH>>("DockingH")
    var dockingV by getHudSettingNotNull<EnumSetting<DockingV>>("DockingV")

    fun onRenderPre() {
        if (!HudEditor.isEnabled() || !isDragging || !isEnabled()) return
        val cornerPos = Vec2d(dockingH.modifier * Screen.width / 2.0, dockingV.modifier * Screen.height / 2.0)
        pos = GuiUtils.hudEditorGui!!.mouse.minus(dragX, dragY).minus(cornerPos)

    }

    fun onRenderPost() {
        if (!HudEditor.isEnabled() || !isEnabled()) return

        animation.setAnimation(100f, 12.0)

        RectBuilder(pos, pos.plus(getWidth(), getHeight())).apply {
            color(Color.WHITE.setAlpha((0.1 * animation.value / 500).toInt()))
            outlineColor(Color.WHITE)
            radius(2.0)
            width(0.8)
            draw()
        }
        if (animation.value > 0.1f)
            Fonts.BonIcon20.drawString(BonIcon.LEFT_MOUSE, Vec2d(pos.x + getWidth(), pos.y + getHeight()), true, ColorUtils.toRGBA(255, 255, 255, (animation.value).toInt() / 100).toColor())

    }

    fun handleMouseAction(mouse: Vec2d, action: MouseAction, button: Int) {
        when (action) {
            MouseAction.CLICK -> {
                if (button != 0) return
                if (isHovered(mouse, pos, pos.plus(getWidth(), getHeight()))) {
                    dragX = mouse.x - pos.x
                    dragY = mouse.y - pos.y
                    isDragging = true
                }
            }

            MouseAction.RELEASE -> {
                isDragging = false
            }
        }
    }

    protected var pos: Vec2d
        get() = getPosByDocking()
        set(value) { x = value.x; y = value.y }

    open fun getWidth(): Double { return 10.0 }
    open fun getHeight(): Double { return 10.0 }

    open fun getDefaultPos(): Vec2d {
        return Vec2d(5.0, 5.0)
    }

    private fun getPosByDocking(): Vec2d {
        val cornerPos = Vec2d(dockingH.modifier * Screen.width / 2.0, dockingV.modifier * Screen.height / 2.0)
        return Vec2d(x, y).plus(cornerPos)
    }

    private fun updatePosByDocking() {
        val cornerPos = Vec2d(dockingH.modifier * Screen.width / 2.0, dockingV.modifier * Screen.height / 2.0)
        x = (Screen.width / 2.0 - getWidth()) * dockingH.modifier - cornerPos.x
        y = (Screen.height / 2.0 - getHeight()) * dockingV.modifier - cornerPos.y
    }
}