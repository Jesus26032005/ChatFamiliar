package com.example.chatfamiliar.ui.family

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.model.Familia
import com.example.chatfamiliar.R
import com.example.chatfamiliar.ui.chat.components.EstadoCargaConversaciones
import com.example.chatfamiliar.ui.chat.components.EstadoErrorConversaciones
import com.example.chatfamiliar.ui.chat.components.EstadoVacioConversaciones
import com.example.chatfamiliar.ui.comun.AvatarIdentidad
import com.example.chatfamiliar.ui.language.ControlIdiomaCompacto
import com.example.chatfamiliar.ui.language.recordarTextosApp
import com.example.chatfamiliar.ui.language.TraducirTexto
import com.example.chatfamiliar.ui.theme.funcional

@Composable
fun PantallaListadoFamilias(familiasConfirmadas: List<Familia>?, cargando: Boolean,
    @StringRes errorRecurso: Int?, idiomaSeleccionado: String?, idiomaEfectivo: String,
    traducir: TraducirTexto, alSeleccionarIdioma: (String?) -> Unit,
    alGestionarFamilia: (Familia) -> Unit, alCrearOUnirse: () -> Unit,
    alRecargar: () -> Unit, modifier: Modifier = Modifier) {
    val textos = recordarTextosApp(idiomaEfectivo = idiomaEfectivo, traducir = traducir)
    val familias = familiasConfirmadas.orEmpty()
    val esperandoPrimeraConsulta = familiasConfirmadas == null && errorRecurso == null
    Column(modifier = modifier.fillMaxSize()
        .safeDrawingPadding()
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = textos.texto(R.string.families_section_title),
                modifier = Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = MaterialTheme.funcional.encabezado)
            ControlIdiomaCompacto(idiomaSeleccionado = idiomaSeleccionado,
                idiomaEfectivo = idiomaEfectivo, traducir = traducir,
                alSeleccionarIdioma = alSeleccionarIdioma) }
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp,
                end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "descripcion") {
                Text(text = textos.texto(R.string.families_section_description),
                    modifier = Modifier.padding(bottom = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item(key = "acciones") {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = alCrearOUnirse,
                        modifier = Modifier.weight(1f).height(52.dp).padding(bottom = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.funcional.accionPrincipal,
                            contentColor = MaterialTheme.funcional.sobreAccionPrincipal)) {
                        Icon(imageVector = Icons.Filled.GroupAdd,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = textos.texto(
                            R.string.families_section_create_join),
                            maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    FilledTonalIconButton(onClick = alRecargar,
                        enabled = !cargando && !esperandoPrimeraConsulta,
                        modifier = Modifier.size(52.dp).padding(bottom = 6.dp),
                        shape = RoundedCornerShape(16.dp)) {
                        Icon(imageVector = Icons.Filled.Refresh,
                            contentDescription = textos.texto(
                                R.string.families_section_refresh)) } } }
            if (errorRecurso != null) {
                item(key = "error") {
                    EstadoErrorConversaciones(
                        titulo = textos.texto(R.string.families_section_error),
                        mensaje = textos.texto(errorRecurso),
                        textoReintentar = textos.texto(
                            R.string.chat_item_retry),
                        alReintentar = alRecargar) } }
            items(items = familias,
                key = { familia -> "familia:${familia.id}" }
            ) { familia ->
                TarjetaFamiliaListado(
                    semilla = familia.id,
                    nombre = familia.nombre.ifBlank {
                        textos.texto(R.string.chat_item_unnamed_family) },
                    textoGestionar = textos.texto(R.string.families_section_manage),
                    alGestionar = { alGestionarFamilia(familia) }) }
            if (cargando || esperandoPrimeraConsulta) {
                item(key = "carga") {
                    EstadoCargaConversaciones(
                        titulo = textos.texto(R.string.families_section_loading),
                        descripcion = textos.texto(
                            R.string.families_section_loading_description)) }
            } else if (familiasConfirmadas != null &&
                familias.isEmpty() &&
                errorRecurso == null) {
                item(key = "sin_familias") {
                    EstadoVacioConversaciones(
                        titulo = textos.texto(
                            R.string.home_no_family_title),
                        descripcion = textos.texto(
                            R.string.families_section_empty_description)) } } } }
}

@Composable
private fun TarjetaFamiliaListado(semilla: String, nombre: String,
                                  textoGestionar: String, alGestionar: () -> Unit) {
    val funcional = MaterialTheme.funcional
    val tono = funcional.tonoPara(semilla)
    Card(
        onClick = alGestionar,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = funcional.tarjeta),
        border = BorderStroke(width = 1.dp, color = tono.texto)
    ) {
        Row(modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            AvatarIdentidad(semilla = semilla, icono = Icons.Filled.Groups,
                esGrupo = true, tamano = 48.dp)
            Column(modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = textoGestionar,
                    style = MaterialTheme.typography.labelLarge,
                    color = funcional.encabezado)
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}