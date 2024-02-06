package com.curseclient.client.utility.math

import baritone.api.utils.Helper.mc

class PingeUtils {

    companion object {
        fun getPing(): Int {
            try {
                val connection = mc.connection
                if (connection != null) {
                    val info = connection.getPlayerInfo(mc.connection!!.gameProfile.id)
                    if (info != null) {
                        return info.responseTime
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            return 0
        }
    }
}
