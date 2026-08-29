package com.example.chatfamiliar.ui.auth
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {
    private val authObjeto = FirebaseAuth.getInstance()
    var correo by mutableStateOf("")
    var password by mutableStateOf("")

    var errorVisual by mutableStateOf<String?>(null)
        private set

    var cargando by mutableStateOf(false)
        private set

    fun iniciarSesion(alTenerExito: () -> Unit) {
        val correoLimpio = correo.trim()
        if (cargando) return
        if (!validarFormulario()) return

        cargando = true
        errorVisual = null

        authObjeto.signInWithEmailAndPassword(correoLimpio, password).addOnCompleteListener { tarea ->
                cargando = false
                if (tarea.isSuccessful) {
                    alTenerExito()
                } else {
                    errorVisual = when (tarea.exception) {
                        is com.google.firebase.FirebaseNetworkException -> "Sin conexión a internet. Verifica tu red e inténtalo de nuevo."
                        is com.google.firebase.FirebaseTooManyRequestsException -> "Demasiados intentos. Espera un momento e inténtalo de nuevo."
                        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos. Verifica tus datos."
                        is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "Correo o contraseña incorrectos. Verifica tus datos."
                        else -> "Ocurrió un error al iniciar sesión. Inténtalo de nuevo."
                    }
                }
            }
    }

    private fun validarFormulario(): Boolean{
        if (correo.isBlank() || password.isBlank()) {
            errorVisual = "Por favor, ingresa tu correo y contraseña."
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            errorVisual = "Por favor, ingrese un correo valido."
            return false
        }
        return true
    }
}