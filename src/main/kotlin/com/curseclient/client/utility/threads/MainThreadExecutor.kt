package com.curseclient.client.utility.threads

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.minecraft.client.Minecraft

object MainThreadExecutor {
    private val jobs = ArrayList<MainThreadJob<*>>()
    private val mutex = Mutex()

    @JvmStatic
    fun begin() =
        runJobs()

    private fun runJobs() {
        if (jobs.isEmpty()) return

        runBlocking {
            mutex.withLock {
                jobs.forEach {
                    it.run()
                }
                jobs.clear()
            }
        }
    }

    fun <T> execute(block: () -> T) =
        MainThreadJob(block).let {
            if (Minecraft.getMinecraft().isCallingFromMinecraftThread) {
                it.run()
            } else {
                runBlocking {
                    mutex.withLock {
                        jobs.add(it)
                    }
                }
            }
            it
        }.deferred

    suspend fun <T> executeSuspend(block: () -> T) =
        MainThreadJob(block).apply {
            if (Minecraft.getMinecraft().isCallingFromMinecraftThread) {
                run()
            } else {
                mutex.withLock {
                    jobs.add(this)
                }
            }
        }.deferred

    private class MainThreadJob<T>(private val block: () -> T) {
        val deferred = CompletableDeferred<T>()

        fun run() {
            deferred.completeWith(
                runCatching { block.invoke() }
            )
        }
    }
}