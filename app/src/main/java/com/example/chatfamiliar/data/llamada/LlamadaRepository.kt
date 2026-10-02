package com.example.chatfamiliar.data.llamada

import android.util.Log
import com.example.chatfamiliar.data.family.MiembroFamiliaRepository
import com.google.firebase.auth.FirebaseAuth
import io.getstream.android.video.generated.models.OwnCapability
import io.getstream.video.android.core.Call
import io.getstream.video.android.core.RingingState
import io.getstream.video.android.core.StreamVideo
import io.getstream.video.android.core.model.RejectReason
import io.getstream.video.android.core.model.SortField
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
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
 * Los errores de Stream se clasifican en un CodigoErrorLlamada para que
 * el ViewModel muestre un mensaje claro de cada caso.
 */
class LlamadaRepository(
    private val sesionVideoRepository: SesionVideoRepository,
    private val miembroFamiliaRepository: MiembroFamiliaRepository = MiembroFamiliaRepository()
) {

    suspend fun llamar(destino: DestinoLlamada): Result<Call> {
        val cliente = sesionVideoRepository.obtenerClienteActual()
            ?: return fallo(CodigoErrorLlamada.SIN_CLIENTE)
        if (cliente.state.hasActiveOrRingingCall()) {
            return fallo(CodigoErrorLlamada.LLAMADA_EN_CURSO)
        }
        val miUid = cliente.userId
        val esFamilia = destino is DestinoLlamada.Familia

        // Quiénes reciben la llamada.
        val otros: List<String> = when (destino) {
            is DestinoLlamada.Privado -> listOf(destino.otroUid)
            is DestinoLlamada.Familia -> {
                val miembros = obtenerUidsFamilia(destino.familiaId)
                val error = miembros.exceptionOrNull()
                if (error != null) {
                    Log.e(ETIQUETA, "ERROR_MIEMBROS: ${error.message}")
                    return fallo(CodigoErrorLlamada.ERROR_INTEGRANTES)
                }
                miembros.getOrDefault(emptyList())
            }
        }
        val destinatarios = otros.filter { it.isNotBlank() && it != miUid }.distinct()
        if (destinatarios.isEmpty()) {
            return fallo(if (esFamilia) CodigoErrorLlamada.FAMILIA_SIN_INTEGRANTES
            else CodigoErrorLlamada.DESTINO_INVALIDO)
        }

        // Datos que viajan con la llamada para que quien recibe sepa
        // si es familiar y de qué familia.
        val datos: Map<String, Any> = when (destino) {
            is DestinoLlamada.Privado ->
                mapOf(DatosLlamada.CLAVE_TIPO_CHAT to DatosLlamada.TIPO_PRIVADO)
            is DestinoLlamada.Familia -> mapOf(
                DatosLlamada.CLAVE_TIPO_CHAT to DatosLlamada.TIPO_FAMILIA,
                DatosLlamada.CLAVE_FAMILIA_ID to destino.familiaId,
                DatosLlamada.CLAVE_NOMBRE_FAMILIA to destino.nombreFamilia)
        }

        val primerIntento = crearConTimbre(cliente, miUid, destinatarios, datos)
        if (primerIntento.isSuccess || !esFamilia) return primerIntento

        // En familia: si falló porque alguien aún no existe en Stream (nunca
        // abrió la app con videollamadas), reintentamos solo con quienes sí.
        val codigo = (primerIntento.exceptionOrNull() as? ExcepcionLlamada)?.codigo
        if (codigo != CodigoErrorLlamada.DESTINATARIO_NO_REGISTRADO) return primerIntento
        val registrados = destinatarios.filter { uid -> estaRegistrado(cliente, miUid, uid) }
        Log.d(ETIQUETA, "REGISTRADOS_EN_STREAM: ${registrados.size} de ${destinatarios.size}")
        if (registrados.isEmpty()) {
            return fallo(CodigoErrorLlamada.FAMILIA_NO_REGISTRADA)
        }
        return crearConTimbre(cliente, miUid, registrados, datos)
    }

    /** Crea la llamada y hace sonar a los destinatarios. */
    private suspend fun crearConTimbre(
        cliente: StreamVideo,
        miUid: String,
        destinatarios: List<String>,
        datos: Map<String, Any>
    ): Result<Call> {
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
                // Un 400 al crear casi siempre es porque algún miembro no
                // existe todavía en Stream (es lo único variable que mandamos).
                val codigo = clasificar(respuesta.value,
                    siEsSolicitudInvalida = CodigoErrorLlamada.DESTINATARIO_NO_REGISTRADO,
                    porDefecto = CodigoErrorLlamada.NO_SE_PUDO_INICIAR)
                fallo(codigo, respuesta.value.message)
            }
        }
    }

    /**
     * Comprueba si un usuario ya existe en Stream creando una llamada de
     * prueba SIN timbre con él como miembro. No le suena ni le aparece nada:
     * las llamadas de prueba no llevan "tipoChat" y la app las ignora.
     */
    private suspend fun estaRegistrado(cliente: StreamVideo, miUid: String, uid: String): Boolean {
        val prueba = cliente.call(TIPO_LLAMADA, "prueba-${UUID.randomUUID()}")
        val respuesta = prueba.create(memberIds = listOf(miUid, uid))
        return respuesta is io.getstream.result.Result.Success
    }

    suspend fun aceptar(llamada: Call): Result<Unit> {
        if (llamada.state.endedAt.value != null) {
            return fallo(CodigoErrorLlamada.LLAMADA_TERMINADA)
        }
        llamada.camera.setEnabled(true)
        llamada.microphone.setEnabled(true)
        val aceptada = llamada.accept()
        if (aceptada is io.getstream.result.Result.Failure) {
            Log.e(ETIQUETA, "ERROR_ACEPTAR: ${aceptada.value.message}")
            return fallo(codigoAlConectar(llamada, aceptada.value), aceptada.value.message)
        }
        val unida = llamada.join()
        if (unida is io.getstream.result.Result.Failure) {
            Log.e(ETIQUETA, "ERROR_UNIRSE: ${unida.value.message}")
            llamada.leave()
            return fallo(codigoAlConectar(llamada, unida.value), unida.value.message)
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
                    ?: return Result.success(null)
                // queryCalls ya actualizó el estado de la llamada. Además de
                // "ongoing" confirmamos que no terminó y que hay alguien dentro,
                // para no ofrecer "Unirse" a una llamada vacía.
                val llamada = cliente.call(info.type, info.id)
                val conectados = llamada.state.session.value?.participants?.size ?: 0
                val disponible = llamada.state.endedAt.value == null && conectados > 0
                Result.success(if (disponible) llamada else null)
            }
            is io.getstream.result.Result.Failure -> {
                Log.w(ETIQUETA, "ERROR_BUSCAR_LLAMADA: ${respuesta.value.message}")
                fallo(clasificar(respuesta.value,
                    porDefecto = CodigoErrorLlamada.NO_SE_PUDO_CONECTAR), respuesta.value.message)
            }
        }
    }

    /**
     * Cuántos integrantes de la familia hay además de mí. Sirve para
     * avisar antes de llamar si no hay nadie más.
     */
    suspend fun contarOtrosIntegrantes(familiaId: String): Result<Int> {
        val miUid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return fallo(CodigoErrorLlamada.SIN_CLIENTE)
        return obtenerUidsFamilia(familiaId).map { uids ->
            uids.filter { it.isNotBlank() && it != miUid }.distinct().size
        }
    }

    /** Entrar a una llamada familiar que ya empezó (sin timbre). */
    suspend fun unirse(llamadaEncontrada: Call): Result<Unit> {
        // Una llamada de la que ya saliste queda "destruida" en el SDK.
        // Pedimos la instancia actual por su id para poder volver a entrar.
        val cliente = sesionVideoRepository.obtenerClienteActual()
            ?: return fallo(CodigoErrorLlamada.SIN_CLIENTE)
        val llamada = cliente.call(llamadaEncontrada.type, llamadaEncontrada.id)
        if (llamada.state.endedAt.value != null) {
            return fallo(CodigoErrorLlamada.LLAMADA_TERMINADA)
        }
        llamada.camera.setEnabled(true)
        llamada.microphone.setEnabled(true)
        val unida = llamada.join()
        if (unida is io.getstream.result.Result.Failure) {
            Log.e(ETIQUETA, "ERROR_UNIRSE: ${unida.value.message}")
            llamada.leave()
            return fallo(codigoAlConectar(llamada, unida.value), unida.value.message)
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

    /**
     * Salir de una llamada ya conectada.
     * - En 1 a 1, o si ya no queda nadie más, se termina la llamada para
     *   todos con end(): Stream avisa a los demás y su pantalla se cierra.
     * - En familia con más gente conectada, solo salgo yo (leave()).
     * end() requiere el permiso "end-call"; si no lo tengo o falla,
     * simplemente salgo.
     */
    suspend fun colgar(llamada: Call) {
        val esUnoAUno = llamada.state.members.value.size <= 2
        val quedanOtros = llamada.state.remoteParticipants.value.isNotEmpty()
        val terminarParaTodos = esUnoAUno || !quedanOtros
        if (terminarParaTodos && llamada.hasCapability(OwnCapability.EndCall)) {
            val resultado = llamada.end()
            if (resultado is io.getstream.result.Result.Failure) {
                Log.w(ETIQUETA, "ERROR_TERMINAR: ${resultado.value.message}")
                llamada.leave()
            }
        } else {
            llamada.leave()
        }
    }

    /**
     * Busca una llamada que me está sonando pero cuyo aviso no me llegó
     * (por ejemplo, abrí la app cuando ya estaban marcando). Si la
     * encuentra, la registra como entrante para que aparezca la pantalla
     * de "Contestar / Rechazar".
     */
    suspend fun mostrarLlamadaEntrantePendiente(): Boolean {
        val cliente = sesionVideoRepository.obtenerClienteActual() ?: return false
        if (cliente.state.hasActiveOrRingingCall()) return false
        val miUid = cliente.userId
        val respuesta = cliente.queryCalls(
            filters = mapOf("members" to mapOf("\$in" to listOf(miUid))),
            sort = listOf(SortField.Desc("created_at")),
            limit = 5)
        if (respuesta !is io.getstream.result.Result.Success) {
            Log.w(ETIQUETA, "ERROR_BUSCAR_PENDIENTES")
            return false
        }
        val ahora = System.currentTimeMillis()
        for (info in respuesta.value.calls.map { it.call }) {
            if (info.createdByUserId == miUid) continue
            // Solo llamadas reales de la app (las de prueba no llevan tipoChat).
            if (info.custom[DatosLlamada.CLAVE_TIPO_CHAT] == null) continue
            val creada = info.createdAt?.time ?: continue
            if (ahora - creada > VENTANA_TIMBRE_MS) continue
            val llamada = cliente.call(info.type, info.id)
            val estado = llamada.state
            val rechazos = estado.rejectedBy.value
            val terminada = estado.endedAt.value != null
            val yaRespondida = miUid in estado.acceptedBy.value || miUid in rechazos
            // Si quien llamó canceló, aparece en los rechazos.
            val cancelada = info.createdByUserId in rechazos
            if (terminada || yaRespondida || cancelada) continue
            cliente.state.addRingingCall(llamada, RingingState.Incoming())
            // get() recalcula el estado de timbre: pasa a "Incoming".
            llamada.get()
            Log.d(ETIQUETA, "LLAMADA_PENDIENTE_MOSTRADA: ${info.id}")
            return true
        }
        return false
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

    /** Al contestar o unirse: si la llamada ya terminó, lo decimos así. */
    private fun codigoAlConectar(llamada: Call, error: io.getstream.result.Error): CodigoErrorLlamada {
        if (llamada.state.endedAt.value != null) return CodigoErrorLlamada.LLAMADA_TERMINADA
        return clasificar(error,
            siNoExiste = CodigoErrorLlamada.LLAMADA_TERMINADA,
            porDefecto = CodigoErrorLlamada.NO_SE_PUDO_CONECTAR)
    }

    /**
     * Traduce un error de Stream a nuestro código:
     * - Excepción de red (sin internet, tiempo agotado) -> SIN_CONEXION
     * - HTTP 403 (el panel de Stream no da ese permiso) -> SIN_PERMISO
     * - HTTP 404 (la llamada ya no existe)              -> siNoExiste
     * - HTTP 400 (datos rechazados por Stream)          -> siEsSolicitudInvalida
     * El mensaje original siempre queda en Logcat (etiqueta "Llamada").
     */
    private fun clasificar(
        error: io.getstream.result.Error,
        porDefecto: CodigoErrorLlamada,
        siEsSolicitudInvalida: CodigoErrorLlamada = porDefecto,
        siNoExiste: CodigoErrorLlamada = porDefecto
    ): CodigoErrorLlamada {
        val causa: Throwable? = when (error) {
            is io.getstream.result.Error.ThrowableError -> error.cause
            is io.getstream.result.Error.NetworkError -> error.cause
            else -> null
        }
        val estadoHttp = (error as? io.getstream.result.Error.NetworkError)?.statusCode
        val texto = error.message
        fun esHttp(codigo: Int) = estadoHttp == codigo || "HTTP $codigo" in texto
        return when {
            causa is IOException -> CodigoErrorLlamada.SIN_CONEXION
            esHttp(403) -> CodigoErrorLlamada.SIN_PERMISO
            esHttp(404) -> siNoExiste
            esHttp(400) -> siEsSolicitudInvalida
            else -> porDefecto
        }
    }

    private fun <T> fallo(codigo: CodigoErrorLlamada, detalle: String? = null): Result<T> =
        Result.failure(ExcepcionLlamada(codigo, detalle))

    companion object {
        // Tipo de llamada configurado por defecto en el panel de Stream.
        const val TIPO_LLAMADA = "default"
        private const val ETIQUETA = "Llamada"
        // Hasta cuánto tiempo después de creada se considera que aún suena.
        private const val VENTANA_TIMBRE_MS = 60_000L
    }
}

enum class CodigoErrorLlamada {
    SIN_CLIENTE,
    DESTINO_INVALIDO,
    FAMILIA_SIN_INTEGRANTES,
    ERROR_INTEGRANTES,
    DESTINATARIO_NO_REGISTRADO,
    FAMILIA_NO_REGISTRADA,
    SIN_CONEXION,
    SIN_PERMISO,
    LLAMADA_EN_CURSO,
    LLAMADA_TERMINADA,
    NO_SE_PUDO_INICIAR,
    NO_SE_PUDO_CONECTAR
}

class ExcepcionLlamada(
    val codigo: CodigoErrorLlamada,
    detalle: String? = null
) : Exception(detalle ?: codigo.name)