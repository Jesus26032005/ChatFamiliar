package com.example.chatfamiliar.data.llamada

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import io.getstream.android.push.firebase.FirebasePushDeviceGenerator
import io.getstream.video.android.core.StreamVideo
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.core.notifications.NotificationConfig
import io.getstream.video.android.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class SesionVideoRepository(context: Context) {
    private val contextoAplicacion = context.applicationContext
    private val auth = FirebaseAuth.getInstance()

    // Guarda uid y nombre en el teléfono para poder crear el cliente de
    // Stream al instante cuando llega una llamada con la app cerrada.
    private val preferencias = contextoAplicacion.getSharedPreferences(
        ARCHIVO_PREFERENCIAS, Context.MODE_PRIVATE)

    // Cliente de Stream observable: la interfaz de llamadas se entera
    // cuando se crea (al iniciar sesión) o se retira (al cerrarla).
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
                guardarSesionLocal(uid = uid, nombre = nombreVisible)
                return@runCatching clienteActual }

            if (clienteActual != null) { StreamVideo.removeClient() }

            val usuarioStream = User(id = uid, name = nombreVisible)

            StreamVideoBuilder(
                context = contextoAplicacion,
                apiKey = apiKey,
                user = usuarioStream,
                token = StreamVideo.devToken(uid),
                // Push: registra este teléfono en Stream con el proveedor
                // "firebase" (el mismo nombre que en el dashboard) para
                // que las llamadas lleguen aunque la app esté cerrada.
                notificationConfig = NotificationConfig(
                    pushDeviceGenerators = listOf(
                        FirebasePushDeviceGenerator(
                            context = contextoAplicacion,
                            providerName = PROVEEDOR_PUSH
                        )
                    ),
                    // Con la app abierta ya mostramos nuestra propia
                    // pantalla de llamada entrante (CapaLlamadas).
                    hideRingingNotificationInForeground = true
                )
            ).build().also { nuevo ->
                _cliente.value = nuevo
                guardarSesionLocal(uid = uid, nombre = nombreVisible)
            }
        }
    }

    /**
     * Crea el cliente con los datos guardados en el teléfono, sin esperar
     * a Firestore. Se llama en Application.onCreate: si la app se abrió
     * por una notificación de llamada, el SDK necesita el cliente ya listo.
     * Devuelve null si no hay sesión o no hay datos guardados de este usuario.
     */
    @Synchronized
    fun inicializarDesdeDatosGuardados(): Result<StreamVideo>? {
        val uid = auth.currentUser?.uid ?: return null
        if (preferencias.getString(CLAVE_UID, null) != uid) return null
        val nombre = preferencias.getString(CLAVE_NOMBRE, null)
            ?.takeIf { it.isNotBlank() } ?: return null
        return inicializar(uid = uid, nombre = nombre)
    }

    @Synchronized
    fun obtenerClienteActual(): StreamVideo? {
        val uid = auth.currentUser?.uid ?: return null

        return StreamVideo.instanceOrNull()
            ?.takeIf { cliente ->
                cliente.userId == uid
            }
    }

    /** Nombre con el que el cliente actual se presentó a Stream. */
    @Synchronized
    fun nombreConectado(): String? = obtenerClienteActual()?.user?.name

    /**
     * Stream no tiene una función en el cliente para renombrar al usuario:
     * el nombre se envía al conectarse. Por eso, para usar el nombre
     * nuevo, se cierra el cliente y se crea otro con el mismo uid.
     * Solo debe llamarse cuando no hay ninguna llamada sonando ni en curso.
     */
    @Synchronized
    fun reconectarConNombre(uid: String, nombre: String): Result<StreamVideo> {
        cerrarSesion()
        return inicializar(uid = uid, nombre = nombre)
    }

    /**
     * Al cerrar sesión en la app: quita este teléfono de Stream para que
     * ya no le lleguen llamadas de esa cuenta, borra los datos guardados
     * y cierra el cliente. Si no hay red, a los pocos segundos se cierra
     * de todos modos.
     */
    suspend fun cerrarSesionDeUsuario() {
        olvidarSesionLocal()
        val clienteAnterior = StreamVideo.instanceOrNull()
        _cliente.value = null
        if (clienteAnterior == null) return

        withTimeoutOrNull(TIEMPO_BAJA_DISPOSITIVO_MS) {
            clienteAnterior.getDevice().first()?.let { dispositivo ->
                clienteAnterior.deleteDevice(dispositivo)
            }
        }
        clienteAnterior.logOut()

        // Solo se retira si sigue siendo el mismo cliente (por si en
        // estos segundos ya inició sesión otra cuenta).
        synchronized(this) {
            if (StreamVideo.instanceOrNull() === clienteAnterior) {
                StreamVideo.removeClient()
            }
        }
    }

    @Synchronized
    fun cerrarSesion() {
        _cliente.value = null
        if (StreamVideo.instanceOrNull() != null) {
            StreamVideo.removeClient()
        }
    }

    private fun guardarSesionLocal(uid: String, nombre: String) {
        preferencias.edit()
            .putString(CLAVE_UID, uid)
            .putString(CLAVE_NOMBRE, nombre)
            .apply()
    }

    private fun olvidarSesionLocal() {
        preferencias.edit().clear().apply()
    }

    private companion object {
        const val PROVEEDOR_PUSH = "firebase"
        const val ARCHIVO_PREFERENCIAS = "sesion_video"
        const val CLAVE_UID = "uid"
        const val CLAVE_NOMBRE = "nombre"
        const val TIEMPO_BAJA_DISPOSITIVO_MS = 5_000L
    }
}