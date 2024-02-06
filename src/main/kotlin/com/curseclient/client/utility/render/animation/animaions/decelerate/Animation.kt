package com.curseclient.client.utility.render.animation.animaions.decelerate

import com.curseclient.client.utility.math.Timer
import com.curseclient.client.utility.render.animation.Direction

/**
 * This animation superclass was made by Foggy and advanced by cedo
 *
 * @author Foggy
 * @author cedo
 * @since 7/21/2020 (yes 2020)
 * @since 7/29/2021
 *
 * I don't know who they are, but KuroHere translates this to kt - kurohere
 */
abstract class Animation(ms: Int, endPoint: Double, var theDirection: Direction = Direction.FORWARDS) {

    private val timerUtil = Timer()
    var durationMS = ms
    var theEndPoint = endPoint
        set(value) {
            field = value
        }

    fun finished(direction: Direction): Boolean {
        return isDone() && this.theDirection == direction
    }

    fun getLinearOutput(): Double {
        return 1 - (timerUtil.getTime().toDouble() / durationMS.toDouble()) * theEndPoint
    }

    fun getEndPoint(): Double {
        return theEndPoint
    }

    fun reset() {
        timerUtil.reset()
    }

    fun isDone(): Boolean {
        return timerUtil.hasTimeElapsed(durationMS.toLong())
    }

    fun changeDirection() {
        setDirection(theDirection.opposite())
    }

    fun getDirection(): Direction {
        return theDirection
    }

    fun setDirection(direction: Direction) {
        if (this.theDirection != direction) {
            this.theDirection = direction
            timerUtil.setTime(System.currentTimeMillis() - (durationMS - durationMS.coerceAtMost(timerUtil.getTime().toInt())))
        }
    }

    fun setDuration(duration: Int) {
        this.durationMS = duration
    }

    protected open fun correctOutput(): Boolean {
        return false
    }

    open fun getOutput(): Double {
        return if (theDirection.forwards()) {
            if (isDone()) {
                theEndPoint
            } else {
                getEquation(timerUtil.getTime().toDouble() / durationMS.toDouble()) * theEndPoint
            }
        } else {
            if (isDone()) {
                0.0
            } else {
                if (correctOutput()) {
                    val revTime = durationMS.coerceAtMost((durationMS - timerUtil.getTime()).toInt())
                    getEquation(revTime.toDouble() / durationMS.toDouble()) * theEndPoint
                } else {
                    (1 - getEquation(timerUtil.getTime().toDouble() / durationMS.toDouble())) * theEndPoint
                }
            }
        }
    }

    protected abstract fun getEquation(x: Double): Double
}