package com.curseclient.client.module

import com.curseclient.client.event.EventBus
import com.curseclient.client.event.events.CurseClientEvent
import com.curseclient.client.module.impls.client.ClickGui
import com.curseclient.client.module.impls.client.SoundManager
import com.curseclient.client.setting.Setting
import com.curseclient.client.setting.getSetting
import com.curseclient.client.setting.type.BooleanSetting
import com.curseclient.client.utility.misc.NotificationType
import com.curseclient.client.utility.misc.NotificationUtils
import com.mojang.realmsclient.gui.ChatFormatting
import net.minecraft.client.Minecraft
import org.lwjgl.input.Keyboard
import java.awt.Color

abstract class Module(
    val name: String,
    val description: String,
    val category: Category,
    var key: Int = Keyboard.KEY_NONE, // TODO: make as setting
    val alwaysListenable: Boolean = false,
    val enabledByDefault: Boolean = false
) {

    private var isEnabled = false // TODO: make as setting
    val isDisabled: Boolean get() = !isEnabled
    val isVisible get() = getSetting<BooleanSetting>("Visible")?.value ?: true
    var settings = ArrayList<Setting<*>>()

    val mc: Minecraft = Minecraft.getMinecraft()

    init {
        settings.add(BooleanSetting("Visible", true, visibility = { this !is HudModule }))
    }
    protected open fun onEnable() {}

    protected open fun onDisable() {}

    protected open fun onClientLoad() {}

    open fun getHudInfo() = ""

    fun onInit() {
        if (alwaysListenable) EventBus.subscribe(this)
        onClientLoad()
    }

    fun toggle() { setEnabled(!isEnabled) }

    fun isEnabled() = isEnabled

    fun setEnabled(state: Boolean){
        if (state) enable() else disable()
    }

    open fun isActive() = isEnabled()

    private fun enable(){
        if(!isEnabled){
            isEnabled = true
            if (!alwaysListenable) EventBus.subscribe(this)

            EventBus.post(CurseClientEvent.ModuleToggleEvent(this))
            NotificationUtils.notify(name, "Module has been " + ChatFormatting.GREEN + "enable" + ChatFormatting.RESET, NotificationType.INFO, descriptionColor = Color.LIGHT_GRAY)
            if (mc.world != null && !ClickGui.isEnabled()) {
               SoundManager.playEnable()
            }
            onEnable()
        }
    }

    private fun disable(){
        if (isEnabled){
            isEnabled = false
            if (!alwaysListenable) EventBus.unsubscribe(this)
            if (this is DraggableHudModule) isDragging = false

            EventBus.post(CurseClientEvent.ModuleToggleEvent(this))
            NotificationUtils.notify(name, "Module has been" + ChatFormatting.RED + " disable" + ChatFormatting.RESET, NotificationType.INFO, descriptionColor = Color.LIGHT_GRAY)
            if (mc.world != null) {
                SoundManager.playDisable()
            }

            onDisable()
        }
    }
}