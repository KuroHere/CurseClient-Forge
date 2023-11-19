package com.curseclient.client.event.events.entity

import com.curseclient.client.event.Cancellable
import java.awt.Color

class EntityHighlightOnHitEvent : Cancellable() {

    var colour: Color? = null

}