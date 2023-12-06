package com.curseclient.client.utility.misc

import com.curseclient.client.event.listener.tryGetOrNull
import org.reflections.Reflections

object ReflectionUtils {
    inline fun <reified T:Any> getClassSequence(pack: String): Sequence<Class<out T>> =
        Reflections(pack).getSubTypesOf(T::class.java).asSequence()

    inline fun <reified T: Any> findObjects(pack: String) =
        getClassSequence<T>(pack).mapNotNull { tryGetOrNull { it.kotlin.objectInstance } }
}