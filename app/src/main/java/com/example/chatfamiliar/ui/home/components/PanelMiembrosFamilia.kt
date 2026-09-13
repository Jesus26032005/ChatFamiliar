package com.example.chatfamiliar.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.model.MiembroFamiliaDetalle


@Composable
fun PanelMiembrosFamilia(
    miembros: List<MiembroFamiliaDetalle>,
    uidUsuarioActual: String,
    uidAdministradorPrincipal: String,
    esAdministradorActual: Boolean,
    titulo: String,
    textoRolAdministrador: String,
    textoRolMiembro: String,
    textoAdministradorPrincipal: String,
    textoTu: String,
    textoHacerAdministrador: String,
    textoQuitarAdministrador: String,
    textoExpulsar: String,
    textoVacio: String,
    cargando: Boolean,
    actualizandoRolUid: String?,
    expulsandoMiembroUid: String?,
    mensajeError: String?,
    alHacerAdministrador: (MiembroFamiliaDetalle) -> Unit,
    alQuitarAdministrador: (MiembroFamiliaDetalle) -> Unit,
    alExpulsar: (MiembroFamiliaDetalle) -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme
                .colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(18.dp)
        ) {
            Text(text = "$titulo (${miembros.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(14.dp))
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(26.dp)
                        .align(Alignment.CenterHorizontally),
                    strokeWidth = 2.dp
                )
            } else if (miembros.isEmpty()) {
                Text(text = textoVacio,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                miembros.forEachIndexed { indice, miembro ->
                    val esPrincipal = miembro.uid == uidAdministradorPrincipal
                    val esActual = miembro.uid == uidUsuarioActual

                    ItemMiembroFamilia(
                        nombre = miembro.nombre,
                        correo = miembro.correo,
                        textoRol =
                            if (miembro.esAdministrador) {
                                textoRolAdministrador
                            } else { textoRolMiembro },
                        textoAdministradorPrincipal = textoAdministradorPrincipal,
                        textoTu = textoTu,
                        esAdministrador = miembro.esAdministrador,
                        esAdministradorPrincipal = esPrincipal,
                        esUsuarioActual = esActual,
                        puedeGestionar = esAdministradorActual,
                        textoHacerAdministrador = textoHacerAdministrador,
                        textoQuitarAdministrador = textoQuitarAdministrador,
                        textoExpulsar = textoExpulsar,
                        actualizandoRol = actualizandoRolUid == miembro.uid,
                        expulsando = expulsandoMiembroUid == miembro.uid,
                        alHacerAdministrador = { alHacerAdministrador(miembro) },
                        alQuitarAdministrador = { alQuitarAdministrador(miembro) },
                        alExpulsar = { alExpulsar(miembro)
                        }
                    )


                    if (indice != miembros.lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
            if (mensajeError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = mensajeError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}