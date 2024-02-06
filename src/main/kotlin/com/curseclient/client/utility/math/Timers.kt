package com.curseclient.client.utility.math

open class Timer {
    var times = currentTime

    private val currentTime get() = System.currentTimeMillis()
    private var lastMS: Long = System.currentTimeMillis()
    private var millis: Long = 0
    private var time = -1L

    init {
        reset()
    }

    fun finished(delay: Long): Boolean {
        return System.currentTimeMillis() - delay >= millis
    }

    fun reset() {
        millis = System.currentTimeMillis()
    }

    fun getElapsedTime(): Long {
        return System.currentTimeMillis() - millis
    }

    fun hasPassed(ms: Double): Boolean {
        return System.currentTimeMillis() - time >= ms
    }

    fun getTime(): Long {
        return System.currentTimeMillis() - lastMS
    }

    fun hasTimeElapsed(time: Long): Boolean {
        return System.currentTimeMillis() - lastMS > time
    }

    fun setTime(time: Long) {
        lastMS = time
    }

    fun reset(offset: Int) {
        reset(offset.toLong())
    }

    fun reset(offset: Long = 0L) {
        times = currentTime + offset
    }

    fun passed(ms: Double): Boolean {
        return System.currentTimeMillis() - times >= ms
    }

}