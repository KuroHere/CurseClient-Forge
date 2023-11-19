package com.curseclient.client.event.listener

import com.curseclient.client.event.ListenerManager

const val DEFAULT_PRIORITY = 0

inline fun <reified T : Any> Any.listener(priority: Int = DEFAULT_PRIORITY, noinline function: (T) -> Unit) {
    this.listener(priority, T::class.java, function)
}

fun <T : Any> Any.listener(priority: Int = DEFAULT_PRIORITY, clazz: Class<T>, function: (T) -> Unit) {
    ListenerManager.register(this, Listener(this, clazz, priority, function))
}

class AsyncListener<T : Any>(
    owner: Any,
    override val eventClass: Class<T>,
    override val function: suspend (T) -> Unit
) : AbstractListener<T, suspend (T) -> Unit>(owner) {
    override val priority: Int = DEFAULT_PRIORITY
}

class Listener<T : Any>(
    owner: Any,
    override val eventClass: Class<T>,
    override val priority: Int,
    override val function: (T) -> Unit
) : AbstractListener<T, (T) -> Unit>(owner)



