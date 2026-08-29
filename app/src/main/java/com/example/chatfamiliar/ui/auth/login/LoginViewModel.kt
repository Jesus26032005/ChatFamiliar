package com.example.chatfamiliar.ui.auth.login

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.chatfamiliar.data.auth.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    var correo by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var errorVisual by mutableStateOf<String?>(null)
        private set

    var cargando by mutableStateOf(false)
        private set

    fun actualizarCorreo(nuevoCorreo: String) { correo = nuevoCorreo }

    fun actualizarPassword(nuevoPassword: String) { password = nuevoPassword }

    fun iniciarSesion(alTenerExito: () -> Unit, alRequerirVerificacion: () -> Unit) {
        if (cargando) return
        val correoLimpio = correo.trim()
        if (!validarFormulario(correoLimpio)) return

        cargando = true
        errorVisual = null

        authRepository.iniciarSesion(correo = correoLimpio, password = password) { resultado ->
            cargando = false
            resultado
                .onSuccess { usuario -> if (usuario.isEmailVerified) { alTenerExito() } else { alRequerirVerificacion() } }
                .onFailure { excepcion -> errorVisual = obtenerMensajeError(excepcion) }
        }
    }

    private fun validarFormulario(correo: String): Boolean {
        if (correo.isBlank() || password.isBlank()) {
            errorVisual = "Por favor, ingresa tu correo y contraseña."
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            errorVisual = "Por favor, ingresa un correo electrónico válido."
            return false
        }
        return true
    }

    private fun obtenerMensajeError(excepcion: Throwable): String {
        return when (excepcion) {
            is FirebaseNetworkException -> "Sin conexión a internet. Verifica tu red e inténtalo de nuevo."
            is FirebaseTooManyRequestsException -> "Demasiados intentos. Espera un momento e inténtalo de nuevo."
            is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos. Verifica tus datos."
            is FirebaseAuthInvalidUserException -> "Correo o contraseña incorrectos. Verifica tus datos."
            else -> "Ocurrió un error al iniciar sesión. Inténtalo de nuevo."
        }
    }
}