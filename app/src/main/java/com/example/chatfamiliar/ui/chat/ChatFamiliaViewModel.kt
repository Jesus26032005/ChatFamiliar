package com.example.chatfamiliar.ui.chat

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatfamiliar.data.auth.AuthRepository
import com.example.chatfamiliar.data.chat.ChatFamiliaRepository
import com.example.chatfamiliar.data.chat.CodigoErrorChat
import com.example.chatfamiliar.data.chat.ExcepcionChat
import com.example.chatfamiliar.data.chat.HistorialFamiliaRepository
import com.example.chatfamiliar.model.Mensaje
import com.example.chatfamiliar.util.TimeoutSolicitud
import com.google.firebase.firestore.ListenerRegistration

class ChatFamiliaViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val chatRepository = ChatFamiliaRepository()
    private val historialRepository = HistorialFamiliaRepository()
    private val timeoutMensajes = TimeoutSolicitud()
    private val timeoutHistorial = TimeoutSolicitud()
    private val timeoutEnvio = TimeoutSolicitud()
    private var escuchaMensajes: ListenerRegistration? = null
    private var familiaIdActual = ""
    private var versionSesion = 0
    private var versionEscucha = 0
    private var versionHistorial = 0
    private var versionEnvio = 0
    private var mensajeIntentoId: String? = null
    private var contenidoIntento: String? = null
    private var accesoDenegado by mutableStateOf(false)
    var uidActual by mutableStateOf("")
        private set
    var mensajes by mutableStateOf<List<Mensaje>>(emptyList())
        private set
    var borrador by mutableStateOf("")
        private set
    var cargandoMensajes by mutableStateOf(false)
        private set
    var cargandoAnteriores by mutableStateOf(false)
        private set
    var historialCompleto by mutableStateOf(false)
        private set
    var enviando by mutableStateOf(false)
        private set
    @get:StringRes
    var errorMensajesRecurso by mutableStateOf<Int?>(null)
        private set
    @get:StringRes
    var errorHistorialRecurso by mutableStateOf<Int?>(null)
        private set
    @get:StringRes
    var errorEnvioRecurso by mutableStateOf<Int?>(null)
        private set
    val puedeEnviar: Boolean
        get() = uidActual.isNotBlank() &&
                familiaIdActual.isNotBlank() &&
                !accesoDenegado
    val puedeCargarAnteriores: Boolean
        get() = !historialCompleto &&
                !cargandoAnteriores &&
                mensajes.any { it.fechaEnvio != null }
    fun iniciar(familiaId: String) {
        val uid = authRepository.obtenerUsuarioActual()?.uid
        if (uid.isNullOrBlank()) {
            detener()
            errorMensajesRecurso = recursoError(CodigoErrorChat.SESION_REQUERIDA)
            return }
        if (familiaId.isBlank() || "/" in familiaId ||
            familiaId == "." || familiaId == "..") {
            detener()
            errorMensajesRecurso = recursoError(
                CodigoErrorChat.IDENTIFICADOR_INVALIDO)
            return }
        if (familiaIdActual == familiaId && uidActual == uid &&
            escuchaMensajes != null) { return }
        detener()
        familiaIdActual = familiaId
        uidActual = uid
        reintentarMensajes()
    }

    fun reintentarMensajes() {
        if (familiaIdActual.isBlank() || !sesionVigente(versionSesion)) { return }
        escuchaMensajes?.remove()
        escuchaMensajes = null
        timeoutMensajes.cancelar()
        val sesion = versionSesion
        val escucha = ++versionEscucha
        cargandoMensajes = true
        errorMensajesRecurso = null

        timeoutMensajes.iniciar(
            scope = viewModelScope
        ) {
            if (sesionVigente(sesion) && escucha == versionEscucha) {
                cargandoMensajes = false
                errorMensajesRecurso = recursoError(CodigoErrorChat.TIEMPO_AGOTADO)
            }
        }

        escuchaMensajes = chatRepository.escucharMensajesRecientes(
            familiaIdActual
        ) { resultado ->
            if (!sesionVigente(sesion) || escucha != versionEscucha
            ) { return@escucharMensajesRecientes }
            timeoutMensajes.cancelar()
            cargandoMensajes = false

            resultado.onSuccess { recientes ->
                    errorMensajesRecurso = null
                    accesoDenegado = false
                    incorporarRecientes(recientes) }
                .onFailure { error ->
                    val excepcion = ExcepcionChat.desde(error)
                    errorMensajesRecurso = obtenerRecursoErrorChat(excepcion)
                    if (excepcion.codigo == CodigoErrorChat.SIN_ACCESO ||
                        excepcion.codigo == CodigoErrorChat.SESION_REQUERIDA) {
                        accesoDenegado = true } }
        }
    }

    fun cargarAnteriores() {
        if (!puedeCargarAnteriores || !sesionVigente(versionSesion)) {
            return
        }
        val masAntiguo = mensajes.firstOrNull {
            it.fechaEnvio != null
        } ?: return
        val sesion = versionSesion
        val consulta = ++versionHistorial
        cargandoAnteriores = true
        errorHistorialRecurso = null

        val solicitud = timeoutHistorial.iniciar(scope = viewModelScope) {
            if (sesionVigente(sesion) && consulta == versionHistorial) {
                versionHistorial++
                cargandoAnteriores = false
                errorHistorialRecurso = recursoError(CodigoErrorChat.TIEMPO_AGOTADO)
            }
        }

        historialRepository.cargarMensajesAnteriores(
            familiaIdActual,
            masAntiguo
        ) { resultado ->
            if (!sesionVigente(sesion) || consulta != versionHistorial) {
                return@cargarMensajesAnteriores
            }
            if (!timeoutHistorial.completar(solicitud)) {
                return@cargarMensajesAnteriores
            }
            cargandoAnteriores = false
            resultado
                .onSuccess { anteriores ->
                    mensajes = combinar(anteriores = mensajes, nuevos = anteriores)
                    historialCompleto =
                        anteriores.size < HistorialFamiliaRepository.TAMANO_PAGINA
                    errorHistorialRecurso = null
                }
                .onFailure { error -> errorHistorialRecurso = obtenerRecursoErrorChat(error) }
        }
    }
    fun actualizarBorrador(texto: String) {
        if (enviando) return
        borrador = texto
        errorEnvioRecurso = null
    }

    fun enviarMensaje() {
        if (enviando) return
        if (!sesionVigente(versionSesion)) {
            errorEnvioRecurso = recursoError(CodigoErrorChat.SESION_REQUERIDA)
            return }
        if (!puedeEnviar) {
            errorEnvioRecurso = recursoError(CodigoErrorChat.SIN_ACCESO)
            return }
        val contenido = borrador.trim()
        if (contenido.isEmpty()) {
            errorEnvioRecurso = recursoError(CodigoErrorChat.MENSAJE_VACIO)
            return }
        if (contenido.length > ChatFamiliaRepository.MAXIMO_CARACTERES) {
            errorEnvioRecurso = recursoError(CodigoErrorChat.MENSAJE_DEMASIADO_LARGO)
            return }
        val mensajeId = if (contenidoIntento == contenido &&
            mensajeIntentoId != null) {
            mensajeIntentoId!! } else {
            chatRepository.generarMensajeId().also { nuevoId -> mensajeIntentoId = nuevoId
                contenidoIntento = contenido } }
        val sesion = versionSesion
        val envio = ++versionEnvio
        enviando = true
        errorEnvioRecurso = null
        val solicitud = timeoutEnvio.iniciar(scope = viewModelScope) {
            if (sesionVigente(sesion) && envio == versionEnvio) {
                versionEnvio++
                enviando = false
                errorEnvioRecurso = recursoError(CodigoErrorChat.TIEMPO_AGOTADO)
            }
        }
        chatRepository.enviarMensaje(familiaIdActual, mensajeId, contenido) { resultado ->
            if (!sesionVigente(sesion) || envio != versionEnvio) { return@enviarMensaje }
            if (!timeoutEnvio.completar(solicitud)) {
                return@enviarMensaje }
            enviando = false
            resultado.onSuccess { if (borrador.trim() == contenido) { borrador = "" }
                    mensajeIntentoId = null
                    contenidoIntento = null
                    errorEnvioRecurso = null }
                .onFailure { error ->
                    val excepcion = ExcepcionChat.desde(error)
                    errorEnvioRecurso = obtenerRecursoErrorChat(excepcion)
                    if (excepcion.codigo == CodigoErrorChat.SIN_ACCESO ||
                        excepcion.codigo == CodigoErrorChat.FAMILIA_NO_DISPONIBLE) {
                        accesoDenegado = true } }
        }
    }

    private fun incorporarRecientes(recientes: List<Mensaje>) {
        if (recientes.isEmpty()) { return }
        val idsActuales = mensajes.mapTo(mutableSetOf()) { it.id }
        val hayCoincidencias = recientes.any { it.id in idsActuales }

        if (mensajes.isNotEmpty() && !hayCoincidencias) {
            versionHistorial++
            timeoutHistorial.cancelar()
            cargandoAnteriores = false
            historialCompleto = false
            errorHistorialRecurso = null
            mensajes = ordenar(recientes)
        } else { mensajes = combinar(
                anteriores = mensajes,
                nuevos = recientes) }
    }
    private fun combinar(
        anteriores: List<Mensaje>,
        nuevos: List<Mensaje>
    ): List<Mensaje> {
        val porId = anteriores.associateBy { it.id }.toMutableMap()
        nuevos.forEach { mensaje -> porId[mensaje.id] = mensaje }
        return ordenar(porId.values.toList())
    }
    private fun ordenar(lista: List<Mensaje>): List<Mensaje> {
        return lista.sortedWith(
            compareBy<Mensaje> { it.fechaEnvio == null
            }.thenBy { it.fechaEnvio }.thenBy { it.id })
    }

    private fun sesionVigente(version: Int): Boolean {
        return version == versionSesion &&
                uidActual.isNotBlank() &&
                authRepository.obtenerUsuarioActual()?.uid == uidActual }

    @StringRes
    private fun recursoError(codigo: CodigoErrorChat): Int {
        return obtenerRecursoErrorChat(ExcepcionChat(codigo)) }

    fun detener() {
        versionSesion++
        versionEscucha++
        versionHistorial++
        versionEnvio++
        escuchaMensajes?.remove()
        escuchaMensajes = null
        timeoutMensajes.cancelar()
        timeoutHistorial.cancelar()
        timeoutEnvio.cancelar()
        familiaIdActual = ""
        uidActual = ""
        mensajes = emptyList()
        borrador = ""
        cargandoMensajes = false
        cargandoAnteriores = false
        historialCompleto = false
        enviando = false
        accesoDenegado = false
        mensajeIntentoId = null
        contenidoIntento = null
        errorMensajesRecurso = null
        errorHistorialRecurso = null
        errorEnvioRecurso = null
    }

    override fun onCleared() {
        detener()
        super.onCleared()
    }
}