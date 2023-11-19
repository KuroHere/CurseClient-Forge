package com.curseclient.client.utility.math

class MSTimer {
    private var ms = System.currentTimeMillis()

    fun hasReached(milliseconds: Double): Boolean {
        return getTime().toDouble() > milliseconds
    }

    fun reset() {
        ms = System.currentTimeMillis()
    }

    private fun getTime(): Long {
        return System.currentTimeMillis() - ms
    }
}