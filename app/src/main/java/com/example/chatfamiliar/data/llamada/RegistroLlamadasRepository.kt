package com.example.chatfamiliar.data.llamada

import android.util.Log
import com.example.chatfamiliar.data.chat.ChatFamiliaRepository
import com.example.chatfamiliar.data.chat.ChatPrivadoRepository
import com.example.chatfamiliar.model.Mensaje

class RegistroLlamadasRepository {
    private val chatPrivadoRepository = ChatPrivadoRepository()
    private val chatFamiliaRepository = ChatFamiliaRepository()

    fun registrar(destino: DestinoLlamada, contestada: Boolean, duracionSegundos: Long) {
        val tipo = if (contestada) Mensaje.TIPO_LLAMADA else Mensaje.TIPO_LLAMADA_PERDIDA
        val contenido = if (contestada) TEXTO_LLAMADA else TEXTO_LLAMADA_PERDIDA
        val duracion = if (contestada) duracionSegundos.coerceAtLeast(0L) else 0L
        val alCompletar: (Result<Unit>) -> Unit = { resultado ->
            resultado
                .onSuccess { Log.d(ETIQUETA, "REGISTRO_GUARDADO: $tipo") }
                .onFailure { error ->
                    Log.w(ETIQUETA, "REGISTRO_FALLIDO: ${error.message}", error) }
        }
        when (destino) {
            is DestinoLlamada.Privado -> chatPrivadoRepository.enviarMensaje(
                conversacionId = destino.conversacionId,
                familiaCompartidaId = destino.familiaCompartidaId,
                mensajeId = chatPrivadoRepository.generarMensajeId(),
                contenido = contenido,
                tipo = tipo,
                duracionSegundos = duracion,
                alCompletar = alCompletar)
            is DestinoLlamada.Familia -> chatFamiliaRepository.enviarMensaje(
                familiaId = destino.familiaId,
                mensajeId = chatFamiliaRepository.generarMensajeId(),
                contenido = contenido,
                tipo = tipo,
                duracionSegundos = duracion,
                alCompletar = alCompletar)
        }
    }

    private companion object {
        const val ETIQUETA = "Llamada"
        const val TEXTO_LLAMADA = "Videollamada"
        const val TEXTO_LLAMADA_PERDIDA = "Videollamada perdida"
    }
}