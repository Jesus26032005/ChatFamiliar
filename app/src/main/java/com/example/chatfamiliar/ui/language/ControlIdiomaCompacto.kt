package com.example.chatfamiliar.ui.language

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
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
import com.example.chatfamiliar.data.language.IdiomaRepository


@Composable
fun ControlIdiomaCompacto(
    idiomaSeleccionado: String?,
    idiomaEfectivo: String,
    traducir: TraducirTexto,
    alSeleccionarIdioma: (String?) -> Unit
) {
    var mostrarSelector by rememberSaveable { mutableStateOf(false)
    }
    val textos = recordarTextosApp(idiomaEfectivo = idiomaEfectivo,
        traducir = traducir)
    val codigoVisible =
        when (idiomaSeleccionado) {
            IdiomaRepository.ESPANOL -> { "ES" }
            IdiomaRepository.INGLES -> { "EN" }
            IdiomaRepository.ITALIANO -> { "IT" }
            IdiomaRepository.FRANCES -> {"FR"}
            else -> { "AUTO" }
        }
    Surface(
        modifier = Modifier.clickable { mostrarSelector = true },
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Language,
                contentDescription = textos.texto(R.string.language_select),
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(text = codigoVisible,
                style = MaterialTheme.typography.labelLarge,
                fontWeight =
                    FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }


    if (mostrarSelector) {
        SelectorIdioma(
            idiomaSeleccionado = idiomaSeleccionado,
            idiomaEfectivo = idiomaEfectivo,
            traducir = traducir,
            alSeleccionarIdioma = alSeleccionarIdioma,
            alCerrar = { mostrarSelector = false
            }
        )
    }
}