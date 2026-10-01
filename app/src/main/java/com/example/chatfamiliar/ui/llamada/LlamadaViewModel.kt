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
import kotlinx.coroutines.flow.first
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
    private var vigilanciaFamiliar: Job? = null

    private val canceladasPorMi = mutableSetOf<String>()

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
        vigilanciaFamiliar = viewModelScope.launch {
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
        llamadaRepository.colgar(llamada)
    }

    fun alternarMicrofono(llamada: Call) {
        val microfono = llamada.microphone
        microfono.setEnabled(!microfono.isEnabled.value)
    }

    fun alternarCamara(llamada: Call) {
        val camara = llamada.camera
        camara.setEnabled(!camara.isEnabled.value)
    }

    fun voltearCamara(llamada: Call) {
        llamada.camera.flip()
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
        return when (codigo) {
            CodigoErrorLlamada.SIN_CLIENTE -> R.string.llamada_error_no_lista
            CodigoErrorLlamada.LLAMADA_EN_CURSO -> R.string.llamada_error_en_curso
            CodigoErrorLlamada.NO_SE_PUDO_CONECTAR -> R.string.llamada_error_conectar
            else -> R.string.llamada_error_iniciar
        }
    }

    private companion object {
        const val TIEMPO_MAXIMO_TIMBRE_MS = 90_000L
        const val TIEMPO_ESPERA_ACTIVA_MS = 10_000L
        const val INTERVALO_REVISION_MS = 10_000L
    }
}