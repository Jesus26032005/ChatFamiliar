package com.example.chatfamiliar

import android.app.Application
import android.util.Log
import com.example.chatfamiliar.data.llamada.ObservadorSesionVideo
import com.example.chatfamiliar.data.llamada.SesionVideoRepository
import com.example.chatfamiliar.data.user.UsuarioRepository

/**
 * Clase Application de ChatFamiliar.
 *
 * Crea una sola vez el repositorio de la sesión de video (Stream) y el
 * observador que lo mantiene sincronizado con la sesión de Firebase, para
 * que las videollamadas existan a nivel de aplicación y no dependan de
 * ninguna pantalla.
 */
class ChatFamiliarApplication : Application() {

    lateinit var sesionVideoRepository: SesionVideoRepository
        private set

    private lateinit var observadorSesionVideo: ObservadorSesionVideo

    override fun onCreate() { super.onCreate()

        sesionVideoRepository = SesionVideoRepository(context = this)

        sesionVideoRepository.inicializarDesdeDatosGuardados()
            ?.onSuccess { Log.d(ETIQUETA, "CLIENTE_STREAM_CREADO_AL_ABRIR") }
            ?.onFailure { error ->
                Log.e(ETIQUETA, "ERROR_CLIENTE_AL_ABRIR: " +
                        error.javaClass.simpleName + " " + error.message) }

        observadorSesionVideo = ObservadorSesionVideo(sesionVideoRepository = sesionVideoRepository,
            usuarioRepository = UsuarioRepository())

        observadorSesionVideo.iniciar() }

    private companion object {
        const val ETIQUETA = "SesionVideo"
    } }