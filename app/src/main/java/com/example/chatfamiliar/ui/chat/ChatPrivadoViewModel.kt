package com.example.chatfamiliar.ui.chat

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatfamiliar.data.auth.AuthRepository
import com.example.chatfamiliar.data.chat.ChatPrivadoRepository
import com.example.chatfamiliar.data.chat.CodigoErrorChat
import com.example.chatfamiliar.data.chat.ConversacionPrivadaRepository
import com.example.chatfamiliar.data.chat.ExcepcionChat
import com.example.chatfamiliar.data.chat.FamiliaCompartidaRepository
import com.example.chatfamiliar.data.chat.HistorialPrivadoRepository
import com.example.chatfamiliar.data.user.UsuarioRepository
import com.example.chatfamiliar.model.Mensaje
import com.example.chatfamiliar.util.TimeoutSolicitud
import com.google.firebase.firestore.ListenerRegistration

class ChatPrivadoViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val conversacionRepository = ConversacionPrivadaRepository()
    private val chatRepository = ChatPrivadoRepository()
    private val historialRepository = HistorialPrivadoRepository()
    private val familiaRepository = FamiliaCompartidaRepository()
    private val usuarioRepository = UsuarioRepository()
    private val esperas = mutableMapOf<String, TimeoutSolicitud>()
    private var escuchaConversacion: ListenerRegistration? = null
    private var escuchaMensajes: ListenerRegistration? = null
    private var escuchaNombre: ListenerRegistration? = null
    private var conversacionId = ""
    private var familiaPreferidaId: String? = null
    private var familiaCompartidaId by mutableStateOf<String?>(null)
    private var datosDisponibles by mutableStateOf(false)
    private var versionSesion = 0
    private var versionDatos = 0
    private var versionMensajes = 0
    private var versionHistorial = 0
    private var versionPermiso = 0
    private var versionEnvio = 0
    private var intentoId: String? = null
    private var intentoContenido: String? = null
    var uidActual by mutableStateOf("")
        private set
    var otroUsuarioId by mutableStateOf("")
        private set
    var nombreContacto by mutableStateOf("")
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
    var verificandoPermiso by mutableStateOf(false)
        private set
    var permisoConsultado by mutableStateOf(false)
        private set
    var enviando by mutableStateOf(false)
        private set
    @get:StringRes
    var errorConversacionRecurso by mutableStateOf<Int?>(null)
        private set
    @get:StringRes
    var errorNombreRecurso by mutableStateOf<Int?>(null)
        private set
    @get:StringRes
    var errorMensajesRecurso by mutableStateOf<Int?>(null)
        private set
    @get:StringRes
    var errorHistorialRecurso by mutableStateOf<Int?>(null)
        private set
    @get:StringRes
    var errorPermisoRecurso by mutableStateOf<Int?>(null)
        private set
    @get:StringRes
    var errorEnvioRecurso by mutableStateOf<Int?>(null)
        private set
    val puedeEnviar: Boolean
        get() = datosDisponibles && familiaCompartidaId != null &&
                permisoConsultado && !verificandoPermiso
    val puedeCargarAnteriores: Boolean
        get() = !historialCompleto && !cargandoAnteriores &&
                mensajes.any { it.fechaEnvio != null }
    fun iniciar(id: String) {
        val uid = authRepository.obtenerUsuarioActual()?.uid
        if (uid.isNullOrBlank()) {
            detener()
            errorConversacionRecurso = recurso(CodigoErrorChat.SESION_REQUERIDA)
            return }
        if (id.isBlank() || "/" in id || id == "." || id == "..") {
            detener()
            errorConversacionRecurso = recurso(CodigoErrorChat.IDENTIFICADOR_INVALIDO)
            return }
        if (conversacionId == id && uidActual == uid && escuchaConversacion != null) {
            return
        }
        detener()
        conversacionId = id
        uidActual = uid
        reintentar() }
    fun reintentar() {
        if (!vigente(versionSesion)) return
        escucharDatos()
        escucharRecientes() }

    private fun escucharDatos() {
        escuchaConversacion?.remove()
        escuchaNombre?.remove()
        escuchaNombre = null
        val sesion = versionSesion
        val revision = ++versionDatos
        errorConversacionRecurso = null
        errorNombreRecurso = null
        datosDisponibles = false
        esperar("datos") { if (vigente(sesion) && revision == versionDatos) {
                errorConversacionRecurso = recurso(CodigoErrorChat.TIEMPO_AGOTADO) } }
        escuchaConversacion = conversacionRepository.escucharConversacion(conversacionId) {
            resultado ->
            if (!vigente(sesion) || revision != versionDatos) {
                return@escucharConversacion }
            cancelarEspera("datos")
            resultado
                .onSuccess { conversacion ->
                    val otro = conversacion.participantes.firstOrNull { it != uidActual }
                    if (otro.isNullOrBlank()) { datosDisponibles = false
                        errorConversacionRecurso = recurso(CodigoErrorChat.DATOS_INVALIDOS)
                    } else {
                        datosDisponibles = true
                        errorConversacionRecurso = null
                        familiaPreferidaId = conversacion.familiaReferenciaId
                        if (otroUsuarioId != otro || escuchaNombre == null) {
                            otroUsuarioId = otro
                            escucharNombre(otro, sesion, revision) }
                        if (!enviando) { comprobarPermiso() } } }
                .onFailure { error ->
                    datosDisponibles = false
                    errorConversacionRecurso = obtenerRecursoErrorChat(error) } }
    }

    private fun escucharNombre(uid: String, sesion: Int, revision: Int) {
        escuchaNombre?.remove()
        esperar("nombre") {
            if (vigente(sesion) && revision == versionDatos) {
                errorNombreRecurso = recurso(CodigoErrorChat.TIEMPO_AGOTADO) } }
        escuchaNombre = usuarioRepository.escucharUsuario(uid) { resultado ->
            if (!vigente(sesion) || revision != versionDatos) { return@escucharUsuario }
            cancelarEspera("nombre")
            resultado
                .onSuccess { usuario -> nombreContacto = usuario?.nombre.orEmpty().trim()
                    errorNombreRecurso = null }
                .onFailure { error ->
                    errorNombreRecurso = obtenerRecursoErrorChat(error) } }
    }
    fun comprobarPermiso() {
        if (!vigente(versionSesion) || !datosDisponibles || otroUsuarioId.isBlank() || enviando) {
            return }
        val sesion = versionSesion
        val revision = ++versionPermiso
        verificandoPermiso = true
        errorPermisoRecurso = null
        val solicitud = esperar("permiso") {
            if (vigente(sesion) && revision == versionPermiso) {
                versionPermiso++
                verificandoPermiso = false
                permisoConsultado = false
                familiaCompartidaId = null
                errorPermisoRecurso = recurso(CodigoErrorChat.TIEMPO_AGOTADO) } }
        familiaRepository.buscarFamiliaCompartida(otroUsuarioId, familiaPreferidaId) {
            resultado ->
            if (!vigente(sesion) || revision != versionPermiso) {
                return@buscarFamiliaCompartida }
            if (!completarEspera("permiso", solicitud)) {
                return@buscarFamiliaCompartida }
            verificandoPermiso = false
            resultado.onSuccess { familiaId ->
                    familiaCompartidaId = familiaId
                    permisoConsultado = true
                    errorPermisoRecurso = null }
                .onFailure { error ->
                    familiaCompartidaId = null
                    permisoConsultado = false
                    errorPermisoRecurso = obtenerRecursoErrorChat(error) } } }

    private fun escucharRecientes() {
        escuchaMensajes?.remove()
        val sesion = versionSesion
        val revision = ++versionMensajes
        cargandoMensajes = true
        errorMensajesRecurso = null
        esperar("mensajes") {
            if (vigente(sesion) && revision == versionMensajes) {
                cargandoMensajes = false
                errorMensajesRecurso = recurso(CodigoErrorChat.TIEMPO_AGOTADO) } }
        escuchaMensajes = chatRepository.escucharMensajesRecientes(conversacionId) { resultado ->
            if (!vigente(sesion) || revision != versionMensajes) {
                return@escucharMensajesRecientes }
            cancelarEspera("mensajes")
            cargandoMensajes = false
            resultado.onSuccess { recientes ->
                    errorMensajesRecurso = null

                    if (recientes.isNotEmpty()) {
                        val ids = mensajes.mapTo(mutableSetOf()) { it.id }
                        if (mensajes.isNotEmpty() && recientes.none { it.id in ids }) {
                            versionHistorial++
                            cancelarEspera("historial")
                            cargandoAnteriores = false
                            historialCompleto = false
                            errorHistorialRecurso = null
                            mensajes = ordenar(recientes)
                        } else {
                            mensajes = combinar(mensajes, recientes) } } }
                .onFailure { error ->
                    errorMensajesRecurso = obtenerRecursoErrorChat(error) } } }
    fun cargarAnteriores() {
        if (!puedeCargarAnteriores || !vigente(versionSesion)) { return }
        val primero = mensajes.firstOrNull { it.fechaEnvio != null } ?: return
        val sesion = versionSesion
        val revision = ++versionHistorial
        cargandoAnteriores = true
        errorHistorialRecurso = null
        val solicitud = esperar("historial") {
            if (vigente(sesion) && revision == versionHistorial) {
                versionHistorial++
                cargandoAnteriores = false
                errorHistorialRecurso = recurso(CodigoErrorChat.TIEMPO_AGOTADO) } }
        historialRepository.cargarMensajesAnteriores(conversacionId, primero) {
            resultado ->
            if (!vigente(sesion) || revision != versionHistorial) {
                return@cargarMensajesAnteriores }
            if (!completarEspera("historial", solicitud)) {
                return@cargarMensajesAnteriores }
            cargandoAnteriores = false
            resultado.onSuccess { anteriores ->
                    mensajes = combinar(mensajes, anteriores)
                    historialCompleto = anteriores.size < HistorialPrivadoRepository.TAMANO_PAGINA
                    errorHistorialRecurso = null }
                .onFailure { error -> errorHistorialRecurso = obtenerRecursoErrorChat(error) } } }

    fun actualizarBorrador(texto: String) {
        if (!enviando) { borrador = texto
            errorEnvioRecurso = null } }

    fun enviarMensaje() {
        if (enviando) return
        if (!vigente(versionSesion)) {
            errorEnvioRecurso = recurso(CodigoErrorChat.SESION_REQUERIDA)
            return }
        if (!puedeEnviar) { errorEnvioRecurso = recurso(CodigoErrorChat.FAMILIA_NO_DISPONIBLE)
            return }
        val contenido = borrador.trim()
        if (contenido.isEmpty()) {
            errorEnvioRecurso = recurso(CodigoErrorChat.MENSAJE_VACIO)
            return }
        if (contenido.length > ChatPrivadoRepository.MAXIMO_CARACTERES) {
            errorEnvioRecurso = recurso(CodigoErrorChat.MENSAJE_DEMASIADO_LARGO)
            return }
        val mensajeId = if (intentoContenido == contenido && intentoId != null) {
            intentoId!!
        } else { chatRepository.generarMensajeId().also {
                intentoId = it
                intentoContenido = contenido } }
        val sesion = versionSesion
        val revision = ++versionEnvio
        enviando = true
        errorEnvioRecurso = null
        val solicitud = esperar("envio") {
            if (vigente(sesion) && revision == versionEnvio) {
                versionEnvio++
                enviando = false
                errorEnvioRecurso = recurso(CodigoErrorChat.TIEMPO_AGOTADO) } }
        // Se vuelve a consultar antes de cada envío.
        familiaRepository.buscarFamiliaCompartida(otroUsuarioId,
            familiaCompartidaId) { resultado ->
            if (!vigente(sesion) || revision != versionEnvio) {
                return@buscarFamiliaCompartida }
            resultado.onSuccess { familiaId -> familiaCompartidaId = familiaId
                    permisoConsultado = true
                    if (familiaId == null) {
                        finalizarEnvio(Result.failure(ExcepcionChat(
                                    CodigoErrorChat.FAMILIA_NO_DISPONIBLE)), solicitud, contenido)
                    } else {
                        chatRepository.enviarMensaje(conversacionId, familiaId,
                            mensajeId, contenido) { envio ->
                            if (vigente(sesion) && revision == versionEnvio) {
                                finalizarEnvio(envio, solicitud, contenido) } } } }
                .onFailure { error ->
                    familiaCompartidaId = null
                    permisoConsultado = false
                    errorPermisoRecurso = obtenerRecursoErrorChat(error)
                    finalizarEnvio(Result.failure(error),
                        solicitud, contenido) } } }

    private fun finalizarEnvio(resultado: Result<Unit>, solicitud: Int,
        contenido: String) {
        if (!completarEspera("envio", solicitud)) return
        enviando = false
        resultado.onSuccess {
                if (borrador.trim() == contenido) { borrador = "" }
                intentoId = null
                intentoContenido = null
                errorEnvioRecurso = null }
            .onFailure { error ->
                errorEnvioRecurso = obtenerRecursoErrorChat(error)
                val codigo = ExcepcionChat.desde(error).codigo
                if (codigo == CodigoErrorChat.SIN_ACCESO ||
                    codigo == CodigoErrorChat.FAMILIA_NO_DISPONIBLE) {
                    familiaCompartidaId = null } }
    }
    private fun combinar(anteriores: List<Mensaje>, nuevos: List<Mensaje>): List<Mensaje> {
        val porId = anteriores.associateBy { it.id }.toMutableMap()
        nuevos.forEach { porId[it.id] = it }
        return ordenar(porId.values.toList()) }
    private fun ordenar(lista: List<Mensaje>): List<Mensaje> {
        return lista.sortedWith(compareBy<Mensaje> { it.fechaEnvio == null }
                .thenBy { it.fechaEnvio }.thenBy { it.id }) }
    private fun vigente(version: Int): Boolean {
        return version == versionSesion && uidActual.isNotBlank() &&
                authRepository.obtenerUsuarioActual()?.uid == uidActual }
    private fun esperar(clave: String, alExpirar: () -> Unit): Int {
        return esperas.getOrPut(clave) { TimeoutSolicitud() }.iniciar(
            scope = viewModelScope, alExpirar = alExpirar) }
    private fun completarEspera(clave: String, solicitud: Int): Boolean {
        return esperas[clave]?.completar(solicitud) == true
    }
    private fun cancelarEspera(clave: String) { esperas[clave]?.cancelar() }
    @StringRes
    private fun recurso(codigo: CodigoErrorChat): Int {
        return obtenerRecursoErrorChat(ExcepcionChat(codigo)) }
    fun detener() {
        versionSesion++
        versionDatos++
        versionMensajes++
        versionHistorial++
        versionPermiso++
        versionEnvio++
        escuchaConversacion?.remove()
        escuchaMensajes?.remove()
        escuchaNombre?.remove()
        escuchaConversacion = null
        escuchaMensajes = null
        escuchaNombre = null
        esperas.values.forEach { it.cancelar() }
        esperas.clear()
        conversacionId = ""
        familiaPreferidaId = null
        familiaCompartidaId = null
        datosDisponibles = false
        uidActual = ""
        otroUsuarioId = ""
        nombreContacto = ""
        mensajes = emptyList()
        borrador = ""
        cargandoMensajes = false
        cargandoAnteriores = false
        historialCompleto = false
        verificandoPermiso = false
        permisoConsultado = false
        enviando = false
        intentoId = null
        intentoContenido = null
        errorConversacionRecurso = null
        errorNombreRecurso = null
        errorMensajesRecurso = null
        errorHistorialRecurso = null
        errorPermisoRecurso = null
        errorEnvioRecurso = null }

    override fun onCleared() { detener()
        super.onCleared() }
}