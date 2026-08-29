package com.example.chatfamiliar.ui.auth.verification

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.chatfamiliar.data.auth.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException

class VerificacionViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    var correoUsuario by mutableStateOf("")
        private set

    var errorVisual by mutableStateOf<String?>(null)
        private set

    var mensajeVisual by mutableStateOf<String?>(null)
        private set

    var cargando by mutableStateOf(false)
        private set

    private var correoInicialEnviado = false

    fun prepararPantalla() {
        correoUsuario = authRepository.obtenerCorreoActual().orEmpty()
        if (!correoInicialEnviado) {
            enviarCorreoVerificacion()
            correoInicialEnviado = true
        }
    }

    fun reenviarCorreo() {
        if (cargando) return
        enviarCorreoVerificacion()
    }

    fun comprobarVerificacion(alEstarVerificado: () -> Unit) {
        if (cargando) return
        cargando = true
        errorVisual = null
        mensajeVisual = null

        authRepository.comprobarCorreoVerificado { resultado ->
            cargando = false
            resultado
                .onSuccess { estaVerificado -> if (estaVerificado) { alEstarVerificado()
                    } else { mensajeVisual = "Tu correo todavía no ha sido verificado. Abre el enlace que enviamos a tu correo e inténtalo nuevamente." } }
                .onFailure { excepcion ->
                    errorVisual = obtenerMensajeError(excepcion)
                }
        }
    }

    fun cerrarSesion(alCerrarSesion: () -> Unit) {
        authRepository.cerrarSesion()
        alCerrarSesion()
    }

    private fun enviarCorreoVerificacion() {
        if (cargando) return
        cargando = true
        errorVisual = null
        mensajeVisual = null

        authRepository.enviarCorreoVerificacion { resultado ->
            cargando = false
            resultado
                .onSuccess { mensajeVisual = "Enviamos un enlace de verificación a tu correo electrónico." }
                .onFailure { excepcion -> errorVisual = obtenerMensajeError(excepcion)
                }
        }
    }

    private fun obtenerMensajeError(excepcion: Throwable): String {
        return when (excepcion) {
            is FirebaseNetworkException -> "Sin conexión a internet. Verifica tu red e inténtalo de nuevo."
            is FirebaseTooManyRequestsException -> "Se realizaron demasiados intentos. Espera un momento antes de volver a intentarlo."
            is IllegalStateException -> "No existe una sesión activa. Inicia sesión nuevamente."
            else -> "Ocurrió un error al verificar tu cuenta. Inténtalo de nuevo."
        }
    }
}