package com.example.chatfamiliar.ui.chat

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatfamiliar.data.auth.AuthRepository
import com.example.chatfamiliar.data.chat.CodigoErrorChat
import com.example.chatfamiliar.data.chat.ContactosPrivadosRepository
import com.example.chatfamiliar.data.chat.ConversacionPrivadaRepository
import com.example.chatfamiliar.data.chat.ExcepcionChat
import com.example.chatfamiliar.data.chat.FamiliaCompartidaRepository
import com.example.chatfamiliar.data.family.FamiliaRepository
import com.example.chatfamiliar.model.ContactoPrivado
import com.example.chatfamiliar.util.TimeoutSolicitud
import java.text.Normalizer
import java.util.Locale

class NuevoMensajeViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val familiaRepository = FamiliaRepository()
    private val contactosRepository = ContactosPrivadosRepository()
    private val familiaCompartidaRepository = FamiliaCompartidaRepository()
    private val conversacionRepository = ConversacionPrivadaRepository()
    private val timeoutCarga = TimeoutSolicitud()
    private val timeoutApertura = TimeoutSolicitud()
    private var uidSesion: String? = null
    private var versionCarga = 0
    private var versionApertura = 0
    var contactos by mutableStateOf<List<ContactoPrivado>>(emptyList())
        private set
    var busqueda by mutableStateOf("")
        private set
    var cargando by mutableStateOf(false)
        private set
    var cargaConfirmada by mutableStateOf(false)
        private set
    var contactoAbriendoUid by mutableStateOf<String?>(null)
        private set
    var conversacionParaAbrir by mutableStateOf<String?>(null)
        private set
    @get:StringRes
    var errorCargaRecurso by mutableStateOf<Int?>(null)
        private set
    @get:StringRes
    var errorAperturaRecurso by mutableStateOf<Int?>(null)
        private set
    var erroresPorFamilia by mutableStateOf<Map<String, Int>>(emptyMap())
        private set
    val abriendoConversacion: Boolean
        get() = contactoAbriendoUid != null
    val listaIncompleta: Boolean
        get() = erroresPorFamilia.isNotEmpty()
    val contactosFiltrados: List<ContactoPrivado>
        get() {
            val consulta = normalizar(busqueda)
            if (consulta.isEmpty()) {
                return contactos }
            return contactos.filter { contacto ->
                normalizar(contacto.nombre).contains(consulta) }
        }
    fun cargarContactos() {
        val uidActual = authRepository.obtenerUsuarioActual()?.uid
        if (uidActual.isNullOrBlank()) {
            limpiar()
            errorCargaRecurso = recursoError(CodigoErrorChat.SESION_REQUERIDA)
            return }
        if (uidSesion != uidActual) { limpiar()
            uidSesion = uidActual }
        if (cargando || abriendoConversacion || conversacionParaAbrir != null) {
            return }
        cargando = true
        errorCargaRecurso = null
        errorAperturaRecurso = null
        val version = ++versionCarga
        val solicitud = timeoutCarga.iniciar(
            scope = viewModelScope) {
            if (version == versionCarga && validarSesion(uidActual)) {
                versionCarga++
                cargando = false
                errorCargaRecurso = recursoError(
                    CodigoErrorChat.TIEMPO_AGOTADO) }
        }
        familiaRepository.obtenerFamiliasDelUsuario(uidActual) { resultado ->
            if (version != versionCarga || !validarSesion(uidActual)) {
                return@obtenerFamiliasDelUsuario
            }
            resultado.onSuccess { familias ->
                    contactosRepository.obtenerContactos(familias) { consulta ->
                        if (version != versionCarga || !validarSesion(uidActual)) {
                            return@obtenerContactos }
                        if (!timeoutCarga.completar(solicitud)) {
                            return@obtenerContactos }
                        cargando = false
                        consulta.onSuccess { datos ->
                                contactos = datos.contactos
                                erroresPorFamilia = datos.erroresPorFamilia.mapValues {
                                        obtenerRecursoErrorChat(it.value) }
                                cargaConfirmada = true
                                errorCargaRecurso = null }
                            .onFailure { error -> errorCargaRecurso =
                                obtenerRecursoErrorChat(error) } } }
                .onFailure { error ->
                    if (timeoutCarga.completar(solicitud)) {
                        cargando = false
                        errorCargaRecurso = obtenerRecursoErrorChat(error) } }
        }
    }
    fun actualizarBusqueda(nuevoValor: String) { busqueda = nuevoValor }
    fun limpiarBusqueda() { busqueda = "" }
    fun abrirConversacion(contacto: ContactoPrivado) {
        if (abriendoConversacion || conversacionParaAbrir != null) { return }
        val uidActual = uidSesion
        if (uidActual == null || !validarSesion(uidActual)) {
            errorAperturaRecurso = recursoError(
                CodigoErrorChat.SESION_REQUERIDA)
            return }
        val contactoActual = contactos.firstOrNull {
            it.uid == contacto.uid }
        if (contactoActual == null || contactoActual.uid == uidActual) {
            errorAperturaRecurso = recursoError(CodigoErrorChat.DATOS_INVALIDOS)
            return }
        contactoAbriendoUid = contactoActual.uid
        errorAperturaRecurso = null
        val version = ++versionApertura
        val solicitud = timeoutApertura.iniciar(scope = viewModelScope) {
            if (version == versionApertura && validarSesion(uidActual)) {
                versionApertura++
                contactoAbriendoUid = null
                errorAperturaRecurso = recursoError(CodigoErrorChat.TIEMPO_AGOTADO)
            }
        }
        val familiaPreferidaId = contactoActual.familiasCompartidas.firstOrNull()?.id

        familiaCompartidaRepository.buscarFamiliaCompartida(contactoActual.uid,
            familiaPreferidaId
        ) { resultado ->
            if (version != versionApertura || !validarSesion(uidActual)
            ) {
                return@buscarFamiliaCompartida
            }
            resultado
                .onSuccess { familiaId ->
                    if (familiaId == null) {
                        if (timeoutApertura.completar(solicitud)) {
                            contactoAbriendoUid = null
                            errorAperturaRecurso = recursoError(
                                CodigoErrorChat.FAMILIA_NO_DISPONIBLE
                            )
                        }
                    } else {
                        conversacionRepository.obtenerOCrearConversacion(
                            familiaId,
                            contactoActual.uid
                        ) { apertura ->
                            if (version != versionApertura ||
                                !validarSesion(uidActual)
                            ) {
                                return@obtenerOCrearConversacion
                            }
                            if (!timeoutApertura.completar(solicitud)) {
                                return@obtenerOCrearConversacion
                            }
                            contactoAbriendoUid = null
                            apertura.onSuccess { conversacionId ->
                                conversacionParaAbrir = conversacionId
                                errorAperturaRecurso = null
                            }
                                .onFailure { error ->
                                    errorAperturaRecurso =
                                        obtenerRecursoErrorChat(error)
                                }
                        }
                    }
                }
                .onFailure { error ->
                    if (timeoutApertura.completar(solicitud)) {
                        contactoAbriendoUid = null
                        errorAperturaRecurso = obtenerRecursoErrorChat(error)
                    } } }
    }

    fun consumirNavegacion(conversacionId: String) {
        if (conversacionParaAbrir == conversacionId) { conversacionParaAbrir = null } }

    fun limpiar() {
        versionCarga++
        versionApertura++
        timeoutCarga.cancelar()
        timeoutApertura.cancelar()
        uidSesion = null
        contactos = emptyList()
        busqueda = ""
        cargando = false
        cargaConfirmada = false
        contactoAbriendoUid = null
        conversacionParaAbrir = null
        errorCargaRecurso = null
        errorAperturaRecurso = null
        erroresPorFamilia = emptyMap()
    }

    private fun validarSesion(uidEsperado: String): Boolean {
        val vigente = uidSesion == uidEsperado &&
                    authRepository.obtenerUsuarioActual()?.uid == uidEsperado
        if (!vigente) { limpiar()
            errorCargaRecurso = recursoError(CodigoErrorChat.SESION_REQUERIDA)
        }
        return vigente
    }

    @StringRes
    private fun recursoError(codigo: CodigoErrorChat): Int {
        return obtenerRecursoErrorChat(ExcepcionChat(codigo))
    }

    private fun normalizar(texto: String): String {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase(Locale.ROOT).trim() }
    override fun onCleared() {
        limpiar()
        super.onCleared() }
}