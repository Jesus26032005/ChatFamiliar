package com.example.chatfamiliar.data.llamada

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import io.getstream.video.android.core.StreamVideo
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SesionVideoRepository(context: Context) {
    private val contextoAplicacion = context.applicationContext
    private val auth = FirebaseAuth.getInstance()
    private val _cliente = MutableStateFlow<StreamVideo?>(null)
    val cliente: StateFlow<StreamVideo?> = _cliente.asStateFlow()

    @Synchronized
    fun inicializar(uid: String, nombre: String): Result<StreamVideo> {
        return runCatching {
            check(uid.isNotBlank() &&
                    auth.currentUser?.uid == uid) { "SESION_FIREBASE_NO_COINCIDE" }

            val nombreVisible = nombre.trim()
            require(nombreVisible.isNotEmpty()) { "NOMBRE_REQUERIDO" }
            val apiKey = ConfiguracionStream.API_KEY.trim()

            check(apiKey.isNotEmpty() && apiKey != "PEGA_AQUI_TU_API_KEY") {
                "API_KEY_STREAM_NO_CONFIGURADA" }
            val clienteActual = StreamVideo.instanceOrNull()
            if (clienteActual?.userId == uid) {
                _cliente.value = clienteActual
                return@runCatching clienteActual }

            if (clienteActual != null) { StreamVideo.removeClient() }

            val usuarioStream = User(id = uid, name = nombreVisible)

            StreamVideoBuilder(
                context = contextoAplicacion,
                apiKey = apiKey,
                user = usuarioStream,
                token = StreamVideo.devToken(uid)
            ).build().also { nuevo -> _cliente.value = nuevo }
        }
    }

    @Synchronized
    fun obtenerClienteActual(): StreamVideo? {
        val uid = auth.currentUser?.uid ?: return null

        return StreamVideo.instanceOrNull()
            ?.takeIf { cliente ->
                cliente.userId == uid
            }
    }

    @Synchronized
    fun cerrarSesion() {
        _cliente.value = null
        if (StreamVideo.instanceOrNull() != null) {
            StreamVideo.removeClient()
        }
    }
}