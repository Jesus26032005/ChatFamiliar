package com.example.chatfamiliar.ui.llamada

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.R
import com.example.chatfamiliar.ui.language.TextosApp
import com.example.chatfamiliar.ui.theme.funcional

@Composable
fun BotonVideollamada(
    textos: TextosApp,
    habilitado: Boolean,
    iniciando: Boolean,
    alLlamar: () -> Unit,
    modifier: Modifier = Modifier,
    // Cuando no hay a quién llamar: el botón se ve atenuado y al tocarlo
    // se explica el motivo en lugar de llamar.
    atenuado: Boolean = false,
    alPulsarAtenuado: () -> Unit = {}
) {
    var pidiendoPermisos by rememberSaveable { mutableStateOf(false) }

    FilledTonalIconButton(
        onClick = {
            if (atenuado) { alPulsarAtenuado() } else { pidiendoPermisos = true }
        },
        enabled = habilitado && !iniciando,
        modifier = modifier.size(44.dp).alpha(if (atenuado) 0.45f else 1f),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.funcional.filtroSeleccionado,
            contentColor = MaterialTheme.funcional.sobreFiltroSeleccionado
        )
    ) {
        if (iniciando) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp)
        } else {
            Icon(imageVector = Icons.Filled.Videocam,
                contentDescription = textos.texto(R.string.llamada_boton_videollamada))
        }
    }

    if (pidiendoPermisos) {
        SolicitudPermisosLlamada(
            textos = textos,
            alPermisosConcedidos = {
                pidiendoPermisos = false
                alLlamar()
            },
            alCancelar = { pidiendoPermisos = false }
        )
    }
}