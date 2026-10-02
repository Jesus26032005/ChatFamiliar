package com.example.chatfamiliar.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.R
import com.example.chatfamiliar.model.Usuario
import com.example.chatfamiliar.ui.comun.AvatarIdentidad
import com.example.chatfamiliar.ui.language.ControlIdiomaCompacto
import com.example.chatfamiliar.ui.language.TextosApp
import com.example.chatfamiliar.ui.language.TraducirTexto
import com.example.chatfamiliar.ui.language.recordarTextosApp
import com.example.chatfamiliar.ui.theme.funcional

private const val LARGO_MAXIMO_NOMBRE = 40

@Composable
fun PantallaPerfil(usuario: Usuario,
                   idiomaSeleccionado: String?, idiomaEfectivo: String,
                   traducir: TraducirTexto, alSeleccionarIdioma: (String?) -> Unit,
    // Edición del nombre (el estado vive en HomeViewModel).
                   editandoNombre: Boolean, nombreEditado: String,
                   guardandoNombre: Boolean, mensajeErrorNombre: String?,
                   alEditarNombre: () -> Unit, alCambiarNombre: (String) -> Unit,
                   alGuardarNombre: () -> Unit, alCancelarEdicionNombre: () -> Unit,
                   alCerrarSesion: () -> Unit,
                   modifier: Modifier = Modifier) {
    val textos = recordarTextosApp(idiomaEfectivo = idiomaEfectivo, traducir = traducir)
    val nombreVisible = usuario.nombre.ifBlank {
        textos.texto(R.string.profile_name_missing) }
    val correoVisible = usuario.correo.ifBlank {
        textos.texto(R.string.profile_email_missing) }
    Column(
        modifier = modifier.fillMaxSize().safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = textos.texto(R.string.profile_title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.funcional.encabezado)
            ControlIdiomaCompacto(idiomaSeleccionado = idiomaSeleccionado,
                idiomaEfectivo = idiomaEfectivo,
                traducir = traducir,
                alSeleccionarIdioma = alSeleccionarIdioma) }
        Column(modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            AvatarIdentidad(semilla = usuario.uid,
                nombre = usuario.nombre,
                tamano = 88.dp,
                idioma = idiomaEfectivo)
            Spacer(modifier = Modifier.height(14.dp))
            Text(text = nombreVisible,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        // "Nombre" aquí es una acción (editar), no un dato repetido.
        SeccionPerfil(titulo = textos.texto(R.string.profile_account_title)) {
            FilaPerfil(icono = Icons.Filled.Person,
                etiqueta = textos.texto(R.string.profile_name_label),
                valor = nombreVisible,
                descripcionAccion = textos.texto(R.string.profile_edit_name),
                alPulsar = alEditarNombre)
            HorizontalDivider(modifier = Modifier.padding(start = 68.dp),
                color = MaterialTheme.colorScheme.outlineVariant)
            FilaPerfil(icono = Icons.Filled.Email,
                etiqueta = textos.texto(R.string.profile_email_label),
                valor = correoVisible) }
        SeccionPerfil(titulo = textos.texto(R.string.profile_language_title)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.Top) {
                IconoSeccion(icono = Icons.Filled.Language)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = textos.texto(R.string.profile_language_description),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        SeccionPerfil(titulo = textos.texto(R.string.profile_session_title)) {
            OutlinedButton(onClick = alCerrarSesion,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = textos.texto(R.string.home_logout),
                    fontWeight = FontWeight.SemiBold) } }
        Spacer(modifier = Modifier.height(8.dp)) }

    if (editandoNombre) {
        DialogoEditarNombre(textos = textos,
            nombre = nombreEditado,
            nombreActual = usuario.nombre.trim(),
            guardando = guardandoNombre,
            mensajeError = mensajeErrorNombre,
            alCambiarNombre = alCambiarNombre,
            alGuardar = alGuardarNombre,
            alCancelar = alCancelarEdicionNombre)
    }
}

/**
 * Diálogo para cambiar el nombre. Antes del campo muestra un aviso
 * de qué pasa al cambiarlo: la familia ve el nombre nuevo en chats y
 * videollamadas, pero los mensajes ya enviados conservan el anterior.
 */
@Composable
private fun DialogoEditarNombre(textos: TextosApp,
                                nombre: String, nombreActual: String,
                                guardando: Boolean, mensajeError: String?,
                                alCambiarNombre: (String) -> Unit,
                                alGuardar: () -> Unit, alCancelar: () -> Unit) {
    val nombreLimpio = nombre.trim()
    val puedeGuardar = !guardando && nombreLimpio.isNotEmpty() &&
            nombreLimpio != nombreActual

    AlertDialog(
        onDismissRequest = alCancelar,
        icon = { Icon(imageVector = Icons.Filled.Edit, contentDescription = null) },
        title = { Text(text = textos.texto(R.string.profile_edit_name),
            fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AvisoCambioNombre(texto = textos.texto(R.string.profile_edit_name_notice))
                OutlinedTextField(value = nombre,
                    onValueChange = { nuevo ->
                        if (nuevo.length <= LARGO_MAXIMO_NOMBRE) alCambiarNombre(nuevo) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = textos.texto(R.string.profile_name_label)) },
                    supportingText = {
                        Text(text = "${nombre.length}/$LARGO_MAXIMO_NOMBRE",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End) },
                    isError = mensajeError != null,
                    singleLine = true,
                    enabled = !guardando,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { if (puedeGuardar) alGuardar() }),
                    shape = RoundedCornerShape(16.dp))
                if (mensajeError != null) {
                    Text(text = mensajeError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = alGuardar, enabled = puedeGuardar) {
                if (guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp)) }
                Text(text = textos.texto(R.string.profile_edit_name_save)) } },
        dismissButton = {
            TextButton(onClick = alCancelar, enabled = !guardando) {
                Text(text = textos.texto(R.string.profile_edit_name_cancel)) } },
        shape = RoundedCornerShape(28.dp))
}

/** Cuadro de aviso con el color de contenedor secundario del tema. */
@Composable
private fun AvisoCambioNombre(texto: String) {
    Surface(modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top) {
            Icon(imageVector = Icons.Outlined.Info, contentDescription = null,
                modifier = Modifier.size(20.dp))
            Text(text = texto, modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall) } }
}

@Composable
private fun SeccionPerfil(titulo: String,
                          contenido: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = titulo,
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.funcional.tarjeta)) {
            contenido() } }
}

@Composable
private fun IconoSeccion(icono: ImageVector) {
    Surface(modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icono, contentDescription = null,
                modifier = Modifier.size(20.dp)) } }
}

/**
 * Fila de dato. Si recibe alPulsar, toda la fila se puede tocar y
 * muestra un lápiz a la derecha; si no, el valor se puede seleccionar
 * para copiarlo.
 */
@Composable
private fun FilaPerfil(icono: ImageVector, etiqueta: String, valor: String,
                       descripcionAccion: String? = null,
                       alPulsar: (() -> Unit)? = null) {
    val modificadorFila = if (alPulsar != null) {
        Modifier.clickable(onClickLabel = descripcionAccion, onClick = alPulsar)
    } else {
        Modifier
    }
    Row(modifier = Modifier.fillMaxWidth()
        .then(modificadorFila)
        .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        IconoSeccion(icono = icono)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (alPulsar != null) {
                Text(text = valor,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            } else {
                SelectionContainer {
                    Text(text = valor,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface) } } }
        if (alPulsar != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Filled.Edit,
                contentDescription = descripcionAccion,
                tint = MaterialTheme.funcional.accionPrincipal,
                modifier = Modifier.size(20.dp)) } }
}