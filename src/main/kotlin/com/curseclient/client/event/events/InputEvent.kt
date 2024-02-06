package com.curseclient.client.event.events

import com.curseclient.client.event.Event
import com.curseclient.client.gui.api.other.MouseAction

sealed class InputEvent(val state: Boolean) : Event {
    class Keyboard(val key: Int, state: Boolean, val action : MouseAction) : InputEvent(state){
    }

    class Mouse(val button: Int, state: Boolean, val action: MouseAction) : InputEvent(state) {
    }
}