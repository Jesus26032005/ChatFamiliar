package com.example.chatfamiliar.ui.auth.login

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.chatfamiliar.ui.auth.components.BotonAuth
import com.example.chatfamiliar.ui.auth.components.CampoCorreo
import com.example.chatfamiliar.ui.auth.components.CampoPassword
import com.example.chatfamiliar.ui.auth.components.MensajeError
import com.example.chatfamiliar.ui.language.ControlIdiomaCompacto
import com.example.chatfamiliar.ui.language.IdiomaViewModel
import com.example.chatfamiliar.ui.language.SelectorIdioma
import com.example.chatfamiliar.ui.language.TraducirTexto
import com.example.chatfamiliar.ui.language.recordarTextosApp
import com.example.chatfamiliar.ui.language.recordarIdiomaEfectivo


@Composable
fun PantallaLogin(
    idiomaViewModel: IdiomaViewModel,
    viewModel: LoginViewModel = viewModel(),
    alNavegarHome: () -> Unit,
    alNavegarRegistro: () -> Unit,
    alNavegarVerificacion: () -> Unit
) {
    val idiomaEfectivo = recordarIdiomaEfectivo(idiomaViewModel.idiomaSeleccionado)

    ContenidoLogin(correo = viewModel.correo,
        password = viewModel.password,
        errorRecurso = viewModel.errorRecurso,
        cargando = viewModel.cargando,
        idiomaSeleccionado = idiomaViewModel.idiomaSeleccionado,
        idiomaEfectivo = idiomaEfectivo,
        alCambiarCorreo = viewModel::actualizarCorreo,
        alCambiarPassword = viewModel::actualizarPassword,
        alSeleccionarIdioma = idiomaViewModel::seleccionarIdioma,
        traducir = idiomaViewModel::traducir,
        alIniciarSesion = {
            viewModel.iniciarSesion(
                alTenerExito = alNavegarHome,
                alRequerirVerificacion = alNavegarVerificacion)
        },
        alNavegarRegistro = alNavegarRegistro
    )
}


@Composable
fun ContenidoLogin(
    correo: String,
    password: String,

    @StringRes
    errorRecurso: Int?,
    cargando: Boolean,
    idiomaSeleccionado: String?,
    idiomaEfectivo: String,
    alCambiarCorreo: (String) -> Unit,
    alCambiarPassword: (String) -> Unit,
    alSeleccionarIdioma: (String?) -> Unit,
    traducir: TraducirTexto,
    alIniciarSesion: () -> Unit,
    alNavegarRegistro: () -> Unit
) {
    val textos = recordarTextosApp(idiomaEfectivo = idiomaEfectivo, traducir = traducir)

    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()
        .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ControlIdiomaCompacto(
                    idiomaSeleccionado = idiomaSeleccionado,
                    idiomaEfectivo = idiomaEfectivo,
                    traducir = traducir,
                    alSeleccionarIdioma = alSeleccionarIdioma)
            }
            Spacer(modifier = Modifier.height(20.dp))


            // Logo
            Surface(modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.Forum,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = textos.texto(R.string.brand_name),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Text(text = textos.texto(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = textos.texto(R.string.login_family_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(28.dp))

            // Formulario
            Card(modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme
                            .surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)
                ) {
                    Text(text = textos.texto(R.string.login_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = textos.texto(R.string.login_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(20.dp))
                    CampoCorreo(valor = correo,
                        alCambiarValor = alCambiarCorreo,
                        etiqueta = textos.texto(R.string.auth_email_label),
                        habilitado = !cargando)
                    Spacer(modifier = Modifier.height(16.dp))
                    CampoPassword(
                        valor = password,
                        alCambiarValor = alCambiarPassword,
                        etiqueta = textos.texto(R.string.auth_password_label),
                        descripcionMostrar = textos.texto(R.string.auth_show_password),
                        descripcionOcultar = textos.texto(R.string.auth_hide_password),
                        habilitado = !cargando)
                    Spacer(modifier = Modifier.height((8.dp)))
                    MensajeError(mensaje = errorRecurso?.let { recurso ->
                        textos.texto(recurso) })
                    Spacer(modifier = Modifier.height(16.dp))
                    BotonAuth(
                        texto = textos.texto(R.string.login_button),
                        cargando = cargando,
                        onClick = alIniciarSesion)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = textos.texto(R.string.login_secure),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = textos.texto(R.string.login_no_account),
                    style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = alNavegarRegistro,
                    enabled = !cargando
                ) {
                    Text(text = textos.texto(R.string.login_register),
                        fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}


@Composable
private fun SelectorIdiomaCompacto(
    idiomaSeleccionado: String?,
    descripcionSeleccionar: String,
    onClick: () -> Unit
) {
    val codigoVisible =
        when (idiomaSeleccionado) {
            IdiomaRepository.ESPANOL -> { "ES" }
            IdiomaRepository.INGLES -> { "EN" }
            IdiomaRepository.ITALIANO -> { "IT" }
            IdiomaRepository.FRANCES -> {"FR"}
            else -> { "AUTO" }
        }

    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Filled.Language,
                contentDescription = descripcionSeleccionar,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(7.dp))
            Text(text = codigoVisible,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun PantallaLoginPreview() {
    MaterialTheme {
        ContenidoLogin(
            correo = "familia@correo.com",
            password = "12345678",
            errorRecurso = null,
            cargando = false,
            idiomaSeleccionado = null,
            idiomaEfectivo = IdiomaRepository.ESPANOL,
            alCambiarCorreo = {},
            alCambiarPassword = {},
            alSeleccionarIdioma = {},
            traducir = { texto, _, alCompletar ->
                alCompletar(Result.success(texto))
            },
            alIniciarSesion = {},
            alNavegarRegistro = {}
        )
    }
}