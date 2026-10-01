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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        atenderIntentLlamada(intent)
    }
    private fun atenderIntentLlamada(intent: Intent?) {
        if (intent?.action == NotificationHandler.ACTION_ACCEPT_CALL) {
            llamadaViewModel.marcarAceptarDesdeNotificacion()
        }
    }
}