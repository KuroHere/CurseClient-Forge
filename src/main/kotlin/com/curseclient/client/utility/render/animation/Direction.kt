package com.curseclient.client.utility.render.animation

enum class Direction {
    FORWARDS,
    BACKWARDS;

    fun opposite() =
        if (this == FORWARDS) {
            BACKWARDS
        } else FORWARDS

    fun forwards() = this == FORWARDS
    fun backwards() = this == BACKWARDS
}