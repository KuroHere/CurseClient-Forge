package com.curseclient.client.module.impls.visual

import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import net.minecraftforge.client.event.RenderSpecificHandEvent
import org.lwjgl.opengl.GL11

object FancyHandshake: Module(
    "FancyHandshake",
    "Allows to control arm bobbing amount",
    Category.VISUAL
) {
    private val amount by setting("Amount", 1.0, 0.5, 5.0, 0.5)
    init {
        safeListener<RenderSpecificHandEvent> {
            GL11.glScaled(amount, amount, amount) // -_- any questions?
        }
    }
}