package com.example.chatfamiliar.ui.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp


@Composable
fun ItemMiembroFamilia(
    nombre: String,
    correo: String,
    textoRol: String,
    textoAdministradorPrincipal: String,
    textoTu: String,
    esAdministrador: Boolean,
    esAdministradorPrincipal: Boolean,
    esUsuarioActual: Boolean,
    puedeGestionar: Boolean,
    textoHacerAdministrador: String,
    textoQuitarAdministrador: String,
    textoExpulsar: String,
    actualizandoRol: Boolean,
    expulsando: Boolean,
    alHacerAdministrador: () -> Unit,
    alQuitarAdministrador: () -> Unit,
    alExpulsar: () -> Unit,

    modifier: Modifier = Modifier
) {

    val mostrarAcciones = puedeGestionar && !esUsuarioActual && !esAdministradorPrincipal

    Surface(modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = if (esAdministrador) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                ) {
                    Box(contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = if (esAdministrador) {
                            Icons.Filled.AdminPanelSettings
                        } else { Icons.Filled.Person },
                            contentDescription = null,
                            modifier = Modifier.size(23.dp),
                            tint = if (esAdministrador) {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                }
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = nombre.ifBlank { correo },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold)
                        if (esUsuarioActual) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "- $textoTu",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = correo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Surface(shape = RoundedCornerShape(50),
                color = if (esAdministrador) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
            ) {
                Text(text = if (esAdministradorPrincipal) {
                    textoAdministradorPrincipal
                } else {textoRol },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (mostrarAcciones) {
                Spacer(modifier = Modifier.height(12.dp))
                if (!esAdministrador) {
                    OutlinedButton(
                        onClick = alHacerAdministrador,
                        enabled = !actualizandoRol && !expulsando,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (actualizandoRol) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(text = textoHacerAdministrador)
                        }
                    } else {
                        OutlinedButton(onClick = alQuitarAdministrador,
                            enabled = !actualizandoRol && !expulsando,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (actualizandoRol) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(text = textoQuitarAdministrador)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = alExpulsar,
                        enabled = !actualizandoRol && !expulsando,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (expulsando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = textoExpulsar,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }