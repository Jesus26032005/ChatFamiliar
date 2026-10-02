package com.example.chatfamiliar.ui.llamada

import android.app.Application
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatfamiliar.ChatFamiliarApplication
import com.example.chatfamiliar.R
import com.example.chatfamiliar.data.llamada.CodigoErrorLlamada
import com.example.chatfamiliar.data.llamada.DestinoLlamada
import com.example.chatfamiliar.data.llamada.ExcepcionLlamada
import com.example.chatfamiliar.data.llamada.LlamadaRepository
import com.example.chatfamiliar.data.llamada.RegistroLlamadasRepository
import io.getstream.video.android.core.Call
import io.getstream.video.android.core.RingingState
import io.getstream.video.android.core.StreamVideo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Estado y acciones de las videollamadas.
 *
 * Vive a nivel de la actividad (se crea en AppNavigation fuera del
 * NavHost), así la llamada no se corta al cambiar de pantalla.
 * Qué llamada mostrar lo decide el propio SDK con
 * cliente.state.ringingCall (sonando) y cliente.state.activeCall (en curso).
 */
class LlamadaViewModel(application: Application) : AndroidViewModel(application) {
    private val sesionVideoRepository =
        (application as ChatFamiliarApplication).sesionVideoRepository
    private val llamadaRepository = LlamadaRepository(sesionVideoRepository)
    private val registroLlamadasRepository = RegistroLlamadasRepository()

    val cliente: StateFlow<StreamVideo?> = sesionVideoRepository.cliente

    var iniciando by mutableStateOf(false)
        private set
    var conectando by mutableStateOf(false)
        private set

    // Mensaje breve para el usuario (error o "no contestó").
    @get:StringRes
    var avisoRecurso by mutableStateOf<Int?>(null)
        private set

    // Se marca al tocar "Contestar" en la notificación del sistema.
    var aceptarAlMostrar by mutableStateOf(false)
        private set

    // Llamada familiar en curso del chat que está abierto (para "Unirse").
    var llamadaFamiliarEnCurso by mutableStateOf<Call?>(null)
        private set
    // true si en la familia abierta no hay nadie más a quien llamar.
    var familiaSinOtrosIntegrantes by mutableStateOf(false)
        private set
    private var vigilanciaFamiliar: Job? = null

    private val canceladasPorMi = mutableSetOf<String>()

    init {
        // Cada vez que hay un cliente de Stream (al iniciar sesión):
        // 1. Revisamos si alguien nos estaba marcando antes de conectarnos.
        // 2. Vigilamos la llamada activa para colgar si nos quedamos solos.
        viewModelScope.launch {
            cliente.collectLatest { clienteActual ->
                if (clienteActual == null) return@collectLatest
                revisarLlamadaEntrantePendiente()
                clienteActual.state.activeCall.collectLatest { activa ->
                    if (activa != null) vigilarLlamadaActiva(activa)
                }
            }
        }
    }

    fun llamar(destino: DestinoLlamada) {
        if (iniciando) return
        iniciando = true
        viewModelScope.launch {
            llamadaRepository.llamar(destino)
                .onSuccess { llamada -> seguirLlamada(llamada, destino) }
                .onFailure { error -> avisoRecurso = recursoDe(error) }
            iniciando = false
        }
    }

    fun aceptar(llamada: Call) {
        if (conectando) return
        conectando = true
        aceptarAlMostrar = false
        viewModelScope.launch {
            llamadaRepository.aceptar(llamada)
                .onFailure { error -> avisoRecurso = recursoDe(error) }
            conectando = false
        }
    }

    /** Entrar a la llamada familiar que ya está en curso. */
    fun unirse(llamada: Call) {
        if (conectando) return
        conectando = true
        viewModelScope.launch {
            llamadaRepository.unirse(llamada)
                .onFailure { error -> avisoRecurso = recursoDe(error) }
            conectando = false
        }
    }

    /**
     * Mientras el chat familiar está abierto, revisa cada cierto tiempo
     * si hay una videollamada en curso para mostrar el aviso "Unirse".
     */
    fun vigilarLlamadaFamiliar(familiaId: String) {
        vigilanciaFamiliar?.cancel()
        familiaSinOtrosIntegrantes = false
        vigilanciaFamiliar = viewModelScope.launch {
            // Una sola vez al abrir el chat: ¿hay alguien más en la familia?
            llamadaRepository.contarOtrosIntegrantes(familiaId)
                .onSuccess { otros -> familiaSinOtrosIntegrantes = otros == 0 }
            while (isActive) {
                llamadaFamiliarEnCurso = llamadaRepository
                    .buscarLlamadaFamiliar(familiaId).getOrNull()
                delay(INTERVALO_REVISION_MS)
            }
        }
    }

    fun dejarDeVigilarLlamadaFamiliar() {
        vigilanciaFamiliar?.cancel()
        vigilanciaFamiliar = null
        llamadaFamiliarEnCurso = null
        familiaSinOtrosIntegrantes = false
    }

    /** Al tocar la cámara en una familia donde solo estás tú. */
    fun avisarFamiliaSinIntegrantes() {
        avisoRecurso = R.string.llamada_error_familia_sola
    }

    fun rechazar(llamada: Call) {
        aceptarAlMostrar = false
        viewModelScope.launch { llamadaRepository.rechazar(llamada) }
    }

    fun cancelar(llamada: Call) {
        canceladasPorMi.add(llamada.id)
        viewModelScope.launch { llamadaRepository.cancelar(llamada) }
    }

    fun colgar(llamada: Call) {
        viewModelScope.launch { llamadaRepository.colgar(llamada) }
    }

    /**
     * Muestra una llamada que nos está sonando aunque el aviso no haya
     * llegado (la app estaba cerrada o se acaba de abrir). Se llama al
     * conectar con Stream y cada vez que la app vuelve al frente.
     */
    fun revisarLlamadaEntrantePendiente() {
        viewModelScope.launch { llamadaRepository.mostrarLlamadaEntrantePendiente() }
    }

    /**
     * Cuelga automáticamente cuando me quedo solo en la llamada:
     * - Si ya hubo alguien y se fue: espera unos segundos (por si solo
     *   fue un corte de red) y cuelga.
     * - Si nunca entró nadie (por ejemplo, me uní a una llamada que ya
     *   se había vaciado): espera más tiempo y cuelga.
     * collectLatest cancela la espera si alguien vuelve a entrar.
     */
    private suspend fun vigilarLlamadaActiva(llamada: Call) {
        var huboOtros = false
        llamada.state.remoteParticipants
            .map { participantes -> participantes.isNotEmpty() }
            .distinctUntilChanged()
            .collectLatest { hayOtros ->
                if (hayOtros) {
                    huboOtros = true
                } else {
                    delay(if (huboOtros) ESPERA_TRAS_SALIDA_MS else ESPERA_SIN_NADIE_MS)
                    llamadaRepository.colgar(llamada)
                }
            }
    }

    fun alternarMicrofono(llamada: Call) {
        val microfono = llamada.microphone
        microfono.setEnabled(!microfono.isEnabled.value)
    }

    fun alternarCamara(llamada: Call) {
        val camara = llamada.camera
        camara.setEnabled(!camara.isEnabled.value)
    }

    fun marcarAceptarDesdeNotificacion() {
        aceptarAlMostrar = true
    }

    fun limpiarAviso() {
        avisoRecurso = null
    }

    /**
     * Sigue una llamada que YO inicié de principio a fin:
     * 1. Espera a que contesten, la rechacen o se acabe el timbre.
     * 2. Si contestaron, espera a que yo salga de la llamada y mide
     *    cuánto duró.
     * 3. Guarda el registro en el chat ("Videollamada" o
     *    "Videollamada perdida").
     * Se hace aquí y no en la pantalla porque, al terminar, el SDK
     * retira la llamada y la pantalla desaparece.
     */
    private fun seguirLlamada(llamada: Call, destino: DestinoLlamada) {
        viewModelScope.launch {
            val final = withTimeoutOrNull(TIEMPO_MAXIMO_TIMBRE_MS) {
                llamada.state.ringingState.first { estado ->
                    estado is RingingState.Active ||
                            estado is RingingState.RejectedByAll ||
                            estado is RingingState.TimeoutNoAnswer
                }
            }
            val canceladaPorMi = canceladasPorMi.remove(llamada.id)

            if (final !is RingingState.Active) {
                // Nadie contestó (o siguió sonando demasiado): la cerramos.
                if (final == null) { llamadaRepository.cancelar(llamada) }
                registroLlamadasRepository.registrar(destino,
                    contestada = false, duracionSegundos = 0L)
                if (!canceladaPorMi) { avisoRecurso = R.string.llamada_no_contestada }
                return@launch
            }

            val inicio = System.currentTimeMillis()
            val estadoCliente = sesionVideoRepository.obtenerClienteActual()?.state
            if (estadoCliente != null) {
                // Primero confirma que la llamada está activa y luego
                // espera a que deje de estarlo (colgué o se terminó).
                withTimeoutOrNull(TIEMPO_ESPERA_ACTIVA_MS) {
                    estadoCliente.activeCall.first { it?.id == llamada.id }
                }
                estadoCliente.activeCall.first { it?.id != llamada.id }
            }
            val segundos = (System.currentTimeMillis() - inicio) / 1_000L
            registroLlamadasRepository.registrar(destino,
                contestada = true, duracionSegundos = segundos)
        }
    }

    @StringRes
    private fun recursoDe(error: Throwable): Int {
        val codigo = (error as? ExcepcionLlamada)?.codigo
        // Un mensaje claro para cada caso; el detalle técnico queda en Logcat.
        return when (codigo) {
            CodigoErrorLlamada.SIN_CLIENTE -> R.string.llamada_error_no_lista
            CodigoErrorLlamada.LLAMADA_EN_CURSO -> R.string.llamada_error_en_curso
            CodigoErrorLlamada.FAMILIA_SIN_INTEGRANTES -> R.string.llamada_error_familia_sola
            CodigoErrorLlamada.ERROR_INTEGRANTES -> R.string.llamada_error_integrantes
            CodigoErrorLlamada.DESTINATARIO_NO_REGISTRADO -> R.string.llamada_error_no_registrado
            CodigoErrorLlamada.FAMILIA_NO_REGISTRADA -> R.string.llamada_error_familia_no_registrada
            CodigoErrorLlamada.SIN_CONEXION -> R.string.llamada_error_sin_conexion
            CodigoErrorLlamada.SIN_PERMISO -> R.string.llamada_error_sin_permiso
            CodigoErrorLlamada.LLAMADA_TERMINADA -> R.string.llamada_error_terminada
            CodigoErrorLlamada.NO_SE_PUDO_CONECTAR -> R.string.llamada_error_conectar
            CodigoErrorLlamada.DESTINO_INVALIDO,
            CodigoErrorLlamada.NO_SE_PUDO_INICIAR,
            null -> R.string.llamada_error_iniciar
        }
    }

    private companion object {
        const val TIEMPO_MAXIMO_TIMBRE_MS = 90_000L
        const val TIEMPO_ESPERA_ACTIVA_MS = 10_000L
        const val INTERVALO_REVISION_MS = 10_000L
        const val ESPERA_TRAS_SALIDA_MS = 3_000L
        const val ESPERA_SIN_NADIE_MS = 45_000L
    }
}