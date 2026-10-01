package com.example.chatfamiliar.ui.family.components

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.ui.comun.BotonConfirmarDestructivo

@Composable
fun DialogoCrearFamilia(
    visible: Boolean, nombre: String,
    titulo: String, descripcion: String,
    etiqueta: String, textoConfirmar: String,
    textoCancelar: String, mensajeError: String?,
    cargando: Boolean, alCambiarNombre: (String) -> Unit,
    alConfirmar: () -> Unit, alCancelar: () -> Unit){
    if (!visible) { return }
    DialogoEntradaFamilia(
        titulo = titulo, descripcion = descripcion,
        valor = nombre, etiqueta = etiqueta,
        textoConfirmar = textoConfirmar, textoCancelar = textoCancelar,
        mensajeError = mensajeError, cargando = cargando,
        alCambiarValor = alCambiarNombre, alConfirmar = alConfirmar,
        alCancelar = alCancelar) }
@Composable
fun DialogoUnirseFamilia(
    visible: Boolean, codigo: String,
    titulo: String, descripcion: String,
    etiqueta: String, textoConfirmar: String,
    textoCancelar: String, mensajeError: String?,
    cargando: Boolean, alCambiarCodigo: (String) -> Unit,
    alConfirmar: () -> Unit, alCancelar: () -> Unit) {
    if (!visible) { return }
    DialogoEntradaFamilia(
        titulo = titulo, descripcion = descripcion,
        valor = codigo, etiqueta = etiqueta,
        textoConfirmar = textoConfirmar, textoCancelar = textoCancelar,
        mensajeError = mensajeError, cargando = cargando,
        alCambiarValor = alCambiarCodigo, alConfirmar = alConfirmar,
        alCancelar = alCancelar) }
@Composable
private fun DialogoEntradaFamilia(
    titulo: String,
    descripcion: String,
    valor: String,
    etiqueta: String,
    textoConfirmar: String,
    textoCancelar: String,
    mensajeError: String?,
    cargando: Boolean,
    alCambiarValor: (String) -> Unit,
    alConfirmar: () -> Unit,
    alCancelar: () -> Unit
) {
    AlertDialog(onDismissRequest = { if (!cargando) { alCancelar() } },
        title = { Text(text = titulo, fontWeight = FontWeight.Bold) },
        text = { Column { Text(text = descripcion)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = valor, onValueChange = alCambiarValor,
                modifier = Modifier.fillMaxWidth(), label = { Text(etiqueta) },
                enabled = !cargando, singleLine = true,
                shape = RoundedCornerShape(16.dp))
            if (mensajeError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = mensajeError, color = MaterialTheme.colorScheme.error) } } },
        confirmButton = { Button(onClick = alConfirmar, enabled = !cargando) {
            if (cargando) { CircularProgressIndicator(modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp)) }
            Text(textoConfirmar) } },
        dismissButton = { OutlinedButton(onClick = alCancelar, enabled = !cargando) {
            Text(textoCancelar) } }, shape = RoundedCornerShape(28.dp)) }

@Composable
fun DialogoEditarFamilia(
    visible: Boolean, nombre: String, titulo: String, descripcion: String,
    etiqueta: String, textoConfirmar: String, textoCancelar: String,
    mensajeError: String?, cargando: Boolean, alCambiarNombre: (String) -> Unit,
    alConfirmar: () -> Unit, alCancelar: () -> Unit) {
    if (!visible) { return }
    DialogoEntradaFamilia(titulo = titulo,
        descripcion = descripcion, valor = nombre,
        etiqueta = etiqueta, textoConfirmar = textoConfirmar,
        textoCancelar = textoCancelar, mensajeError = mensajeError,
        cargando = cargando, alCambiarValor = alCambiarNombre,
        alConfirmar = alConfirmar, alCancelar = alCancelar) }
@Composable
fun DialogoAbandonarFamilia(
    visible: Boolean, nombreFamilia: String, titulo: String, descripcion: String,
    textoConfirmar: String, textoCancelar: String, mensajeError: String?,
    cargando: Boolean, alConfirmar: () -> Unit, alCancelar: () -> Unit) {
    if (!visible) { return }
    AlertDialog(onDismissRequest = { if (!cargando) { alCancelar() } },
        title = { Text(text = titulo, fontWeight = FontWeight.Bold) },
        text = { Column { Text(text = descripcion)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = nombreFamilia, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            if (mensajeError != null) { Spacer(modifier = Modifier.height(12.dp))
                Text(text = mensajeError, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall) } } },
        confirmButton = { BotonConfirmarDestructivo(onClick = alConfirmar, enabled = !cargando) {
            if (cargando) { CircularProgressIndicator(modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp)) }
            Text(text = textoConfirmar) } },
        dismissButton = { OutlinedButton(onClick = alCancelar, enabled = !cargando) {
            Text(text = textoCancelar) } },
        shape = RoundedCornerShape(28.dp)) }
@Composable
fun DialogoEliminarFamilia(
    visible: Boolean, nombreFamilia: String, titulo: String,
    descripcion: String, textoConfirmar: String, textoCancelar: String,
    mensajeError: String?, cargando: Boolean, alConfirmar: () -> Unit,
    alCancelar: () -> Unit) {
    if (!visible) { return }
    AlertDialog(onDismissRequest = { if (!cargando) { alCancelar() } },
        title = { Text(text = titulo, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error) },
        text = { Column { Text(text = descripcion,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = nombreFamilia, fontWeight = FontWeight.Bold)
            if (mensajeError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = mensajeError, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall) } } },
        confirmButton = { BotonConfirmarDestructivo(onClick = alConfirmar,
            enabled = !cargando) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp)) }
            Text(text = textoConfirmar) } },
        dismissButton = { OutlinedButton(onClick = alCancelar, enabled = !cargando) {
            Text(text = textoCancelar) } }, shape = RoundedCornerShape(28.dp)
    )
}