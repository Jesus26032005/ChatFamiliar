package com.example.chatfamiliar.ui.family.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.ui.comun.AvatarIdentidad
import com.example.chatfamiliar.ui.comun.BotonDestructivo
import com.example.chatfamiliar.ui.theme.funcional

@Composable
fun ItemMiembroFamilia(
    uid: String,
    nombre: String, correo: String,
    textoRol: String, textoAdministradorPrincipal: String,
    textoTu: String, esAdministrador: Boolean,
    esAdministradorPrincipal: Boolean, esUsuarioActual: Boolean,
    puedeGestionar: Boolean, textoHacerAdministrador: String,
    textoQuitarAdministrador: String, textoExpulsar: String,
    actualizandoRol: Boolean, expulsando: Boolean,
    alHacerAdministrador: () -> Unit,
    alQuitarAdministrador: () -> Unit,
    alExpulsar: () -> Unit, modifier: Modifier = Modifier) {
    val funcional = MaterialTheme.funcional
    val mostrarAcciones = puedeGestionar && !esUsuarioActual && !esAdministradorPrincipal
    val nombreVisible = nombre.ifBlank { correo }

    Surface(modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = funcional.tarjetaInterior
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarIdentidad(semilla = uid, nombre = nombreVisible,
                    tamano = 44.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = nombreVisible,
                            modifier = Modifier.weight(1f, fill = false),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (esUsuarioActual) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "- $textoTu",
                                style = MaterialTheme.typography.labelSmall,
                                color = funcional.encabezado) } }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = correo, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis) } }
            Spacer(modifier = Modifier.height(10.dp))
            EtiquetaRol(texto = if (esAdministradorPrincipal) {
                    textoAdministradorPrincipal } else { textoRol },
                esAdministrador = esAdministrador)
            if (mostrarAcciones) {
                Spacer(modifier = Modifier.height(12.dp))
                if (!esAdministrador) {
                    OutlinedButton(onClick = alHacerAdministrador,
                        enabled = !actualizandoRol && !expulsando,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)) {
                        if (actualizandoRol) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp)) }
                        Text(text = textoHacerAdministrador) }
                } else {
                    OutlinedButton(onClick = alQuitarAdministrador,
                        enabled = !actualizandoRol && !expulsando,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)) {
                        if (actualizandoRol) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp)) }
                        Text(text = textoQuitarAdministrador) } }
                Spacer(modifier = Modifier.height(8.dp))
                BotonDestructivo(onClick = alExpulsar,
                    enabled = !actualizandoRol && !expulsando,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)) {
                    if (expulsando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp) } else {
                        Icon(imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = null) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = textoExpulsar)
                }
            }
        }
    }
}


@Composable
fun EtiquetaRol(texto: String, esAdministrador: Boolean,
    modifier: Modifier = Modifier) {

    val colores = MaterialTheme.colorScheme
    val funcional = MaterialTheme.funcional
    Surface(modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (esAdministrador) colores.primaryContainer
        else funcional.etiquetaNeutra,
        contentColor = if (esAdministrador) colores.onPrimaryContainer
        else funcional.sobreEtiquetaNeutra) {
        Row(modifier = Modifier.padding(start = 8.dp, end = 10.dp,
            top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(imageVector = if (esAdministrador) {
                Icons.Filled.AdminPanelSettings } else { Icons.Filled.Person },
                contentDescription = null,
                modifier = Modifier.size(14.dp))
            Text(text = texto,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold) }
    }
}