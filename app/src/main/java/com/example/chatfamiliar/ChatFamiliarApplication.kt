package com.example.chatfamiliar

import android.app.Application
import com.example.chatfamiliar.data.llamada.ObservadorSesionVideo
import com.example.chatfamiliar.data.llamada.SesionVideoRepository
import com.example.chatfamiliar.data.user.UsuarioRepository

class ChatFamiliarApplication : Application() {

    lateinit var sesionVideoRepository: SesionVideoRepository
        private set

    private lateinit var observadorSesionVideo: ObservadorSesionVideo

    override fun onCreate() {
        super.onCreate()

        sesionVideoRepository = SesionVideoRepository(
            context = this
        )

        observadorSesionVideo = ObservadorSesionVideo(
            sesionVideoRepository = sesionVideoRepository,
            usuarioRepository = UsuarioRepository()
        )

        observadorSesionVideo.iniciar()
    }
}