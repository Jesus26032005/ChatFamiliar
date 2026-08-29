package com.example.chatfamiliar.ui.auth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import android.util.Patterns

class RegistroViewModel : ViewModel() {
    private val authObjeto = FirebaseAuth.getInstance()
    var correo by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmarPassword by mutableStateOf("")
    var errorVisual by mutableStateOf<String?>(null)
        private set
    var cargando by mutableStateOf(false)
        private set

    fun crearCuenta(alTenerExito: () -> Unit) {
        val correoLimpio = correo.trim()

        if (cargando) return
        if (!validarFormulario(correoLimpio)) return

        cargando = true
        errorVisual = null

        authObjeto.createUserWithEmailAndPassword(correoLimpio, password).addOnCompleteListener { tarea ->
                cargando = false
                if (tarea.isSuccessful) {
                    alTenerExito()
                } else {
                    errorVisual = when (tarea.exception) {
                        is com.google.firebase.FirebaseNetworkException -> "Sin conexión a internet. Verifica tu red e inténtalo de nuevo."
                        is com.google.firebase.FirebaseTooManyRequestsException -> "Demasiados intentos. Espera un momento e inténtalo de nuevo."
                        is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "Este correo ya está registrado. Inicia sesión o usa otro."
                        is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil. Usa al menos 6 caracteres."
                        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "El formato del correo electrónico no es válido."
                        else -> "No se pudo crear la cuenta. Inténtalo de nuevo."
                    }
                }
            }
    }

    private fun validarFormulario(correo: String): Boolean {
        if (correo.isBlank() || password.isBlank() || confirmarPassword.isBlank()) {
            errorVisual = "Por favor, rellena todos los campos del formulario."
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            errorVisual = "Por favor, ingrese un correo valido."
            return false
        }
        if (password != confirmarPassword) {
            errorVisual = "Las contraseñas no son iguales. Intenta de nuevo."
            return false
        }
        if (password.length < 6) {
            errorVisual = "La contraseña debe tener al menos 6 caracteres."
            return false
        }
        return true
    }

}


