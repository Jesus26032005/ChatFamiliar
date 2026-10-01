package com.example.chatfamiliar.ui.profile

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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.R
import com.example.chatfamiliar.model.Usuario
import com.example.chatfamiliar.ui.comun.AvatarIdentidad
import com.example.chatfamiliar.ui.language.ControlIdiomaCompacto
import com.example.chatfamiliar.ui.language.TraducirTexto
import com.example.chatfamiliar.ui.language.recordarTextosApp
import com.example.chatfamiliar.ui.theme.funcional

@Composable
fun PantallaPerfil(usuario: Usuario,
    idiomaSeleccionado: String?, idiomaEfectivo: String,
    traducir: TraducirTexto, alSeleccionarIdioma: (String?) -> Unit,
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = correoVisible,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        SeccionPerfil(titulo = textos.texto(R.string.profile_account_title)) {
            FilaPerfil(icono = Icons.Filled.Person,
                etiqueta = textos.texto(R.string.profile_name_label),
                valor = nombreVisible)
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

@Composable
private fun FilaPerfil(icono: ImageVector, etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        IconoSeccion(icono = icono)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            SelectionContainer {
                Text(text = valor,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface) } } }
}