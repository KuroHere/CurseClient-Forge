package com.curseclient.client.event.events

import com.curseclient.client.event.Event

abstract class ConnectionEvent : Event {
    class Connect : ConnectionEvent()
    class Disconnect : ConnectionEvent()
}