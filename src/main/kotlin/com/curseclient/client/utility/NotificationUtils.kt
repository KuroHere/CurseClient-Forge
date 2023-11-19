package com.curseclient.client.utility

import com.curseclient.client.event.EventBus
import com.curseclient.client.event.events.CurseClientEvent
import com.curseclient.client.utility.render.font.BonIcon
import java.awt.Color

object NotificationUtils {
    fun notify(text: String, description: String, type: NotificationType, mainColor: Color = Color.WHITE, descriptionColor: Color = Color.WHITE) {
        val notification = NotificationInfo(text, description, type, mainColor, descriptionColor)
        val event = CurseClientEvent.NotificationEvent(notification)

        EventBus.post(event)
    }

    val notification_success = BonIcon.CHECK_CIRCLE
    val notification_error = BonIcon.CANCEL
    val notification_info = BonIcon.INFO
}

class NotificationInfo(val text: String, val description: String, val type: NotificationType, val mainColor: Color = Color.WHITE, val descriptionColor: Color = Color.LIGHT_GRAY)

enum class NotificationType(val icon: String, val typeName: String){
    SUCCESS(NotificationUtils.notification_success, "Success"),
    ERROR(NotificationUtils.notification_error, "Error"),
    INFO(NotificationUtils.notification_info, "Info")
}