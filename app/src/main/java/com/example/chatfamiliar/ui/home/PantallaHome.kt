package com.example.chatfamiliar.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun PantallaHome(viewModel: HomeViewModel = viewModel(), alCerrarSesion: () -> Unit) {
    ContenidoHome(
        alCerrarSesion = {
            viewModel.cerrarSesion()
            alCerrarSesion()
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContenidoHome(
    alCerrarSesion: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "FamiliaChat", fontWeight = FontWeight.Bold) },
                actions = { IconButton(onClick = alCerrarSesion) { Icon(imageVector = Icons.Filled.Logout, contentDescription = "Cerrar sesión") } }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(imageVector = Icons.Filled.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)

                Text(text = "¡Bienvenido a FamiliaChat!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center, modifier = Modifier.padding(top = 16.dp))

                Text(text = "Tu cuenta está lista. Más adelante aquí aparecerán tus conversaciones familiares.", style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true
)
@Composable
private fun PantallaHomePreview() {
    MaterialTheme {
        ContenidoHome(
            alCerrarSesion = {}
        )
    }
}