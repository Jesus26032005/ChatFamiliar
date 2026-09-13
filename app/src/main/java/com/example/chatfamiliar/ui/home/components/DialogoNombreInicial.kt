package com.example.chatfamiliar.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun DialogoNombreInicial(
    visible: Boolean,
    nombre: String,
    titulo: String,
    descripcion: String,
    etiquetaNombre: String,
    textoGuardar: String,
    mensajeError: String?,
    guardando: Boolean,
    alCambiarNombre: (String) -> Unit,
    alGuardarNombre: () -> Unit
) {
    if (!visible) { return }
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = titulo, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(18.dp))
                OutlinedTextField(value = nombre,
                    onValueChange = alCambiarNombre,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = etiquetaNombre) },
                    singleLine = true,
                    enabled = !guardando,
                    shape = RoundedCornerShape(16.dp))
                if (mensajeError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = mensajeError, style =
                        MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = alGuardarNombre, enabled = !guardando
            ) {
                if (guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = textoGuardar) } },
        shape = RoundedCornerShape(28.dp)
    )
}