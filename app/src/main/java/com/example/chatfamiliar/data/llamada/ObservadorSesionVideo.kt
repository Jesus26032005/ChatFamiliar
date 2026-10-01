package com.example.chatfamiliar.data.llamada

import android.util.Log
import com.example.chatfamiliar.data.user.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

class ObservadorSesionVideo(
    private val sesionVideoRepository: SesionVideoRepository,
    private val usuarioRepository: UsuarioRepository
) {

    private val auth = FirebaseAuth.getInstance()

    private var observadorAuth: FirebaseAuth.AuthStateListener? = null
    private var observadorPerfil: ListenerRegistration? = null

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

        sesionVideoRepository.cerrarSesion()

        if (uidActual == null) {
            Log.d(ETIQUETA, "SIN_SESION_FIREBASE_CLIENTE_RETIRADO")
            return
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

    companion object {
        private const val ETIQUETA = "SesionVideo"
    }
}