package com.example.chatfamiliar.ui.family.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import com.example.chatfamiliar.ui.comun.BotonConfirmarDestructivo

@Composable
fun DialogoQuitarAdministrador(
    visible: Boolean, nombreMiembro: String, titulo: String,
    descripcion: String, textoConfirmar: String, textoCancelar: String,
    mensajeError: String?, cargando: Boolean, alConfirmar: () -> Unit,
    alCancelar: () -> Unit) {
    if (!visible) { return }
    DialogoConfirmacionMiembro(
        nombreMiembro = nombreMiembro, titulo = titulo,
        descripcion = descripcion, textoConfirmar = textoConfirmar,
        textoCancelar = textoCancelar, mensajeError = mensajeError,
        cargando = cargando, alConfirmar = alConfirmar, alCancelar = alCancelar) }
@Composable
fun DialogoExpulsarMiembro(
    visible: Boolean, nombreMiembro: String, titulo: String,
    descripcion: String, textoConfirmar: String, textoCancelar: String,
    mensajeError: String?, cargando: Boolean, alConfirmar: () -> Unit,
    alCancelar: () -> Unit) {
    if (!visible) { return }
    DialogoConfirmacionMiembro(
        nombreMiembro = nombreMiembro, titulo = titulo,
        descripcion = descripcion, textoConfirmar = textoConfirmar,
        textoCancelar = textoCancelar, mensajeError = mensajeError,
        cargando = cargando, alConfirmar = alConfirmar, alCancelar = alCancelar,
        destructivo = true) }
@Composable
private fun DialogoConfirmacionMiembro(
    nombreMiembro: String, titulo: String, descripcion: String,
    textoConfirmar: String, textoCancelar: String, mensajeError: String?,
    cargando: Boolean, alConfirmar: () -> Unit, alCancelar: () -> Unit,
    destructivo: Boolean = false) {
    AlertDialog(onDismissRequest = { if (!cargando) { alCancelar() } },
        title = { Text(text = titulo, fontWeight = FontWeight.Bold) },
        text = { Column { Text(text = descripcion,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = nombreMiembro, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            if (mensajeError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = mensajeError, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error) } } },
        confirmButton = {
            val contenido: @Composable RowScope.() -> Unit = {
                if (cargando) { CircularProgressIndicator(
                    modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp)) }
                Text(text = textoConfirmar) }
            // Expulsar usa rojo; quitar administrador conserva el color principal
            if (destructivo) {
                BotonConfirmarDestructivo(onClick = alConfirmar,
                    enabled = !cargando, content = contenido)
            } else {
                Button(onClick = alConfirmar, enabled = !cargando,
                    content = contenido)
            } },
        dismissButton = { OutlinedButton(onClick = alCancelar, enabled = !cargando) {
            Text(text = textoCancelar) } },
        shape = RoundedCornerShape(28.dp)) }