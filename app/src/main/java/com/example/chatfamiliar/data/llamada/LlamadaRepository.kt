package com.example.chatfamiliar.data.llamada

import android.util.Log
import com.example.chatfamiliar.data.family.MiembroFamiliaRepository
import io.getstream.video.android.core.Call
import io.getstream.video.android.core.model.RejectReason
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume

/**
 * Operaciones de una videollamada con Stream.
 *
 * Flujo de llamada con timbre (ringing):
 * - Quien llama crea la llamada con ring = true. Stream avisa a los
 *   demás miembros y, cuando alguien contesta, el SDK une
 *   automáticamente a quien llamó.
 * - Quien recibe acepta y luego se une (join).
 * Los errores de Stream se convierten a Result de Kotlin para que el
 * ViewModel los maneje igual que los de Firebase.
 */
class LlamadaRepository(
    private val sesionVideoRepository: SesionVideoRepository,
    private val miembroFamiliaRepository: MiembroFamiliaRepository = MiembroFamiliaRepository()
) {

    suspend fun llamar(destino: DestinoLlamada): Result<Call> {
        val cliente = sesionVideoRepository.obtenerClienteActual()
            ?: return Result.failure(ExcepcionLlamada(CodigoErrorLlamada.SIN_CLIENTE))
        if (cliente.state.hasActiveOrRingingCall()) {
            return Result.failure(ExcepcionLlamada(CodigoErrorLlamada.LLAMADA_EN_CURSO))
        }
        val miUid = cliente.userId

        // Quiénes reciben la llamada y qué datos viajan con ella.
        val otros: List<String> = when (destino) {
            is DestinoLlamada.Privado -> listOf(destino.otroUid)
            is DestinoLlamada.Familia -> {
                val miembros = obtenerUidsFamilia(destino.familiaId)
                val error = miembros.exceptionOrNull()
                if (error != null) {
                    Log.e(ETIQUETA, "ERROR_MIEMBROS: ${error.message}")
                    return Result.failure(
                        ExcepcionLlamada(CodigoErrorLlamada.NO_SE_PUDO_INICIAR))
                }
                miembros.getOrDefault(emptyList())
            }
        }
        val datos: Map<String, Any> = when (destino) {
            is DestinoLlamada.Privado ->
                mapOf(DatosLlamada.CLAVE_TIPO_CHAT to DatosLlamada.TIPO_PRIVADO)
            is DestinoLlamada.Familia -> mapOf(
                DatosLlamada.CLAVE_TIPO_CHAT to DatosLlamada.TIPO_FAMILIA,
                DatosLlamada.CLAVE_FAMILIA_ID to destino.familiaId,
                DatosLlamada.CLAVE_NOMBRE_FAMILIA to destino.nombreFamilia)
        }
        val destinatarios = otros.filter { it.isNotBlank() && it != miUid }.distinct()
        if (destinatarios.isEmpty()) {
            return Result.failure(ExcepcionLlamada(CodigoErrorLlamada.DESTINO_INVALIDO))
        }

        // Cada llamada usa un id nuevo para no mezclar llamadas anteriores.
        val llamada = cliente.call(TIPO_LLAMADA, UUID.randomUUID().toString())
        llamada.camera.setEnabled(true)
        llamada.microphone.setEnabled(true)
        val respuesta = llamada.create(
            memberIds = listOf(miUid) + destinatarios,
            custom = datos,
            ring = true,
            video = true
        )
        return when (respuesta) {
            is io.getstream.result.Result.Success -> Result.success(llamada)
            is io.getstream.result.Result.Failure -> {
                Log.e(ETIQUETA, "ERROR_CREAR_LLAMADA: ${respuesta.value.message}")
                Result.failure(ExcepcionLlamada(CodigoErrorLlamada.NO_SE_PUDO_INICIAR,
                    respuesta.value.message))
            }
        }
    }

    suspend fun aceptar(llamada: Call): Result<Unit> {
        llamada.camera.setEnabled(true)
        llamada.microphone.setEnabled(true)
        val aceptada = llamada.accept()
        if (aceptada is io.getstream.result.Result.Failure) {
            Log.e(ETIQUETA, "ERROR_ACEPTAR: ${aceptada.value.message}")
            return Result.failure(ExcepcionLlamada(CodigoErrorLlamada.NO_SE_PUDO_CONECTAR,
                aceptada.value.message))
        }
        val unida = llamada.join()
        if (unida is io.getstream.result.Result.Failure) {
            Log.e(ETIQUETA, "ERROR_UNIRSE: ${unida.value.message}")
            llamada.leave()
            return Result.failure(ExcepcionLlamada(CodigoErrorLlamada.NO_SE_PUDO_CONECTAR,
                unida.value.message))
        }
        return Result.success(Unit)
    }

    /**
     * Busca si la familia tiene una videollamada en curso (alguien
     * conectado). Se filtra por el familiaId que viaja en custom.
     * Devuelve null si no hay ninguna.
     */
    suspend fun buscarLlamadaFamiliar(familiaId: String): Result<Call?> {
        val cliente = sesionVideoRepository.obtenerClienteActual()
            ?: return Result.success(null)
        val respuesta = cliente.queryCalls(
            filters = mapOf(
                "custom.${DatosLlamada.CLAVE_FAMILIA_ID}" to familiaId,
                "ongoing" to true),
            limit = 1)
        return when (respuesta) {
            is io.getstream.result.Result.Success -> {
                val info = respuesta.value.calls.firstOrNull()?.call
                Result.success(info?.let { cliente.call(it.type, it.id) })
            }
            is io.getstream.result.Result.Failure -> {
                Log.w(ETIQUETA, "ERROR_BUSCAR_LLAMADA: ${respuesta.value.message}")
                Result.failure(ExcepcionLlamada(CodigoErrorLlamada.NO_SE_PUDO_CONECTAR,
                    respuesta.value.message))
            }
        }
    }

    /** Entrar a una llamada familiar que ya empezó (sin timbre). */
    suspend fun unirse(llamada: Call): Result<Unit> {
        llamada.camera.setEnabled(true)
        llamada.microphone.setEnabled(true)
        val unida = llamada.join()
        if (unida is io.getstream.result.Result.Failure) {
            Log.e(ETIQUETA, "ERROR_UNIRSE: ${unida.value.message}")
            llamada.leave()
            return Result.failure(ExcepcionLlamada(CodigoErrorLlamada.NO_SE_PUDO_CONECTAR,
                unida.value.message))
        }
        return Result.success(Unit)
    }

    /** Quien recibe no contesta. */
    suspend fun rechazar(llamada: Call) {
        llamada.reject(RejectReason.Decline)
        llamada.leave()
    }

    /** Quien llama se arrepiente antes de que contesten. */
    suspend fun cancelar(llamada: Call) {
        llamada.reject(RejectReason.Cancel)
        llamada.leave()
    }

    /** Salir de una llamada ya conectada. */
    fun colgar(llamada: Call) {
        llamada.leave()
    }

    /** Convierte la consulta de miembros (con callback) en una función suspend. */
    private suspend fun obtenerUidsFamilia(familiaId: String): Result<List<String>> =
        suspendCancellableCoroutine { continuacion ->
            miembroFamiliaRepository.obtenerMiembrosFamilia(familiaId) { resultado ->
                if (continuacion.isActive) {
                    continuacion.resume(resultado.map { miembros -> miembros.map { it.uid } })
                }
            }
        }

    companion object {
        // Tipo de llamada configurado por defecto en el panel de Stream.
        const val TIPO_LLAMADA = "default"
        private const val ETIQUETA = "Llamada"
    }
}

enum class CodigoErrorLlamada {
    SIN_CLIENTE,
    DESTINO_INVALIDO,
    LLAMADA_EN_CURSO,
    NO_SE_PUDO_INICIAR,
    NO_SE_PUDO_CONECTAR
}

class ExcepcionLlamada(
    val codigo: CodigoErrorLlamada,
    detalle: String? = null
) : Exception(detalle ?: codigo.name)