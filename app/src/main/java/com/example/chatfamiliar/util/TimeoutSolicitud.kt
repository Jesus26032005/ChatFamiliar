package com.example.chatfamiliar.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Tiempo límite para operaciones con callback. Cada solicitud recibe un id;
 * si la respuesta llega después de expirar (o de iniciar otra), se descarta.
 */
class TimeoutSolicitud {
    private var job: Job? = null
    private var idSolicitud = 0

    /** Inicia una solicitud y ejecuta alExpirar si no se completa a tiempo. Devuelve su id. */
    fun iniciar(scope: CoroutineScope,
        tiempoMillis: Long = 10_000,
        alExpirar: () -> Unit): Int { cancelar()
        idSolicitud++
        val idActual = idSolicitud
        job = scope.launch { delay(tiempoMillis)
            if (idActual == idSolicitud) { job = null
                idSolicitud++
                alExpirar() } }
        return idActual }


    /** Marca la solicitud como respondida. Devuelve false si ya no está vigente. */
    fun completar(id: Int): Boolean { if (id != idSolicitud) { return false }
        job?.cancel()
        job = null
        return true }


    /** Cancela la solicitud actual sin ejecutar alExpirar. */
    fun cancelar() { job?.cancel()
        job = null
        idSolicitud++ } }