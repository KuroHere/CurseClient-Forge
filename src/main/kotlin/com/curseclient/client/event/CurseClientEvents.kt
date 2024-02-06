package com.curseclient.client.event

interface Event

interface ICancellable {
    var cancelled: Boolean

    fun cancel() {
        cancelled = true
    }
}

interface ProfilerEvent {
    val profilerName: String
}

open class Cancellable : ICancellable {
    override var cancelled = false
}