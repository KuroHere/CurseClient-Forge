package com.curseclient.client.gui.api.other

interface IGuiElement {
    fun onRegister()
    fun onGuiOpen()
    fun onGuiClose()
    fun onGuiCloseAttempt()
    fun onTick()
    fun onRender()
    fun onMouseAction(action: MouseAction, button: Int)
    fun onKey(typedChar: Char, key: Int)
}
