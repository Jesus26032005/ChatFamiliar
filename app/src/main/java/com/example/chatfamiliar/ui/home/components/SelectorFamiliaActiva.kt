package com.example.chatfamiliar.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.model.Familia
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun SelectorFamiliaActiva(
    familias: List<Familia>,
    familiaActiva: Familia,
    titulo: String,
    textoRol: String?,
    cargando: Boolean,
    alSeleccionarFamilia: (Familia) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxWidth()
    ) {
        Card(modifier = Modifier.fillMaxWidth()
            .clickable(
                enabled = familias.size > 1 && !cargando) {
                    expandido = true },
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme
                    .surfaceContainer)
        ) {
            Row(modifier = Modifier.fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme
                            .primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.Home,
                            contentDescription = null,
                            modifier = Modifier.size(25.dp),
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)
                ) {
                    Text(text = titulo,
                        style = MaterialTheme.typography
                                .labelMedium,
                        color = MaterialTheme.colorScheme
                                .onSurfaceVariant)
                    Text(text = familiaActiva.nombre,
                        style = MaterialTheme.typography
                                .titleMedium,
                        fontWeight = FontWeight.Bold)

                    if (textoRol != null && !cargando) {
                        Text(text = textoRol,
                            style = MaterialTheme.typography
                                    .bodySmall,
                            color = MaterialTheme.colorScheme
                                    .primary,
                            fontWeight = FontWeight.SemiBold)
                    }
                }

                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp)
                } else if (familias.size > 1) {
                    Icon(imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null)
                }
            }
        }

        DropdownMenu(expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            familias
                .forEach { familia ->
                    DropdownMenuItem(
                        text = {
                            Text(text = familia.nombre,
                                fontWeight =
                                if (familia.id == familiaActiva.id
                                ) { FontWeight.Bold
                                } else {
                                    FontWeight.Normal })
                    },
                    onClick = {
                        expandido = false
                        alSeleccionarFamilia(familia) }
                    )
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "Selector familia - Administrador"
)
@Composable
private fun SelectorFamiliaActivaPreview() {
    val familiaMartinez =
        Familia(id = "familia_1",
            nombre = "Familia Martínez",
            codigoInvitacion = "FAM-A1B2C3",
            creadoPor = "uid_zaddkiel"
        )

    val familiaLopez =
        Familia(id = "familia_2",
            nombre = "Familia López",
            codigoInvitacion = "FAM-D4E5F6",
            creadoPor = "uid_maria"
        )

    MaterialTheme {

        SelectorFamiliaActiva(
            familias = listOf(familiaMartinez,
                familiaLopez
            ),
            familiaActiva = familiaMartinez,
            titulo = "Familia activa",
            textoRol = "Administrador",
            cargando = false,
            alSeleccionarFamilia = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(
    showBackground = true,
    name = "Selector familia - Miembro"
)
@Composable
private fun SelectorFamiliaMiembroPreview() {

    val familia =
        Familia(
            id = "familia_1",
            nombre = "Familia Martínez",
            codigoInvitacion = "FAM-A1B2C3",
            creadoPor = "uid_admin"
        )


    MaterialTheme {

        SelectorFamiliaActiva(
            familias =
                listOf(familia),
            familiaActiva =
                familia,
            titulo =
                "Familia activa",
            textoRol =
                "Miembro",
            cargando =
                false,
            alSeleccionarFamilia =
                {},
            modifier =
                Modifier.padding(16.dp)
        )
    }
}