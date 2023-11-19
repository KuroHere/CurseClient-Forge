package com.curseclient.client.gui.impl.altmanager

import com.curseclient.mixin.accessor.AccessorMinecraft
import fr.litarvan.openauth.microsoft.MicrosoftAuthResult
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator
import net.minecraft.client.Minecraft
import net.minecraft.util.Session
import java.util.*

class Alt(private val login: String, private val password: String, private val altType: AltType) {
    private var altSession: Session? = null

    fun login() {
        if (altSession == null) {
            when (getAltType()) {
                AltType.MICROSOFT -> {
                    val authenticator = MicrosoftAuthenticator()
                    try {
                        val result: MicrosoftAuthResult = authenticator.loginWithCredentials(login, password)
                        altSession = Session(result.profile.name, result.profile.id, result.accessToken, "legacy")
                    } catch (e: MicrosoftAuthenticationException) {
                        e.printStackTrace()
                    }
                }
                AltType.CRACKED -> altSession = Session(getLogin(), UUID.randomUUID().toString(), "", "legacy")

            }
        }
        if (altSession != null) {
            (Minecraft.getMinecraft() as AccessorMinecraft).setSession(altSession)
        }
    }

    private fun getAltType(): AltType {
        return altType
    }


    private fun getLogin(): String {
        return login
    }

    fun getPassword(): String {
        return password
    }

    fun getAltSession(): Session? {
        return altSession
    }


    enum class AltType {
        MICROSOFT,
        CRACKED
    }
}
