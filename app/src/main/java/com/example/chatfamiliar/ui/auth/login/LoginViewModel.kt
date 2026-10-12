package com.example.chatfamiliar.ui.auth.login

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
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import androidx.lifecycle.viewModelScope
import com.example.chatfamiliar.util.TimeoutSolicitud

/** Estado y lógica de la pantalla de inicio de sesión. */
class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val timeoutLogin = TimeoutSolicitud()
    var correo by mutableStateOf("")
        private set


    var password by mutableStateOf("")
        private set

    @get:StringRes
    var errorRecurso by mutableStateOf<Int?>(null)
        private set

    var cargando by mutableStateOf(false)
        private set

    fun actualizarCorreo(nuevoCorreo: String) { correo = nuevoCorreo }

    fun actualizarPassword(nuevoPassword: String) { password = nuevoPassword }

    /**
     * Valida el formulario e inicia sesión. Si el correo no está verificado
     * envía a la pantalla de verificación. Si la respuesta llega después del
     * tiempo límite, se cierra la sesión para no dejarla abierta a medias.
     */
    fun iniciarSesion(alTenerExito: () -> Unit,
        alRequerirVerificacion: () -> Unit) { if (cargando) { return }

        val correoLimpio = correo.trim()

        if (!validarFormulario(correoLimpio)) { return }
        cargando = true
        errorRecurso = null

        val solicitud = timeoutLogin.iniciar(scope = viewModelScope) { cargando = false
                errorRecurso = R.string.error_network }

        authRepository.iniciarSesion(correo = correoLimpio, password = password) { resultado ->
            if (!timeoutLogin.completar(solicitud)) { authRepository.cerrarSesion()
                return@iniciarSesion }

            cargando = false
            resultado
                .onSuccess { usuario ->
                    if (usuario.isEmailVerified) { alTenerExito() } else { alRequerirVerificacion() } }
                .onFailure { excepcion ->
                    errorRecurso = obtenerMensajeError(excepcion) } } }


    /** Revisa campos vacíos y formato del correo antes de llamar a Firebase. */
    private fun validarFormulario(correo: String): Boolean { if (correo.isBlank() || password.isBlank()) {
            errorRecurso = R.string.login_error_empty_fields
            return false }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) { errorRecurso = R.string.login_error_invalid_email
            return false }
        return true }

    /** Traduce la excepción de Firebase al texto que verá el usuario. */
    @StringRes
    private fun obtenerMensajeError(excepcion: Throwable): Int { return when (excepcion) {
            is FirebaseNetworkException -> { R.string.login_error_network }
            is FirebaseTooManyRequestsException -> { R.string.login_error_too_many_requests }
            is FirebaseAuthInvalidCredentialsException,
            is FirebaseAuthInvalidUserException -> { R.string.login_error_invalid_credentials }
            else -> { R.string.login_error_generic } } } }