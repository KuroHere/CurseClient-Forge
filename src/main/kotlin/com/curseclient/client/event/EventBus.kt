package com.curseclient.client.event

import com.curseclient.client.event.eventbus.AbstractAsyncEventBus
import com.curseclient.client.event.listener.AsyncListener
import com.curseclient.client.event.listener.Listener
import io.netty.util.internal.ConcurrentSet
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicInteger

object EventBus : AbstractAsyncEventBus() {
    override val subscribedListeners = ConcurrentHashMap<Class<*>, MutableSet<Listener<*>>>()
    override val subscribedListenersAsync = ConcurrentHashMap<Class<*>, MutableSet<AsyncListener<*>>>()

    override fun post(event: Any) {
        subscribedListeners[event.javaClass]?.forEach {
            @Suppress("UNCHECKED_CAST") // IDE meme
            (it as Listener<Any>).function.invoke(event)
        }

        /*val listeners = subscribedListenersAsync[event.javaClass] ?: return

        if (listeners.isNotEmpty()) {
            runBlocking {
                listeners.forEach {
                    launch(Dispatchers.Default) {
                        @Suppress("UNCHECKED_CAST")
                        (it as AsyncListener<Any>).function.invoke(event)
                    }
                }
            }
        }*/
    }


    override fun newSet() = ConcurrentSkipListSet<Listener<*>>(Comparator.reverseOrder())

    override fun newSetAsync() = ConcurrentSet<AsyncListener<*>>()
}