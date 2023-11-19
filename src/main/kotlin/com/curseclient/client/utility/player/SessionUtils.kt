package com.curseclient.client.utility.player

import net.minecraft.client.Minecraft
import net.minecraft.util.Session
import java.lang.reflect.Field

object SessionUtils {
    fun setSession(s: Session?) {
        val mc: Class<out Minecraft> = Minecraft.getMinecraft().javaClass
        try {
            var session: Field? = null
            for (f in mc.declaredFields) {
                if (f.type.isInstance(s)) {
                    session = f
                }
            }
            checkNotNull(session) { "Session Null" }
            session.isAccessible = true
            session[Minecraft.getMinecraft()] = s
            session.isAccessible = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}