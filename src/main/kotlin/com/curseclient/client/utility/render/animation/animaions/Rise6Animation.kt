package com.curseclient.client.utility.render.animation.animaions

import com.curseclient.client.utility.render.animation.ease.EaseUtils

class Rise6Animation(private var easing: EaseUtils.EaseType, private var duration: Long) {

    private var millis: Long = 0
    private var startTime: Long = System.currentTimeMillis()

    var startValue: Double = 0.0
    var destinationValue: Double = 0.0
    var value: Double = 0.0
    var finished: Boolean = false

    fun run(destinationValue: Double) {
        millis = System.currentTimeMillis()
        if (this.destinationValue != destinationValue) {
            this.destinationValue = destinationValue
            reset()
        } else {
            finished = millis - duration > startTime
            if (finished) {
                value = destinationValue
                return
            }
        }

        val result = EaseUtils.getEase(getProgress(), easing, false)
        value = if (value > destinationValue) {
            startValue - (startValue - destinationValue) * result
        } else {
            startValue + (destinationValue - startValue) * result
        }
    }

    fun getProgress(): Double {
        return (System.currentTimeMillis() - startTime).toDouble() / duration.toDouble()
    }

    fun reset() {
        startTime = System.currentTimeMillis()
        startValue = value
        finished = false
    }

    fun getEasing(): EaseUtils.EaseType {
        return easing
    }

    fun setEasing(easing: EaseUtils.EaseType) {
        this.easing = easing
    }

    fun getDuration(): Long {
        return duration
    }

    fun setDuration(duration: Long) {
        this.duration = duration
    }
}