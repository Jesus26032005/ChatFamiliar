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
import com.example.chatfamiliar.data.chat.LecturaFamiliaRepository
import com.example.chatfamiliar.data.chat.MensajesNoLeidosRepository
import com.example.chatfamiliar.data.chat.UltimoMensajeFamiliaRepository
import com.example.chatfamiliar.model.ConversacionResumen
import com.example.chatfamiliar.model.Familia
import com.example.chatfamiliar.model.LecturaConversacion
import com.google.firebase.firestore.ListenerRegistration

class FamiliasInicioViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val ultimoMensajeRepository = UltimoMensajeFamiliaRepository()
    private val lecturaRepository = LecturaFamiliaRepository()
    private val noLeidosRepository = MensajesNoLeidosRepository()
    var conversaciones by mutableStateOf<List<EstadoConversacionInicio>>(
        emptyList())
        private set
    var errorSesionRecurso by mutableStateOf<Int?>(null)
        private set
    private var uidSesion: String? = null
    private val seguimientos = mutableMapOf<String, Seguimiento>()
    private val handler = Handler(Looper.getMainLooper())
    private val esperas = mutableMapOf<String, Runnable>()

    private class Seguimiento(val id: String,
        var estado: EstadoConversacionInicio) {
        var lectura: LecturaConversacion? = null
        var ultimoMensajeConfirmado = false
        var escuchaUltimoMensaje: ListenerRegistration? = null
        var escuchaLectura: ListenerRegistration? = null
        var versionConteo = 0L
    }

    fun sincronizarFamilias(familias: List<Familia>) {
        val uid = authRepository.obtenerUsuarioActual()?.uid
        if (uid.isNullOrBlank()) {
            detener()
            errorSesionRecurso = recursoError(CodigoErrorChat.SESION_REQUERIDA)
            return }
        if (uidSesion != uid) {
            detener()
            uidSesion = uid }
        errorSesionRecurso = null
        val familiasActuales = familias.distinctBy { it.id }
        val idsActuales = familiasActuales.map { it.id }.toSet()
        seguimientos.keys
            .filter { it !in idsActuales }
            .toList()
            .forEach { eliminarSeguimiento(it) }
        familiasActuales.forEach { familia ->
            val existente = seguimientos[familia.id]
            if (existente == null) {
                val nuevo = Seguimiento(
                    id = familia.id,
                    estado = EstadoConversacionInicio(
                        resumen = ConversacionResumen(
                            id = familia.id,
                            tipo = ConversacionResumen.TIPO_FAMILIA,
                            titulo = familia.nombre,
                            familiaId = familia.id),
                        cargandoNombre = false,
                        cargandoUltimoMensaje = true))
                seguimientos[familia.id] = nuevo
                escucharUltimoMensaje(nuevo)
                escucharLectura(nuevo)
            } else {
                existente.estado = existente.estado.copy(
                    resumen = existente.estado.resumen.copy(
                        titulo = familia.nombre)) }
        }
        publicar()
    }

    private fun escucharUltimoMensaje(seguimiento: Seguimiento) {
        val claveEspera = "ultimo:${seguimiento.id}"
        programarEspera(claveEspera) {
            if (estaVigente(seguimiento)) {
                seguimiento.estado = seguimiento.estado.copy(
                    cargandoUltimoMensaje = false,
                    errorUltimoMensajeRecurso = recursoError(
                        CodigoErrorChat.TIEMPO_AGOTADO))
                publicar()
            }
        }
        seguimiento.escuchaUltimoMensaje =
            ultimoMensajeRepository.escucharUltimoMensaje(
                familiaId = seguimiento.id) { resultado ->
                if (!estaVigente(seguimiento)) { return@escucharUltimoMensaje }
                cancelarEspera(claveEspera)
                resultado.onSuccess { mensaje ->
                    seguimiento.ultimoMensajeConfirmado = true
                    seguimiento.estado = seguimiento.estado.copy(
                        resumen = seguimiento.estado.resumen.copy(
                            ultimoMensaje = mensaje?.contenido.orEmpty(),
                            ultimoRemitenteId = mensaje?.remitenteId.orEmpty(),
                            nombreUltimoRemitente = mensaje?.nombreRemitente.orEmpty(),
                            fechaUltimoMensaje = mensaje?.fechaEnvio),
                        cargandoUltimoMensaje = false,
                        errorUltimoMensajeRecurso = null)
                    actualizarConteo(seguimiento)
                }.onFailure { error ->
                    seguimiento.estado = seguimiento.estado.copy(
                        cargandoUltimoMensaje = false,
                        errorUltimoMensajeRecurso =
                            obtenerRecursoErrorChat(error)) }
                publicar()
            }
    }

    private fun escucharLectura(seguimiento: Seguimiento) {
        val claveEspera = "lectura:${seguimiento.id}"
        programarEspera(claveEspera) {
            if (estaVigente(seguimiento)) {
                seguimiento.versionConteo++
                cancelarEspera("conteo:${seguimiento.id}")
                seguimiento.estado = seguimiento.estado.copy(
                    actualizandoConteo = false,
                    errorConteoRecurso = recursoError(
                        CodigoErrorChat.TIEMPO_AGOTADO))
                publicar()
            }
        }
        seguimiento.escuchaLectura = lecturaRepository.escucharLectura(
            familiaId = seguimiento.id
        ) { resultado ->
            if (!estaVigente(seguimiento)) { return@escucharLectura }
            cancelarEspera(claveEspera)
            resultado.onSuccess { lectura ->
                seguimiento.lectura = lectura
                actualizarConteo(seguimiento)
            }.onFailure { error ->
                seguimiento.lectura = null
                seguimiento.versionConteo++
                cancelarEspera("conteo:${seguimiento.id}")
                seguimiento.estado = seguimiento.estado.copy(
                    actualizandoConteo = false,
                    errorConteoRecurso = obtenerRecursoErrorChat(error))
                publicar()
            }
        }
    }

    private fun actualizarConteo(seguimiento: Seguimiento) {
        if (!estaVigente(seguimiento) ||
            !seguimiento.ultimoMensajeConfirmado
        ) { return }
        val lectura = seguimiento.lectura ?: return
        seguimiento.versionConteo++
        val versionActual = seguimiento.versionConteo
        val claveEspera = "conteo:${seguimiento.id}"
        seguimiento.estado = seguimiento.estado.copy(
            actualizandoConteo = true,
            errorConteoRecurso = null)
        publicar()
        programarEspera(claveEspera) {
            if (estaVigente(seguimiento) &&
                seguimiento.versionConteo == versionActual) {
                seguimiento.versionConteo++
                seguimiento.estado = seguimiento.estado.copy(
                    actualizandoConteo = false,
                    errorConteoRecurso = recursoError(
                        CodigoErrorChat.TIEMPO_AGOTADO))
                publicar() }
        }

        noLeidosRepository.contarFamilia(familiaId = seguimiento.id,
            lectura = lectura) { resultado ->
            if (!estaVigente(seguimiento) || seguimiento.versionConteo != versionActual) {
                return@contarFamilia }
            cancelarEspera(claveEspera)
            resultado.onSuccess { cantidad ->
                seguimiento.estado = seguimiento.estado.copy(
                    resumen = seguimiento.estado.resumen.copy(
                        mensajesNoLeidos = cantidad),
                    conteoConfirmado = true,
                    actualizandoConteo = false,
                    errorConteoRecurso = null)
            }.onFailure { error ->
                seguimiento.estado = seguimiento.estado.copy(
                    actualizandoConteo = false,
                    errorConteoRecurso = obtenerRecursoErrorChat(error))
            }
            publicar()
        }
    }

    fun reintentarFamilia(familiaId: String) {
        val anterior = seguimientos[familiaId] ?: return
        if (!estaVigente(anterior)) { return }
        eliminarSeguimiento(familiaId)
        val nuevo = Seguimiento(
            id = anterior.id,
            estado = anterior.estado.copy(
                cargandoUltimoMensaje = true,
                actualizandoConteo = false,
                errorUltimoMensajeRecurso = null,
                errorConteoRecurso = null
            ))
        seguimientos[familiaId] = nuevo
        publicar()
        escucharUltimoMensaje(nuevo)
        escucharLectura(nuevo)
    }
    private fun publicar() {
        conversaciones = seguimientos.values
            .map { it.estado }
            .sortedWith(compareByDescending<EstadoConversacionInicio> {
                    it.resumen.fechaUltimoMensaje
                }.thenBy { it.resumen.titulo.lowercase()
                }.thenBy { it.resumen.id }) }
    private fun estaVigente(seguimiento: Seguimiento): Boolean {
        return uidSesion != null &&
                authRepository.obtenerUsuarioActual()?.uid == uidSesion &&
                seguimientos[seguimiento.id] === seguimiento
    }
    private fun eliminarSeguimiento(id: String) {
        val seguimiento = seguimientos.remove(id) ?: return
        seguimiento.versionConteo++
        seguimiento.escuchaUltimoMensaje?.remove()
        seguimiento.escuchaLectura?.remove()
        cancelarEspera("ultimo:$id")
        cancelarEspera("lectura:$id")
        cancelarEspera("conteo:$id") }

    private fun programarEspera(clave: String,
        alAgotar: () -> Unit) { cancelarEspera(clave)
        val tarea = Runnable {
            esperas.remove(clave)
            alAgotar() }
        esperas[clave] = tarea
        handler.postDelayed(tarea, TIEMPO_ESPERA_MS) }
    private fun cancelarEspera(clave: String) {
        val tarea = esperas.remove(clave) ?: return
        handler.removeCallbacks(tarea)}
    private fun recursoError(codigo: CodigoErrorChat): Int {
        return obtenerRecursoErrorChat(ExcepcionChat(codigo)) }
    fun detener() {
        seguimientos.keys.toList().forEach {
            eliminarSeguimiento(it) }
        esperas.values.forEach {
            handler.removeCallbacks(it) }
        esperas.clear()

        uidSesion = null
        conversaciones = emptyList()
        errorSesionRecurso = null
    }
    override fun onCleared() {
        detener()
        super.onCleared() }
    companion object {
        private const val TIEMPO_ESPERA_MS = 10_000L
    }
}