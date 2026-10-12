package com.example.chatfamiliar.ui.chat

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.chatfamiliar.data.auth.AuthRepository
import com.example.chatfamiliar.data.chat.CodigoErrorChat
import com.example.chatfamiliar.data.chat.ExcepcionChat
import com.example.chatfamiliar.data.chat.LecturaPrivadaRepository
import com.example.chatfamiliar.data.chat.ListadoPrivadosRepository
import com.example.chatfamiliar.data.chat.MensajesNoLeidosRepository
import com.example.chatfamiliar.data.user.UsuarioRepository
import com.example.chatfamiliar.model.ConversacionPrivada
import com.example.chatfamiliar.model.ConversacionResumen
import com.example.chatfamiliar.model.LecturaConversacion
import com.google.firebase.firestore.ListenerRegistration

/**
 * Mantiene la lista de chats privados de Inicio: escucha las conversaciones,
 * el nombre de cada contacto, su lectura y los mensajes no leídos.
 */
class PrivadosInicioViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val listadoRepository = ListadoPrivadosRepository()
    private val usuarioRepository = UsuarioRepository()
    private val lecturaRepository = LecturaPrivadaRepository()
    private val noLeidosRepository = MensajesNoLeidosRepository()
    var conversaciones by mutableStateOf<List<EstadoConversacionInicio>>(emptyList())
        private set
    var cargando by mutableStateOf(false)
        private set
    var errorListadoRecurso by mutableStateOf<Int?>(null)
        private set
    private var uidSesion: String? = null
    private var iniciado = false
    private var versionSesion = 0L
    private var escuchaListado: ListenerRegistration? = null

    private val seguimientos = mutableMapOf<String, Seguimiento>()

    private val handler = Handler(Looper.getMainLooper())
    private val esperas = mutableMapOf<String, Runnable>()

    /** Escuchas y estado de una conversación privada en la lista de Inicio. */
    private class Seguimiento(val id: String, val otroUsuarioId: String,
        var estado: EstadoConversacionInicio) {
        var lectura: LecturaConversacion? = null
        var escuchaPerfil: ListenerRegistration? = null
        var escuchaLectura: ListenerRegistration? = null

        var versionConteo = 0L }

    /** Empieza a escuchar las conversaciones privadas del usuario. */
    fun iniciar() { val uid = authRepository.obtenerUsuarioActual()?.uid
        if (uid.isNullOrBlank()) { detener()
            errorListadoRecurso = recursoError(CodigoErrorChat.SESION_REQUERIDA)
            return }

        if (iniciado && uidSesion == uid) { return }
        detener()
        uidSesion = uid
        iniciado = true
        cargando = true
        val versionActual = versionSesion
        programarEspera(CLAVE_LISTADO) { if (versionSesion == versionActual) { cargando = false
                errorListadoRecurso = recursoError(CodigoErrorChat.TIEMPO_AGOTADO) } }
        escuchaListado = listadoRepository.escucharConversaciones { resultado ->
            if (versionSesion != versionActual ||
                authRepository.obtenerUsuarioActual()?.uid != uid) { return@escucharConversaciones }
            cancelarEspera(CLAVE_LISTADO)
            cargando = false
            resultado.onSuccess { listado ->
                errorListadoRecurso = null
                actualizarListado(listado, uid) }.onFailure { error ->
                errorListadoRecurso = obtenerRecursoErrorChat(error) } } }
    /** Agrega, actualiza o retira seguimientos según el listado recibido. */
    private fun actualizarListado(listado: List<ConversacionPrivada>,
        uid: String) { val idsActuales = listado.map { it.id }.toSet()
        seguimientos.keys
            .filter { it !in idsActuales }
            .toList()
            .forEach { eliminarSeguimiento(it) }
        listado.forEach { conversacion ->
            val otroUid = conversacion.participantes.first { it != uid }
            var seguimiento = seguimientos[conversacion.id]

            if (seguimiento != null && seguimiento.otroUsuarioId != otroUid) { eliminarSeguimiento(conversacion.id)
                seguimiento = null }
            if (seguimiento == null) { val nuevo = Seguimiento(id = conversacion.id,
                    otroUsuarioId = otroUid,
                    estado = EstadoConversacionInicio(resumen = ConversacionResumen(id = conversacion.id,
                            tipo = ConversacionResumen.TIPO_PRIVADO,
                            ultimoMensaje = conversacion.ultimoMensaje,
                            ultimoRemitenteId =
                                conversacion.ultimoRemitenteId,
                            nombreUltimoRemitente =
                                conversacion.nombreUltimoRemitente,
                            fechaUltimoMensaje =
                                conversacion.fechaUltimoMensaje,
                            otroUsuarioId = otroUid),
                        cargandoNombre = true,
                        cargandoUltimoMensaje = false))
                seguimientos[conversacion.id] = nuevo
                escucharPerfil(nuevo)
                escucharLectura(nuevo) } else { val anterior = seguimiento.estado.resumen
                val cambioUltimoMensaje =
                    anterior.ultimoMensaje != conversacion.ultimoMensaje ||
                            anterior.ultimoRemitenteId !=
                            conversacion.ultimoRemitenteId ||
                            anterior.fechaUltimoMensaje !=
                            conversacion.fechaUltimoMensaje
                seguimiento.estado = seguimiento.estado.copy(resumen = anterior.copy(
                        ultimoMensaje = conversacion.ultimoMensaje,
                        ultimoRemitenteId =
                            conversacion.ultimoRemitenteId,
                        nombreUltimoRemitente =
                            conversacion.nombreUltimoRemitente,
                        fechaUltimoMensaje =
                            conversacion.fechaUltimoMensaje),
                    cargandoUltimoMensaje = false,
                    errorUltimoMensajeRecurso = null)
                if (cambioUltimoMensaje) { actualizarConteo(seguimiento) } } }
        publicar() }

    /** Escucha el perfil del contacto para mostrar su nombre. */
    private fun escucharPerfil(seguimiento: Seguimiento) { val claveEspera = "perfil:${seguimiento.id}"
        programarEspera(claveEspera) { if (estaVigente(seguimiento)) { seguimiento.estado = seguimiento.estado.copy(
                    cargandoNombre = false,
                    errorNombreRecurso = recursoError(CodigoErrorChat.TIEMPO_AGOTADO))
                publicar() } }
        seguimiento.escuchaPerfil = usuarioRepository.escucharUsuario(uidUsuario = seguimiento.otroUsuarioId
        ) { resultado ->
            if (!estaVigente(seguimiento)) { return@escucharUsuario }
            cancelarEspera(claveEspera)
            resultado.onSuccess { usuario ->
                seguimiento.estado = seguimiento.estado.copy(resumen = seguimiento.estado.resumen.copy(
                        titulo = usuario?.nombre.orEmpty().trim()),
                    cargandoNombre = false,
                    errorNombreRecurso = null) }.onFailure { error ->
                seguimiento.estado = seguimiento.estado.copy(cargandoNombre = false,
                    errorNombreRecurso = obtenerRecursoErrorChat(error)) }
            publicar() } }

    /** Escucha el marcador de lectura para recalcular los no leídos. */
    private fun escucharLectura(seguimiento: Seguimiento) { val claveEspera = "lectura:${seguimiento.id}"
        programarEspera(claveEspera) { if (estaVigente(seguimiento)) { seguimiento.versionConteo++
                cancelarEspera("conteo:${seguimiento.id}")
                seguimiento.estado = seguimiento.estado.copy(actualizandoConteo = false,
                    errorConteoRecurso = recursoError(CodigoErrorChat.TIEMPO_AGOTADO))
                publicar() } }
        seguimiento.escuchaLectura = lecturaRepository.escucharLectura(conversacionId = seguimiento.id) { resultado ->
            if (!estaVigente(seguimiento)) { return@escucharLectura }
            cancelarEspera(claveEspera)
            resultado.onSuccess { lectura ->
                seguimiento.lectura = lectura
                actualizarConteo(seguimiento) }.onFailure { error ->
                seguimiento.lectura = null
                seguimiento.versionConteo++
                cancelarEspera("conteo:${seguimiento.id}")
                seguimiento.estado = seguimiento.estado.copy(actualizandoConteo = false,
                    errorConteoRecurso = obtenerRecursoErrorChat(error))
                publicar() } } }

    /** Vuelve a contar los no leídos de la conversación. */
    private fun actualizarConteo(seguimiento: Seguimiento) { if (!estaVigente(seguimiento)) { return }
        val lectura = seguimiento.lectura ?: return
        seguimiento.versionConteo++
        val versionActual = seguimiento.versionConteo
        val claveEspera = "conteo:${seguimiento.id}"
        seguimiento.estado = seguimiento.estado.copy(actualizandoConteo = true,
            errorConteoRecurso = null)
        publicar()
        programarEspera(claveEspera) { if (estaVigente(seguimiento) &&
                seguimiento.versionConteo == versionActual) { seguimiento.versionConteo++
                seguimiento.estado = seguimiento.estado.copy(actualizandoConteo = false,
                    errorConteoRecurso = recursoError(CodigoErrorChat.TIEMPO_AGOTADO))
                publicar() } }
        noLeidosRepository.contarPrivado(conversacionId = seguimiento.id,
            lectura = lectura) { resultado ->
            if (!estaVigente(seguimiento) ||
                seguimiento.versionConteo != versionActual) { return@contarPrivado }
            cancelarEspera(claveEspera)
            resultado.onSuccess { cantidad ->
                seguimiento.estado = seguimiento.estado.copy(resumen = seguimiento.estado.resumen.copy(
                        mensajesNoLeidos = cantidad),
                    conteoConfirmado = true,
                    actualizandoConteo = false,
                    errorConteoRecurso = null) }.onFailure { error ->
                seguimiento.estado = seguimiento.estado.copy(actualizandoConteo = false,
                    errorConteoRecurso = obtenerRecursoErrorChat(error)) }
            publicar() } }

    /** Vuelve a crear las escuchas de una conversación que tuvo error. */
    fun reintentarConversacion(conversacionId: String) { val seguimiento = seguimientos[conversacionId] ?: return
        if (!estaVigente(seguimiento)) { return }

        seguimiento.escuchaPerfil?.remove()
        seguimiento.escuchaLectura?.remove()
        seguimiento.versionConteo++
        cancelarEsperasConversacion(conversacionId)

        // Reemplaza el seguimiento para ignorar callbacks anteriores.
        val nuevo = Seguimiento(id = seguimiento.id,
            otroUsuarioId = seguimiento.otroUsuarioId,
            estado = seguimiento.estado.copy(cargandoNombre = true, actualizandoConteo = false,
                errorNombreRecurso = null, errorConteoRecurso = null))
        seguimientos[conversacionId] = nuevo
        publicar()
        escucharPerfil(nuevo)
        escucharLectura(nuevo) }
    /** Reinicia por completo la escucha del listado. */
    fun recargar() { detener()
        iniciar() }

    /** Publica la lista actualizada para la interfaz. */
    private fun publicar() { conversaciones = seguimientos.values
            .map { it.estado }
            .sortedWith(compareByDescending<EstadoConversacionInicio> { it.resumen.fechaUltimoMensaje
                }.thenBy { it.resumen.id }) }

    /** true si el seguimiento sigue activo para la sesión actual. */
    private fun estaVigente(seguimiento: Seguimiento): Boolean { return iniciado && uidSesion != null &&
                authRepository.obtenerUsuarioActual()?.uid == uidSesion &&
                seguimientos[seguimiento.id] === seguimiento }
    /** Retira las escuchas y esperas de una conversación. */
    private fun eliminarSeguimiento(id: String) { val seguimiento = seguimientos.remove(id) ?: return
        seguimiento.versionConteo++
        seguimiento.escuchaPerfil?.remove()
        seguimiento.escuchaLectura?.remove()
        cancelarEsperasConversacion(id) }

    /** Cancela todos los tiempos límite de una conversación. */
    private fun cancelarEsperasConversacion(id: String) { cancelarEspera("perfil:$id")
        cancelarEspera("lectura:$id")
        cancelarEspera("conteo:$id") }

    /** Ejecuta la acción si la operación no responde dentro del tiempo límite. */
    private fun programarEspera(clave: String,
        alAgotar: () -> Unit) { cancelarEspera(clave)
        val tarea = Runnable { esperas.remove(clave)
            alAgotar() }
        esperas[clave] = tarea
        handler.postDelayed(tarea, TIEMPO_ESPERA_MS) }
    /** Cancela el tiempo límite de una operación. */
    private fun cancelarEspera(clave: String) { val tarea = esperas.remove(clave) ?: return
        handler.removeCallbacks(tarea) }
    /** Texto de error para un código de chat. */
    private fun recursoError(codigo: CodigoErrorChat): Int { return obtenerRecursoErrorChat(ExcepcionChat(codigo)) }
    /** Retira todas las escuchas y limpia la lista. */
    fun detener() { versionSesion++
        iniciado = false
        escuchaListado?.remove()
        escuchaListado = null
        seguimientos.keys.toList().forEach { eliminarSeguimiento(it) }
        esperas.values.forEach { handler.removeCallbacks(it) }
        esperas.clear()
        uidSesion = null
        conversaciones = emptyList()
        cargando = false
        errorListadoRecurso = null }
    override fun onCleared() { detener()
        super.onCleared() }
    companion object {
        private const val CLAVE_LISTADO = "listado"
        private const val TIEMPO_ESPERA_MS = 10_000L } }