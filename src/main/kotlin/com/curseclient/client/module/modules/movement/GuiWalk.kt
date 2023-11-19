package com.curseclient.client.module.modules.movement

import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiRepair
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.util.MovementInputFromOptions
import net.minecraftforge.client.event.InputUpdateEvent
import org.lwjgl.input.Keyboard

object GuiWalk : Module(
    "GuiWalk",
    "Allows to walk while gui is open",
    Category.MOVEMENT
){
    private val sneak by setting("Sneak", false)

    init {
        safeListener<InputUpdateEvent>(9999) {
            if (it.movementInput !is MovementInputFromOptions || isInvalidGui(mc.currentScreen)) return@safeListener

            try {
                if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.keyCode)) {
                    ++it.movementInput.moveForward
                    it.movementInput.forwardKeyDown = true
                } else {
                    it.movementInput.forwardKeyDown = false
                }

                if (Keyboard.isKeyDown(mc.gameSettings.keyBindBack.keyCode)) {
                    --it.movementInput.moveForward
                    it.movementInput.backKeyDown = true
                } else {
                    it.movementInput.backKeyDown = false
                }

                if (Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.keyCode)) {
                    ++it.movementInput.moveStrafe
                    it.movementInput.leftKeyDown = true
                } else {
                    it.movementInput.leftKeyDown = false
                }

                if (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.keyCode)) {
                    --it.movementInput.moveStrafe
                    it.movementInput.rightKeyDown = true
                } else {
                    it.movementInput.rightKeyDown = false
                }

                if (Keyboard.isKeyDown(mc.gameSettings.keyBindJump.keyCode)) {
                    it.movementInput.jump = true
                }

                if (Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.keyCode) && sneak) {
                    it.movementInput.sneak = true
                }
            } catch (_: Exception) { }
        }
    }

    private fun isInvalidGui(guiScreen: GuiScreen?) = guiScreen == null
        || guiScreen is GuiChat
        || guiScreen is GuiEditSign
        || guiScreen is GuiRepair
}