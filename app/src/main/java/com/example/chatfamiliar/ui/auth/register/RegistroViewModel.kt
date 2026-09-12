package com.example.chatfamiliar.ui.auth.register

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.chatfamiliar.R
import com.example.chatfamiliar.data.auth.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException


class RegistroViewModel : ViewModel() {
    private val authRepository =
        AuthRepository()

    var correo by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var confirmarPassword by mutableStateOf("")
        private set

    @get:StringRes
    var errorRecurso by mutableStateOf<Int?>(null)
        private set

    var cargando by mutableStateOf(false)
        private set

    fun actualizarCorreo(nuevoCorreo: String) { correo = nuevoCorreo
        errorRecurso = null
    }

    fun actualizarPassword(nuevoPassword: String) {
        password = nuevoPassword
        errorRecurso = null
    }

    fun actualizarConfirmarPassword(nuevoPassword: String) {
        confirmarPassword = nuevoPassword
        errorRecurso = null
    }

    fun crearCuenta(alTenerExito: () -> Unit) {
        if (cargando) { return }
        val correoLimpio = correo.trim()

        if (!validarFormulario(correoLimpio)) { return }
        cargando = true
        errorRecurso = null

        authRepository.crearUsuario(correo = correoLimpio,
            password = password) { resultado ->
            cargando = false
            resultado
                .onSuccess {
                    alTenerExito()
                }
                .onFailure { excepcion ->
                    errorRecurso = obtenerMensajeError(excepcion
                    )
                }
        }
    }

    private fun validarFormulario(
        correo: String
    ): Boolean {
        if (correo.isBlank() || password.isBlank() ||
            confirmarPassword.isBlank()) {
            errorRecurso = R.string.register_error_empty_fields
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            errorRecurso = R.string.register_error_invalid_email
            return false
        }
        if (password.length < 6) {
            errorRecurso = R.string.register_error_short_password
            return false
        }
        if (password != confirmarPassword) {
            errorRecurso = R.string.register_error_password_mismatch
            return false
        }
        return true
    }

    @StringRes
    private fun obtenerMensajeError(
        excepcion: Throwable
    ): Int {
        return when (excepcion) {
            is FirebaseNetworkException -> { R.string.register_error_network }
            is FirebaseTooManyRequestsException -> { R.string.register_error_too_many_requests }
            is FirebaseAuthUserCollisionException -> { R.string.register_error_email_in_use }
            is FirebaseAuthWeakPasswordException -> { R.string.register_error_weak_password}
            is FirebaseAuthInvalidCredentialsException -> { R.string.register_error_invalid_email_firebase }
            else -> { R.string.register_error_generic }
        }
    }
}