package com.curseclient.client.utility.math

import baritone.api.utils.Helper.mc

class PingerUtils {

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
                // Đoạn code này có thể được gọi bất đồng bộ nên tốt hơn là an toàn hơn
                t.printStackTrace()
            }

            return 0
        }
    }
}
