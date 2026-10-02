package com.example.chatfamiliar.data.llamada

import android.util.Log
import com.example.chatfamiliar.data.user.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ObservadorSesionVideo(
    private val sesionVideoRepository: SesionVideoRepository,
    private val usuarioRepository: UsuarioRepository
) {

    private val auth = FirebaseAuth.getInstance()

    // Vive lo mismo que la app (este observador se crea en la Application).
    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var observadorAuth: FirebaseAuth.AuthStateListener? = null
    private var observadorPerfil: ListenerRegistration? = null
    private var cambioNombrePendiente: Job? = null

    private var uidObservado: String? = null
    private var sesionRevisada = false
    private var versionSesion = 0L

    fun iniciar() {
        if (observadorAuth != null) {
            return
        }

        val observador = FirebaseAuth.AuthStateListener {
            actualizarSesion()
        }

        observadorAuth = observador
        auth.addAuthStateListener(observador)
    }

    private fun actualizarSesion() {
        val uidActual = auth.currentUser?.uid

        if (sesionRevisada && uidActual == uidObservado) {
            return
        }

        sesionRevisada = true
        uidObservado = uidActual
        versionSesion += 1

        observadorPerfil?.remove()
        observadorPerfil = null
        cambioNombrePendiente?.cancel()
        cambioNombrePendiente = null

        if (uidActual == null) {
            // Cerró sesión: quitamos este teléfono de Stream para que no
            // le sigan llegando llamadas de esa cuenta.
            alcance.launch {
                sesionVideoRepository.cerrarSesionDeUsuario()
                Log.d(ETIQUETA, "SIN_SESION_FIREBASE_CLIENTE_RETIRADO")
            }
            return
        }

        // Si el cliente ya se creó al abrir la app (desde los datos
        // guardados) y es de este mismo usuario, se conserva: puede estar
        // atendiendo una llamada que llegó con la app cerrada.
        if (sesionVideoRepository.obtenerClienteActual() == null) {
            sesionVideoRepository.cerrarSesion()
        }

        val versionActual = versionSesion

        observadorPerfil = usuarioRepository.escucharUsuario(
            uidUsuario = uidActual
        ) { resultado ->

            if (
                versionActual != versionSesion ||
                auth.currentUser?.uid != uidActual
            ) {
                return@escucharUsuario
            }

            resultado.onSuccess { usuario ->
                val nombre = usuario?.nombre.orEmpty().trim()

                if (nombre.isEmpty()) {
                    sesionVideoRepository.cerrarSesion()

                    Log.d(
                        ETIQUETA,
                        "ESPERANDO_NOMBRE_DEL_PERFIL"
                    )

                    return@onSuccess
                }

                if (
                    sesionVideoRepository.obtenerClienteActual() != null
                ) {
                    // Ya hay cliente: si el nombre del perfil cambió,
                    // lo pasamos también a Stream (videollamadas).
                    if (sesionVideoRepository.nombreConectado() != nombre) {
                        actualizarNombreEnStream(
                            uid = uidActual,
                            nombre = nombre,
                            version = versionActual
                        )
                    }
                    return@onSuccess
                }

                sesionVideoRepository.inicializar(
                    uid = uidActual,
                    nombre = nombre
                ).onSuccess {
                    Log.d(
                        ETIQUETA,
                        "CLIENTE_STREAM_CREADO"
                    )
                }.onFailure { error ->
                    Log.e(
                        ETIQUETA,
                        "ERROR_CREACION_CLIENTE: " +
                                error.javaClass.simpleName
                    )
                }
            }.onFailure { error ->
                Log.e(
                    ETIQUETA,
                    "ERROR_LECTURA_PERFIL: " +
                            error.javaClass.simpleName
                )
            }
        }
    }

    /**
     * Reconecta a Stream con el nombre nuevo. Si en ese momento hay una
     * llamada sonando o en curso, espera a que termine para no cortarla.
     * Si el nombre vuelve a cambiar antes, se cancela la espera anterior
     * y se usa el más reciente.
     */
    private fun actualizarNombreEnStream(uid: String, nombre: String, version: Long) {
        cambioNombrePendiente?.cancel()
        cambioNombrePendiente = alcance.launch {
            val cliente = sesionVideoRepository.obtenerClienteActual() ?: return@launch

            combine(
                cliente.state.activeCall,
                cliente.state.ringingCall
            ) { activa, sonando -> activa == null && sonando == null }
                .first { libre -> libre }

            if (version != versionSesion || auth.currentUser?.uid != uid) {
                return@launch
            }

            sesionVideoRepository.reconectarConNombre(uid = uid, nombre = nombre)
                .onSuccess {
                    Log.d(ETIQUETA, "NOMBRE_STREAM_ACTUALIZADO")
                }
                .onFailure { error ->
                    Log.e(
                        ETIQUETA,
                        "ERROR_ACTUALIZAR_NOMBRE_STREAM: " +
                                error.javaClass.simpleName
                    )
                }
        }
    }

    companion object {
        private const val ETIQUETA = "SesionVideo"
    }
}