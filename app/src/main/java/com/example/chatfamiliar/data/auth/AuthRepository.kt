package com.example.chatfamiliar.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/** Acceso a Firebase Authentication: registro, verificación de correo, inicio y cierre de sesión. */
class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    /** Crea la cuenta con correo y contraseña y devuelve el usuario de Firebase creado. */
    fun crearUsuario(correo: String,
                     password: String,
                     alCompletar: (Result<FirebaseUser>) -> Unit) { auth.createUserWithEmailAndPassword(correo,
            password)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) { val usuario = tarea.result?.user
                    if (usuario != null) { alCompletar(Result.success(usuario)) } else { alCompletar(Result.failure(
                            IllegalStateException(
                                "No se pudo obtener el usuario creado."))) } } else { val excepcion = tarea.exception ?:
                    Exception("No se pudo crear la cuenta.")
                    alCompletar(Result.failure(excepcion)) } } }

    /** Envía el correo de verificación al usuario que tiene la sesión abierta. */
    fun enviarCorreoVerificacion(alCompletar:
                                     (Result<Unit>) -> Unit) { val usuario = auth.currentUser
        if (usuario == null) { alCompletar(Result.failure(IllegalStateException(
                    "No existe un usuario autenticado.")))
            return }
        usuario.sendEmailVerification()
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) { alCompletar(Result.success(Unit)) } else { val excepcion = tarea.exception ?:
                    Exception("No se pudo enviar el correo " +
                            "de verificación.")
                    alCompletar(Result.failure(excepcion)) } } }

    /** Recarga el usuario desde Firebase y devuelve si ya confirmó su correo. */
    fun comprobarCorreoVerificado(alCompletar:
                                      (Result<Boolean>)
                                  -> Unit) { val usuario = auth.currentUser
        if (usuario == null) { alCompletar(Result.failure(IllegalStateException(
                    "No existe un usuario autenticado.")))
            return }
        usuario.reload().addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) { val correoVerificado =
                        auth.currentUser
                            ?.isEmailVerified == true
                    alCompletar(Result
                        .success(correoVerificado)) } else { val excepcion = tarea
                        .exception ?:
                        Exception("No se pudo " +
                                "comprobar la verificación.")
                    alCompletar(Result.failure(excepcion)) } } }

    /** Inicia sesión con correo y contraseña. */
    fun iniciarSesion(correo: String,
                      password: String,
                      alCompletar: (Result<FirebaseUser>)
                      -> Unit) { auth.signInWithEmailAndPassword(correo,
            password).addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) { val usuario = tarea.result?.user
                    if (usuario != null) { alCompletar(Result.success(usuario)) } else { alCompletar(Result.failure(
                            IllegalStateException(
                                "No se pudo obtener el " +
                                        "usuario autenticado."))) } } else { val excepcion = tarea.exception ?:
                    Exception("No se pudo iniciar sesión.")
                    alCompletar(Result.failure(excepcion)) } } }
    /** Cierra la sesión de Firebase en este dispositivo. */
    fun cerrarSesion() { auth.signOut() }
    /** Usuario con sesión abierta, o null si no hay sesión. */
    fun obtenerUsuarioActual(): FirebaseUser? { return auth
        .currentUser }
    /** Correo del usuario con sesión abierta, o null si no hay sesión. */
    fun obtenerCorreoActual(): String? { return auth
        .currentUser?.email } }