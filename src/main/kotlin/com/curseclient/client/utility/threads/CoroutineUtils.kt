package com.curseclient.client.utility.threads

import baritone.api.utils.Helper.mc
import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.listener.runSafe
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.crash.CrashReport
import kotlin.coroutines.CoroutineContext

/*
 *FROM 711.CLUB vvv
 */
private val defaultContext = Dispatchers.Default + CoroutineExceptionHandler { context, throwable ->
    mc.crashed(
        CrashReport(
            """
            Curse: An uncaught exception was thrown from a coroutine. This means something 
            bad happened that would probably make the game unplayable if it wasn't shut down.
            
            Context: $context
            
            DM the devs and tell them to fix their shitcode! (also please send them this whole log)
            
            """.trimIndent(), throwable
        )
    )
}

val defaultScope = CoroutineScope(Dispatchers.Default)

object Background : CoroutineScope by CoroutineScope(defaultContext), CoroutineContext by defaultContext

// big
fun mainThread(block: () -> Unit): ListenableFuture<Any> = mc.addScheduledTask(block)

// BIG
fun backgroundThread(block: suspend CoroutineScope.() -> Unit) = Background.launch(block = block)


fun CoroutineScope.safe(block: SafeClientEvent.() -> Unit) =
    this.launch { runSafe { block() } }

fun runAsync(block: suspend () -> Unit) =
    runAsync(defaultScope, block)

fun runAsync(scope: CoroutineScope, block: suspend () -> Unit) =
    scope.launch {
        try { block() } catch (_: Exception) {}
    }
