package com.curseclient.client.event.listener

import com.curseclient.client.event.ClientEvent
import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.utility.threads.MainThreadExecutor

inline fun runSafe(block: SafeClientEvent.() -> Unit) {
    ClientEvent().toSafe()?.let { block(it) }
}

inline fun <T> runSafeR(block: SafeClientEvent.() -> T): T? {
    return ClientEvent().toSafe()?.let { block(it) }
}

inline fun runTrying(block: () -> Unit) {
    try { block() } catch (_: Exception) { }
}

fun <T: Any> tryGetOrNull(block: () -> T?): T? {
    0.let {  }
    runTrying { return block() }
    return null
}

fun <T> onMainThread(block: () -> T) =
    MainThreadExecutor.execute(block)

fun <T> onMainThreadSafe(block: SafeClientEvent.() -> T) =
    onMainThread { ClientEvent().toSafe()?.block() }

suspend fun <T> onMainThreadSuspend(block: () -> T) =
    MainThreadExecutor.executeSuspend(block)

fun <T: Any, R> T.withSync(block: (obj: T) -> R) =
    synchronized(lock = this) {
        block(this)
    }