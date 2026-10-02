package com.example.chatfamiliar

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.chatfamiliar.navigation.AppNavigation
import com.example.chatfamiliar.ui.llamada.LlamadaViewModel
import com.example.chatfamiliar.ui.theme.ChatFamiliarTheme
import io.getstream.video.android.core.notifications.NotificationHandler

class MainActivity : AppCompatActivity() {
    // Mismo ViewModel que usa AppNavigation (ambos con alcance de la actividad).
    private val llamadaViewModel: LlamadaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) { atenderIntentLlamada(intent) }
        setContent {
            ChatFamiliarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) { AppNavigation(llamadaViewModel = llamadaViewModel) }
            }
        }
    }

    // Al volver a la app revisamos si alguien nos está marcando y no
    // nos llegó el aviso (por ejemplo, la app estaba en segundo plano).
    override fun onResume() {
        super.onResume()
        llamadaViewModel.revisarLlamadaEntrantePendiente()
    }

    // La actividad es singleTop: al tocar la notificación de llamada con
    // la app abierta, llega aquí en lugar de crear otra pantalla.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        atenderIntentLlamada(intent)
    }

    /**
     * La notificación de llamada entrante de Stream abre esta actividad.
     * Con "Contestar" marcamos que se debe aceptar al mostrar la llamada;
     * con tocar la notificación basta con abrir la app, la capa de
     * llamadas ya muestra la pantalla de llamada entrante.
     */
    private fun atenderIntentLlamada(intent: Intent?) {
        if (intent?.action == NotificationHandler.ACTION_ACCEPT_CALL) {
            llamadaViewModel.marcarAceptarDesdeNotificacion()
        }
    }
}