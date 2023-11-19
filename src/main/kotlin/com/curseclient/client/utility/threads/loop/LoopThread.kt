package com.curseclient.client.utility.threads.loop

import kotlin.reflect.KProperty

open class LoopThread(val name: String, val block: () -> Unit) {
    protected var currentThread: Thread? = null

    fun reload() {
        currentThread?.stop()
        currentThread = null

        runThread()
    }

    private fun runThread() {
        currentThread = Thread({
                while (true) {
                    block()
                }
            }, name).also { it.isDaemon = true }

        currentThread?.start()
    }

    operator fun getValue(ref: Any, property: KProperty<*>) =
        currentThread!!
}