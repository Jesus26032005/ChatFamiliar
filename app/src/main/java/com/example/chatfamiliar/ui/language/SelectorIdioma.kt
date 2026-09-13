package com.example.chatfamiliar.ui.language

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.R
import com.example.chatfamiliar.data.language.IdiomaRepository


@Composable
fun SelectorIdioma(
    idiomaSeleccionado: String?,
    idiomaEfectivo: String,

    traducir: TraducirTexto,

    alSeleccionarIdioma: (String?) -> Unit,
    alCerrar: () -> Unit
) {
    val textos = recordarTextosApp(idiomaEfectivo = idiomaEfectivo, traducir = traducir)

    AlertDialog(onDismissRequest = alCerrar,
        icon = { Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(imageVector = Icons.Filled.Language,
                    contentDescription = null,
                    modifier = Modifier.padding(14.dp),
                    tint = MaterialTheme.colorScheme.primary)
            }
        },
        title = {
            Text(text = textos.texto(R.string.language_select),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = textos.texto(R.string.language_dialog_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))


                OpcionIdioma(
                    codigo = "AUTO",
                    titulo = textos.texto(R.string.language_system),
                    descripcion = textos.texto(R.string.language_system_option_description),
                    textoActual = textos.texto(R.string.language_current),
                    seleccionado = idiomaSeleccionado == null,
                    onClick = {alSeleccionarIdioma(null)
                        alCerrar() })
                OpcionIdioma(
                    codigo = "ES",
                    titulo = textos.texto(R.string.language_spanish),
                    descripcion = textos.texto(R.string.language_spanish_description),
                    textoActual = textos.texto(R.string.language_current),
                    seleccionado = idiomaSeleccionado == IdiomaRepository.ESPANOL,
                    onClick = { alSeleccionarIdioma(IdiomaRepository.ESPANOL)
                        alCerrar() })
                OpcionIdioma(
                    codigo = "EN",
                    titulo = textos.texto(R.string.language_english),
                    descripcion = textos.texto(R.string.language_english_description),
                    textoActual = textos.texto(R.string.language_current),
                    seleccionado = idiomaSeleccionado == IdiomaRepository.INGLES,
                    onClick = { alSeleccionarIdioma(IdiomaRepository.INGLES)
                        alCerrar() })
                OpcionIdioma(
                    codigo = "IT",
                    titulo = textos.texto(R.string.language_italian),
                    descripcion = textos.texto(R.string.language_italian_description),
                    textoActual = textos.texto(R.string.language_current),
                    seleccionado = idiomaSeleccionado == IdiomaRepository.ITALIANO,
                    onClick = { alSeleccionarIdioma(IdiomaRepository.ITALIANO)
                        alCerrar() })
                OpcionIdioma(
                    codigo = "FR",
                    titulo = textos.texto(R.string.language_french),
                    descripcion = textos.texto(R.string.language_french_description),
                    textoActual = textos.texto(R.string.language_current),
                    seleccionado = idiomaSeleccionado == IdiomaRepository.FRANCES,
                    onClick = { alSeleccionarIdioma(IdiomaRepository.FRANCES)
                        alCerrar() })
            }
        },
        confirmButton = {
            TextButton(
                onClick = alCerrar) {
                Text(text = textos.texto(R.string.language_cancel))
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}


@Composable
private fun OpcionIdioma(
    codigo: String,
    titulo: String,
    descripcion: String,
    textoActual: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Surface(modifier = Modifier
            .fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (seleccionado) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
        border =
            BorderStroke(
                width = if (seleccionado) { 2.dp } else { 1.dp },
                color = if (seleccionado) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant }),
        tonalElevation =
            if (seleccionado) { 3.dp
            } else { 0.dp }
    ) {
        Row(modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = if (seleccionado) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = codigo,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (seleccionado) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            }
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    if (seleccionado) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(text = textoActual,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            RadioButton(selected = seleccionado, onClick = onClick)
        }
    }
}


@Preview(
    showBackground = true
)
@Composable
private fun SelectorIdiomaPreview() {
    MaterialTheme {
        Column(
            modifier =
                Modifier.padding(20.dp),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            OpcionIdioma(
                codigo = "AUTO",
                titulo = "Sistema",
                descripcion =
                    "Seguir automáticamente el idioma del dispositivo",
                textoActual = "Actual",
                seleccionado = false,
                onClick = {}
            )
            OpcionIdioma(
                codigo = "ES",
                titulo = "Español",
                descripcion =
                    "Mostrar la aplicación en español",
                textoActual = "Actual",
                seleccionado = true,
                onClick = {}
            )
            OpcionIdioma(
                codigo = "EN",
                titulo = "Inglés",
                descripcion =
                    "Mostrar la aplicación en inglés",
                textoActual = "Actual",
                seleccionado = false,
                onClick = {}
            )
            OpcionIdioma(
                codigo = "IT",
                titulo = "Italiano",
                descripcion =
                    "Traducir automáticamente la aplicación al italiano",
                textoActual = "Actual",
                seleccionado = false,
                onClick = {}
            )
        }
    }
}