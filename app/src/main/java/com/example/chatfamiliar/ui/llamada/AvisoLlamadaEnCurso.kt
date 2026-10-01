package com.example.chatfamiliar.ui.llamada

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.R
import com.example.chatfamiliar.ui.language.TextosApp
import com.example.chatfamiliar.ui.theme.funcional

@Composable
fun AvisoLlamadaEnCurso(
    textos: TextosApp,
    conectando: Boolean,
    alUnirse: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pidiendoPermisos by rememberSaveable { mutableStateOf(false) }
    val funcional = MaterialTheme.funcional

    Surface(modifier = modifier.fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = funcional.filtroSeleccionado,
        contentColor = funcional.sobreFiltroSeleccionado) {
        Row(modifier = Modifier.padding(start = 12.dp, end = 8.dp,
            top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            Surface(modifier = Modifier.size(36.dp), shape = CircleShape,
                color = funcional.accionPrincipal,
                contentColor = funcional.sobreAccionPrincipal) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Filled.Videocam, contentDescription = null,
                        modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = textos.texto(R.string.llamada_en_curso_familia),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { pidiendoPermisos = true },
                enabled = !conectando,
                colors = ButtonDefaults.buttonColors(
                    containerColor = funcional.accionPrincipal,
                    contentColor = funcional.sobreAccionPrincipal)) {
                if (conectando) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp, color = funcional.sobreAccionPrincipal)
                } else {
                    Text(text = textos.texto(R.string.llamada_unirse))
                }
            }
        }
    }

    if (pidiendoPermisos) {
        SolicitudPermisosLlamada(textos = textos,
            alPermisosConcedidos = {
                pidiendoPermisos = false
                alUnirse()
            },
            alCancelar = { pidiendoPermisos = false })
    }
}