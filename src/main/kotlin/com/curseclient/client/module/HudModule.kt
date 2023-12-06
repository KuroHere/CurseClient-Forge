package com.curseclient.client.module

import com.curseclient.client.event.EventBus
import com.curseclient.client.event.events.CurseClientEvent
import com.curseclient.client.event.events.render.Render2DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.setting.Setting
import com.curseclient.client.setting.getHudSetting
import com.curseclient.client.setting.type.BooleanSetting
import com.curseclient.client.utility.misc.NotificationType
import com.curseclient.client.utility.misc.NotificationUtils
import com.curseclient.client.utility.render.graphic.GLUtils.matrix
import net.minecraft.client.Minecraft
import java.awt.Color

abstract class HudModule(
    val name: String,
    val description: String,
    val hudCategory: HudCategory,
    val alwaysListenable: Boolean = false,
    val enabledByDefault: Boolean = false
) {

    private var isEnabled = false // TODO: make as setting
    val isDisabled: Boolean get() = !isEnabled
    val isVisible get() = getHudSetting<BooleanSetting>("Visible")?.value ?: true

    var settings = ArrayList<Setting<*>>()

    val mc: Minecraft = Minecraft.getMinecraft()

    init {
        settings.add(BooleanSetting("Visible", true, visibility = { this !is HudModule }))
    }

    init {
        safeListener<Render2DEvent>(10) {
            if (this@HudModule is DraggableHudModule) onRenderPre()
            matrix {
                onRender()
            }
            if (this@HudModule is DraggableHudModule) onRenderPost()
        }
    }

    open fun onRender() {}

    protected open fun onEnable() {}

    protected open fun onDisable() {}

    protected open fun onClientLoad() {}

    open fun getHudInfo():String {
        return ""
    }

    fun onInit() {
        if (alwaysListenable) EventBus.subscribe(this)
        onClientLoad()
    }

    fun toggle() { setEnabled(!isEnabled) }

    fun isEnabled(): Boolean {
        return isEnabled
    }

    fun setEnabled(state: Boolean){
        if (state) enable() else disable()
    }

    private fun enable(){
        if(!isEnabled){
            isEnabled = true
            if (!alwaysListenable) EventBus.subscribe(this)

            EventBus.post(CurseClientEvent.HudModuleToggleEvent(this))
            NotificationUtils.notify(name, "Module has been enable", NotificationType.INFO, descriptionColor = Color.LIGHT_GRAY)

            onEnable()
        }
    }

    private fun disable(){
        if (isEnabled){
            isEnabled = false
            if (!alwaysListenable) EventBus.unsubscribe(this)
            if (this is DraggableHudModule) isDragging = false

            EventBus.post(CurseClientEvent.HudModuleToggleEvent(this))
            NotificationUtils.notify(name, "Module has been disable", NotificationType.INFO, descriptionColor = Color.LIGHT_GRAY)

            onDisable()
        }
    }

}