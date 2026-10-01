package com.example.chatfamiliar.ui.chat

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatfamiliar.data.auth.AuthRepository
import com.example.chatfamiliar.data.chat.CodigoErrorChat
import com.example.chatfamiliar.data.chat.ExcepcionChat
import com.example.chatfamiliar.data.chat.LecturaFamiliaRepository
import com.example.chatfamiliar.data.chat.LecturaPrivadaRepository
import com.example.chatfamiliar.model.ConversacionResumen
import com.example.chatfamiliar.model.LecturaConversacion
import com.example.chatfamiliar.model.Mensaje
import com.example.chatfamiliar.util.TimeoutSolicitud
import com.google.firebase.firestore.ListenerRegistration

class LecturaChatViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val familiaRepository = LecturaFamiliaRepository()
    private val privadoRepository = LecturaPrivadaRepository()
    private val controlLectura = ControlLecturaChat()
    private val timeoutEscucha = TimeoutSolicitud()
    private val timeoutGuardado = TimeoutSolicitud()
    private var escucha: ListenerRegistration? = null
    private var uidActual = ""
    private var conversacionId = ""
    private var tipoActual = ""
    private var versionSesion = 0
    private var versionEscucha = 0
    private var versionGuardado = 0
    private var lecturaDisponible = false
    private var guardadoBloqueado = false
    private var mensajesActuales by mutableStateOf<List<Mensaje>>(emptyList())
    private var historialCompleto by mutableStateOf(false)
    var lectura by mutableStateOf<LecturaConversacion?>(null)
        private set
    var cargandoLectura by mutableStateOf(false)
        private set
    var guardandoLectura by mutableStateOf(false)
        private set
    @get:StringRes
    var errorLecturaRecurso by mutableStateOf<Int?>(null)
        private set
    @get:StringRes
    var errorGuardadoRecurso by mutableStateOf<Int?>(null)
        private set
    val necesitaHistorialAnterior: Boolean
        get() { val actual = lectura ?: return false
            if (!lecturaDisponible || historialCompleto || mensajesActuales.isEmpty()) {
                return false }
            val cursorPresente = mensajesActuales.any {
                it.id == actual.ultimoMensajeLeidoId && it.fechaEnvio ==
                        actual.fechaUltimoMensajeLeido }
            return !cursorPresente }
    fun iniciar(tipo: String, id: String) {
        val uid = authRepository.obtenerUsuarioActual()?.uid
        if (uid.isNullOrBlank()) {
            detener()
            errorLecturaRecurso = recursoError(CodigoErrorChat.SESION_REQUERIDA)
            return }
        val tipoValido = tipo == ConversacionResumen.TIPO_FAMILIA ||
                    tipo == ConversacionResumen.TIPO_PRIVADO
        val idValido = id.isNotBlank() && "/" !in id && id != "." && id != ".."
        if (!tipoValido || !idValido) {
            detener()
            errorLecturaRecurso = recursoError(CodigoErrorChat.IDENTIFICADOR_INVALIDO)
            return }
        if (uidActual == uid && tipoActual == tipo && conversacionId == id && escucha != null) {
            return }
        detener()
        uidActual = uid
        tipoActual = tipo
        conversacionId = id
        escucharLectura() }
    fun actualizarHistorial(mensajes: List<Mensaje>, completo: Boolean) {
        mensajesActuales = mensajes.toList()
        historialCompleto = completo
        intentarGuardarAvance() }
    fun registrarMensajesVisibles(ids: Set<String>) {
        controlLectura.registrarMensajesVisibles(idsVisibles = ids,
            mensajesCargados = mensajesActuales)
        intentarGuardarAvance() }
    fun reintentar() {
        if (!sesionVigente(versionSesion)) return
        if (!lecturaDisponible) { escucharLectura() }
        if (!guardandoLectura) {
            guardadoBloqueado = false
            errorGuardadoRecurso = null
            intentarGuardarAvance() }
    }
    private fun escucharLectura() {
        if (!sesionVigente(versionSesion)) return
        escucha?.remove()
        escucha = null
        timeoutEscucha.cancelar()
        val sesion = versionSesion
        val numeroEscucha = ++versionEscucha
        lecturaDisponible = false
        cargandoLectura = true
        errorLecturaRecurso = null
        timeoutEscucha.iniciar(scope = viewModelScope) {
            if (sesionVigente(sesion) && numeroEscucha == versionEscucha) {
                cargandoLectura = false
                errorLecturaRecurso = recursoError(CodigoErrorChat.TIEMPO_AGOTADO) } }
        val alRecibir: (Result<LecturaConversacion>) -> Unit =
            respuesta@{ resultado ->
                if (!sesionVigente(sesion) || numeroEscucha != versionEscucha) {
                    return@respuesta }
                timeoutEscucha.cancelar()
                cargandoLectura = false
                resultado
                    .onSuccess { nuevaLectura ->
                        if (!esLecturaValida(nuevaLectura)) {
                            lecturaDisponible = false
                            errorLecturaRecurso = recursoError(CodigoErrorChat.DATOS_INVALIDOS)
                        } else {
                            aplicarLectura(nuevaLectura)
                            lecturaDisponible = true
                            errorLecturaRecurso = null
                            intentarGuardarAvance() } }
                    .onFailure { error ->
                        lecturaDisponible = false
                        errorLecturaRecurso = obtenerRecursoErrorChat(error) } }
        escucha = when (tipoActual) {
            ConversacionResumen.TIPO_FAMILIA -> {
                familiaRepository.escucharLectura(conversacionId, alRecibir)}
            ConversacionResumen.TIPO_PRIVADO -> {
                privadoRepository.escucharLectura(conversacionId, alRecibir) }
            else -> null
        } }
    private fun intentarGuardarAvance() {
        if (!sesionVigente(versionSesion) || !lecturaDisponible ||
            guardandoLectura || guardadoBloqueado) { return }
        val lecturaActual = lectura ?: return
        val candidato = controlLectura.buscarSiguienteLectura(uidActual = uidActual,
            lecturaActual = lecturaActual, mensajesCargados = mensajesActuales,
            historialCompleto = historialCompleto) ?: return
        val fechaCandidato = candidato.fechaEnvio ?: return
        val sesion = versionSesion
        val guardado = ++versionGuardado
        guardandoLectura = true
        errorGuardadoRecurso = null
        val solicitud = timeoutGuardado.iniciar(scope = viewModelScope) {
            if (sesionVigente(sesion) && guardado == versionGuardado) {
                versionGuardado++
                guardandoLectura = false
                guardadoBloqueado = true
                errorGuardadoRecurso = recursoError(CodigoErrorChat.TIEMPO_AGOTADO) }
        }

        val alGuardar: (Result<Unit>) -> Unit =
            respuesta@{ resultado ->
                if (!sesionVigente(sesion) || guardado != versionGuardado) {
                    return@respuesta }
                if (!timeoutGuardado.completar(solicitud)) { return@respuesta }
                guardandoLectura = false
                resultado.onSuccess {
                        aplicarLectura(LecturaConversacion(
                                uid = uidActual, ultimoMensajeLeidoId = candidato.id,
                                fechaUltimoMensajeLeido = fechaCandidato))
                        errorGuardadoRecurso = null
                        intentarGuardarAvance() }
                    .onFailure { error ->
                        guardadoBloqueado = true
                        errorGuardadoRecurso = obtenerRecursoErrorChat(error) } }
        when (tipoActual) {
            ConversacionResumen.TIPO_FAMILIA -> {
                familiaRepository.actualizarLectura(conversacionId,
                    candidato.id, alGuardar) }
            ConversacionResumen.TIPO_PRIVADO -> {
                privadoRepository.actualizarLectura(conversacionId,
                    candidato.id, alGuardar) }
        } }

    private fun aplicarLectura(nueva: LecturaConversacion) {
        val anterior = lectura
        if (anterior == null || compararLecturas(nueva, anterior) > 0) {
            lectura = nueva }
        lectura?.let { controlLectura.descartarHastaLecturaConfirmada(it) }
    }

    private fun esLecturaValida(valor: LecturaConversacion): Boolean {
        if (valor.uid != uidActual) return false
        val tieneId = valor.ultimoMensajeLeidoId.isNotBlank()
        val tieneFecha = valor.fechaUltimoMensajeLeido != null
        return tieneId == tieneFecha }

    private fun compararLecturas(primera: LecturaConversacion, segunda: LecturaConversacion): Int {
        val fechaPrimera = primera.fechaUltimoMensajeLeido
        val fechaSegunda = segunda.fechaUltimoMensajeLeido
        if (fechaPrimera == null && fechaSegunda == null) return 0
        if (fechaPrimera == null) return -1
        if (fechaSegunda == null) return 1
        val porFecha = fechaPrimera.compareTo(fechaSegunda)
        return if (porFecha != 0) { porFecha } else {
            primera.ultimoMensajeLeidoId.compareTo(segunda.ultimoMensajeLeidoId) } }

    private fun sesionVigente(version: Int): Boolean {
        return version == versionSesion && uidActual.isNotBlank() &&
                authRepository.obtenerUsuarioActual()?.uid == uidActual }

    @StringRes
    private fun recursoError(codigo: CodigoErrorChat): Int {
        return obtenerRecursoErrorChat(ExcepcionChat(codigo))
    }

    fun detener() {
        versionSesion++
        versionEscucha++
        versionGuardado++
        escucha?.remove()
        escucha = null
        timeoutEscucha.cancelar()
        timeoutGuardado.cancelar()
        controlLectura.reiniciar()
        uidActual = ""
        conversacionId = ""
        tipoActual = ""
        mensajesActuales = emptyList()
        historialCompleto = false
        lectura = null
        lecturaDisponible = false
        guardadoBloqueado = false
        cargandoLectura = false
        guardandoLectura = false
        errorLecturaRecurso = null
        errorGuardadoRecurso = null
    }

    override fun onCleared() {
        detener()
        super.onCleared()
    }
}