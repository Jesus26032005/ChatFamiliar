package com.example.chatfamiliar.ui.auth.verification

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.chatfamiliar.R
import com.example.chatfamiliar.data.auth.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException


class VerificacionViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    var correoUsuario by mutableStateOf("")
        private set
    @get:StringRes
    var errorRecurso by mutableStateOf<Int?>(null)
        private set
    @get:StringRes
    var mensajeRecurso by mutableStateOf<Int?>(null)
        private set
    var cargando by mutableStateOf(false)
        private set
    private var correoInicialEnviado =
        false

    fun prepararPantalla() {
        correoUsuario = authRepository.obtenerCorreoActual().orEmpty()

        if (!correoInicialEnviado) {
            correoInicialEnviado = true
            enviarCorreoVerificacion()
        }
    }

    fun reenviarCorreo() {
        if (cargando) { return }
        enviarCorreoVerificacion()
    }

    fun comprobarVerificacion(alEstarVerificado: () -> Unit) {
        if (cargando) { return }
        cargando = true
        errorRecurso = null
        mensajeRecurso = null
        authRepository
            .comprobarCorreoVerificado { resultado ->
                cargando = false
                resultado
                    .onSuccess { estaVerificado ->
                        if (estaVerificado) {
                            alEstarVerificado()
                        } else {
                            mensajeRecurso = R.string
                                .verification_message_not_verified
                        }
                    }
                    .onFailure { excepcion ->
                        errorRecurso = obtenerMensajeError(excepcion)
                    }
            }
    }

    fun cerrarSesion(alCerrarSesion: () -> Unit) {
        authRepository.cerrarSesion()
        alCerrarSesion()
    }

    private fun enviarCorreoVerificacion() {
        if (cargando) { return }
        cargando = true
        errorRecurso = null
        mensajeRecurso = null
        authRepository.enviarCorreoVerificacion { resultado ->
                cargando = false
                resultado
                    .onSuccess {
                        mensajeRecurso = R.string
                            .verification_message_email_sent
                    }
                    .onFailure { excepcion ->
                        errorRecurso = obtenerMensajeError(excepcion)
                    }
            }
    }

    @StringRes
    private fun obtenerMensajeError(excepcion: Throwable): Int {
        return when (excepcion) {
            is FirebaseNetworkException -> {
                R.string.verification_error_network }
            is FirebaseTooManyRequestsException -> { R.string
                .verification_error_too_many_requests }
            is IllegalStateException -> {
                R.string.verification_error_no_session }
            else -> {
                R.string.verification_error_generic }
        }
    }
}