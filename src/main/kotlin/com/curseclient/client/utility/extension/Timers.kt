package com.curseclient.client.utility.extension


open class Timer {
    var times = currentTime;

    protected val currentTime get() = System.currentTimeMillis()
    private var lastMS: Long = System.currentTimeMillis()
    private var time = -1L
    fun sync() {
        this.time = System.nanoTime()
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

    fun reset(): Timer {
        times = System.currentTimeMillis()
        return this
    }

    fun reset(offset: Long = 0L) {
        times = currentTime + offset
    }

    fun skipTime(delay: Int) {
        skipTime(delay.toLong())
    }

    fun skipTime(delay: Long) {
        times = currentTime - delay
    }

    fun passedMs(ms: Long): Boolean {
        return getMs(System.nanoTime() - times) >= ms
    }

    private fun convertToNS(time: Long): Long {
        return time * 1000000L
    }

    fun passedDMs(ms: Long): Boolean {
        return this.passedNS(this.convertToNS(ms))
    }

    private fun passedNS(ns: Long): Boolean {
        return System.nanoTime() - time >= ns
    }

    private fun getMs(time: Long): Long {
        return time / 1000000L
    }

    fun passed(ms: Double): Boolean {
        return System.currentTimeMillis() - times >= ms
    }

    fun passed(ms: Long): Boolean {
        return System.currentTimeMillis() - times >= ms
    }

    fun passed(delay: Long, reset: Boolean): Boolean {
        if (reset) this.reset()
        return currentTime - times >= delay
    }

}

class TickTimer(val timeUnit: TimeUnit = TimeUnit.MILLISECONDS) : Timer() {
    fun tick(delay: Int, resetIfTick: Boolean = true): Boolean {
        return tick(delay.toLong(), resetIfTick)
    }

    fun tick(delay: Long, resetIfTick: Boolean = true): Boolean {
        return if (currentTime - times > delay * timeUnit.multiplier) {
            if (resetIfTick) times = currentTime
            true
        } else {
            false
        }
    }
}

class StopTimer(val timeUnit: TimeUnit = TimeUnit.MILLISECONDS) : Timer() {
    fun stop(): Long {
        return (currentTime - times) / timeUnit.multiplier
    }
}

enum class TimeUnit(val multiplier: Long) {
    MILLISECONDS(1L),
    TICKS(50L),
    SECONDS(1000L),
    MINUTES(60000L);
}