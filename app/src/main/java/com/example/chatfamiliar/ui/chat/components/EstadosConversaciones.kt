package com.example.chatfamiliar.ui.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EstadoCargaConversaciones(titulo: String, descripcion: String,
    modifier: Modifier = Modifier) {
    ContenidoEstadoConversaciones(titulo = titulo, descripcion = descripcion,
        cargando = true, modifier = modifier)
}

@Composable
fun EstadoVacioConversaciones(titulo: String, descripcion: String,
    modifier: Modifier = Modifier, textoAccion: String? = null,
    alAccion: (() -> Unit)? = null){
    ContenidoEstadoConversaciones(titulo = titulo, descripcion = descripcion,
        icono = Icons.Filled.Forum, textoAccion = textoAccion,
        alAccion = alAccion, modifier = modifier)
}

@Composable
fun EstadoErrorConversaciones(titulo: String, mensaje: String,
    textoReintentar: String, alReintentar: () -> Unit,
    modifier: Modifier = Modifier) {
    ContenidoEstadoConversaciones(titulo = titulo,
        descripcion = mensaje, icono = Icons.Filled.ErrorOutline,
        esError = true, textoAccion = textoReintentar,
        alAccion = alReintentar, modifier = modifier)
}

@Composable
private fun ContenidoEstadoConversaciones(
    titulo: String, descripcion: String,
    modifier: Modifier = Modifier, icono: ImageVector? = null,
    cargando: Boolean = false, esError: Boolean = false,
    textoAccion: String? = null, alAccion: (() -> Unit)? = null) {
    Column(modifier = modifier.fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (cargando) {
            CircularProgressIndicator(modifier = Modifier.padding(vertical = 12.dp)
                    .size(32.dp), strokeWidth = 3.dp)
        } else if (icono != null) {
            Surface(modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(20.dp),
                color = if (esError) { MaterialTheme.colorScheme.errorContainer
                } else { MaterialTheme.colorScheme.primaryContainer },
                contentColor = if (esError) { MaterialTheme.colorScheme.onErrorContainer
                } else { MaterialTheme.colorScheme.onPrimaryContainer }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icono, contentDescription = null,
                        modifier = Modifier.size(30.dp)) } }
        }
        Text(text = titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center)
        Text(text = descripcion,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
        if (!textoAccion.isNullOrBlank() && alAccion != null) {
            Button(onClick = alAccion, shape = RoundedCornerShape(14.dp)) {
                Text( text = textoAccion, textAlign = TextAlign.Center)
            }
        }
    }
}