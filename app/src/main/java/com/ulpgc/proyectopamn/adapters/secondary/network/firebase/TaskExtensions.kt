package com.ulpgc.proyectopamn.adapters.secondary.network.firebase

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Convierte una `Task` de Firebase (que avisa con callbacks) en una función `suspend`,
 * para poder escribir `val doc = query.get().awaitTask()` en vez de anidar listeners.
 *
 * Compartida por todos los adaptadores de Firebase.
 */
internal suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { exception -> continuation.resumeWithException(exception) }
}
