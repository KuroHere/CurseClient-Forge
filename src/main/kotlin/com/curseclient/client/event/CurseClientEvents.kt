package com.curseclient.client.event

import com.curseclient.client.event.listener.AsyncListener
import com.curseclient.client.event.listener.Listener

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