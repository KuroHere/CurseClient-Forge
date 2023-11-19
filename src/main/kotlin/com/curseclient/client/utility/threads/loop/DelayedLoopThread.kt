package com.curseclient.client.utility.threads.loop

class DelayedLoopThread(name: String, val runIf: () -> Boolean, val delay: () -> Long, val blockIn: () -> Unit): LoopThread(name, {
    try {
        while (!runIf()) {
            try {
                Thread.sleep(1000L)
            } catch (_: InterruptedException) { }
        }

        val startTime = System.nanoTime()

        blockIn()

        val timeExisted = System.nanoTime() - startTime

        try {
            val time = (delay() * 1000000L) - timeExisted
            if (time > 0) Thread.sleep(time / 1000000, (time % 1000000).toInt())
        } catch (_: InterruptedException) { }
    } catch (_: Exception) { }

}) {
    fun interrupt() {
        currentThread?.interrupt()
    }
}