package com.curseclient.client.utility.render.animation

enum class Direction {
    FORWARDS,
    BACKWARDS;

    fun opposite(): Direction {
        return if (this == FORWARDS) {
            BACKWARDS
        } else FORWARDS
    }

    fun forwards(): Boolean {
        return this == FORWARDS
    }

    fun backwards(): Boolean {
        return this == BACKWARDS
    }
}