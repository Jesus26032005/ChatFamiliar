package com.example.chatfamiliar.ui.auth.register

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.chatfamiliar.data.auth.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class RegistroViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    var correo by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmarPassword by mutableStateOf("")
        private set
    var errorVisual by mutableStateOf<String?>(null)
        private set
    var cargando by mutableStateOf(false)
        private set
    fun actualizarCorreo(nuevoCorreo: String) {
        correo = nuevoCorreo }
    fun actualizarPassword(nuevoPassword: String) {
        password = nuevoPassword }
    fun actualizarConfirmarPassword(nuevoPassword: String) {
        confirmarPassword = nuevoPassword }

    fun crearCuenta(alTenerExito: () -> Unit) {
        if (cargando) return
        val correoLimpio = correo.trim()
        if (!validarFormulario(correoLimpio)) return

        cargando = true
        errorVisual = null
        authRepository.crearUsuario(correo = correoLimpio,
            password = password) { resultado ->
            cargando = false
            resultado
                .onSuccess { alTenerExito() }
                .onFailure { excepcion -> errorVisual =
                    obtenerMensajeError(excepcion) }
        }
    }
    private fun validarFormulario(correo: String): Boolean {
        if (correo.isBlank() || password.isBlank()
            || confirmarPassword.isBlank()) {
            errorVisual = "Por favor, completa" +
                    " todos los campos."
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo)
            .matches()) {
            errorVisual = "Por favor, ingresa " +
                    "un correo electrónico válido."
            return false
        }
        if (password.length < 6) {
            errorVisual = "La contraseña debe tener al " +
                    "menos 6 caracteres."
            return false
        }
        if (password != confirmarPassword) {
            errorVisual =
                "Las contraseñas no coinciden."
            return false
        }
        return true
    }

    private fun obtenerMensajeError(excepcion: Throwable): String {
        return when (excepcion) {
            is FirebaseNetworkException ->
                "Sin conexión a internet. Verifica tu red " +
                        "e inténtalo de nuevo."
            is FirebaseTooManyRequestsException ->
                "Demasiados intentos. Espera un momento " +
                        "e inténtalo de nuevo."
            is FirebaseAuthUserCollisionException ->
                "Este correo ya está registrado. Inicia sesión" +
                        " o utiliza otro."
            is FirebaseAuthWeakPasswordException ->
                "La contraseña es demasiado débil. Utiliza una " +
                        "contraseña más segura."
            is FirebaseAuthInvalidCredentialsException ->
                "El formato del correo electrónico no es válido."
            else -> "No se pudo crear la cuenta. Inténtalo de nuevo."
        }
    }
}