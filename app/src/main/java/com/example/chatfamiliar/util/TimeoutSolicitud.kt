package com.example.chatfamiliar.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimeoutSolicitud {
    private var job: Job? = null
    private var idSolicitud = 0

    fun iniciar(scope: CoroutineScope,
        tiempoMillis: Long = 10_000,
        alExpirar: () -> Unit
    ): Int {
        cancelar()
        idSolicitud++
        val idActual = idSolicitud
        job = scope.launch {
            delay(tiempoMillis)
            if (idActual == idSolicitud) {
                job = null
                idSolicitud++
                alExpirar()
            }
        }
        return idActual
    }


    fun completar(id: Int): Boolean {
        if (id != idSolicitud) { return false }
        job?.cancel()
        job = null
        return true
    }


    fun cancelar() {
        job?.cancel()
        job = null
        idSolicitud++
    }
}