package com.example.chatfamiliar.ui.home

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatfamiliar.R
import com.example.chatfamiliar.data.language.IdiomaRepository
import com.example.chatfamiliar.ui.language.IdiomaViewModel
import com.example.chatfamiliar.ui.language.SelectorIdioma
import com.example.chatfamiliar.ui.language.TraducirTexto
import com.example.chatfamiliar.ui.language.recordarIdiomaEfectivo
import com.example.chatfamiliar.ui.language.recordarTextosApp


@Composable
fun PantallaHome(
    idiomaViewModel: IdiomaViewModel,
    viewModel: HomeViewModel = viewModel(),
    alCerrarSesion: () -> Unit
){
    val idiomaEfectivo = recordarIdiomaEfectivo(
        idiomaViewModel.idiomaSeleccionado)
    ContenidoHome(
        idiomaSeleccionado = idiomaViewModel.idiomaSeleccionado,
        idiomaEfectivo = idiomaEfectivo,
        traducir = idiomaViewModel::traducir,
        alSeleccionarIdioma = idiomaViewModel::seleccionarIdioma,
        alCerrarSesion = {
            viewModel.cerrarSesion(alCerrarSesion = alCerrarSesion) }
    )
}


@Composable
fun ContenidoHome(
    idiomaSeleccionado: String?,
    idiomaEfectivo: String,
    traducir: TraducirTexto,
    alSeleccionarIdioma: (String?) -> Unit,
    alCerrarSesion: () -> Unit
) {
    var mostrarSelectorIdioma by rememberSaveable {
        mutableStateOf(false)
    }

    val textos =
        recordarTextosApp(idiomaEfectivo = idiomaEfectivo,
            traducir = traducir)

    val codigoIdioma =
        when (idiomaSeleccionado) {
            IdiomaRepository.ESPANOL -> { "ES" }
            IdiomaRepository.INGLES -> { "EN" }
            IdiomaRepository.ITALIANO -> { "IT" }
            IdiomaRepository.FRANCES -> {"FR"}
            else -> { "AUTO" }
        }

    Box(modifier = Modifier.fillMaxSize()
        .safeDrawingPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //Encabezado
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.Forum,
                            contentDescription = null,
                            modifier = Modifier.size(27.dp),
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = textos.texto(R.string.home_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                    Text(text = textos.texto(R.string.login_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            // Bienvenida
            Card(modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme
                            .primaryContainer)
            ) {
                Column(modifier = Modifier.padding(22.dp)
                ) {
                    Text(text = textos.texto(R.string.home_welcome),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = textos.texto(R.string.home_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Configuración de idioma.
            Card(modifier = Modifier.fillMaxWidth()
                    .clickable { mostrarSelectorIdioma = true },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor =
                    MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icono de idioma.
                    Surface(modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme
                            .secondaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Filled.Language,
                                contentDescription = textos.texto(
                                    R.string.language_select),
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme
                                    .onSecondaryContainer)
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    // Nombre y descripción
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = textos.texto(
                            R.string.language_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = textos.texto(
                            R.string.language_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(text = codigoIdioma,
                            modifier = Modifier.padding(
                                horizontal = 11.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            // Cerrar sesión
            OutlinedButton(onClick = alCerrarSesion,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Filled.Logout,
                    contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = textos.texto(R.string.home_logout),
                    fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }


    if (mostrarSelectorIdioma) {
        SelectorIdioma(idiomaSeleccionado = idiomaSeleccionado,
            idiomaEfectivo = idiomaEfectivo,
            traducir = traducir,
            alSeleccionarIdioma = alSeleccionarIdioma,
            alCerrar = { mostrarSelectorIdioma = false }
        )
    }
}


@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun PantallaHomePreview() {
    MaterialTheme {
        ContenidoHome(
            idiomaSeleccionado = IdiomaRepository.ESPANOL,
            idiomaEfectivo = IdiomaRepository.ESPANOL,
            traducir = { texto, _, alCompletar ->
                alCompletar(Result.success(texto)) },
            alSeleccionarIdioma = {},
            alCerrarSesion = {}
        )
    }
}