package com.example.chatfamiliar.ui.auth.verification

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatfamiliar.R
import com.example.chatfamiliar.data.language.IdiomaRepository
import com.example.chatfamiliar.ui.auth.components.BotonAuth
import com.example.chatfamiliar.ui.auth.components.MensajeError
import com.example.chatfamiliar.ui.language.IdiomaViewModel
import com.example.chatfamiliar.ui.language.TraducirTexto
import com.example.chatfamiliar.ui.language.recordarIdiomaEfectivo
import com.example.chatfamiliar.ui.language.recordarTextosApp


@Composable
fun PantallaVerificacion(
    viewModel: VerificacionViewModel = viewModel(),
    idiomaViewModel: IdiomaViewModel = viewModel(),
    alNavegarHome: () -> Unit,
    alNavegarLogin: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.prepararPantalla() }
    val idiomaEfectivo = recordarIdiomaEfectivo(idiomaViewModel.idiomaSeleccionado)

    ContenidoVerificacion(
        correoUsuario = viewModel.correoUsuario,
        errorRecurso = viewModel.errorRecurso,
        mensajeRecurso = viewModel.mensajeRecurso,
        cargando = viewModel.cargando,
        idiomaEfectivo = idiomaEfectivo,
        traducir = idiomaViewModel::traducir,
        alComprobarVerificacion = {
            viewModel.comprobarVerificacion(
                alEstarVerificado = alNavegarHome) },
        alReenviarCorreo = { viewModel.reenviarCorreo() },
        alCambiarCuenta = {
            viewModel.cerrarSesion(alCerrarSesion = alNavegarLogin) }
    )
}

@Composable
fun ContenidoVerificacion(
    correoUsuario: String,
    @StringRes
    errorRecurso: Int?,
    @StringRes
    mensajeRecurso: Int?,
    cargando: Boolean,
    idiomaEfectivo: String,
    traducir: TraducirTexto,
    alComprobarVerificacion: () -> Unit,
    alReenviarCorreo: () -> Unit,
    alCambiarCuenta: () -> Unit
) {
    val textos = recordarTextosApp(idiomaEfectivo = idiomaEfectivo,
            traducir = traducir)
    Box(modifier = Modifier.fillMaxSize()
            .safeDrawingPadding().imePadding()
    ) {
        Column(modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono principal
            Surface(
                modifier = Modifier.size(76.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        modifier = Modifier.size(38.dp),
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            // Título
            Text(text = textos.texto(R.string.verification_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(6.dp))
            // Recordatorio de revisar spam
            Text(text = textos.texto(R.string.verification_spam),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            // Tarjeta con el correo y las instrucciones
            Card(modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme
                            .surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = textos.texto(R.string.verification_sent_to),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Correo actual
                    Text(text = correoUsuario.ifBlank { textos
                            .texto(R.string.verification_email_placeholder) },
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(18.dp))
                    // Instrucciones
                    Text(text = textos.texto(
                            R.string.verification_instructions),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                }
            }
            // Mensaje informativo
            AnimatedVisibility(
                visible = mensajeRecurso != null
            ) { mensajeRecurso?.let { recurso ->
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(text = textos.texto(recurso),
                                modifier = Modifier.padding(14.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            // Error técnico
            MensajeError(mensaje = errorRecurso?.let { recurso ->
                        textos.texto(recurso) })
            Spacer(modifier = Modifier.height(16.dp))
            // Acción principal
            BotonAuth(texto = textos.texto(
                R.string.verification_check_button),
                cargando = cargando,
                onClick = alComprobarVerificacion)
            Spacer(modifier = Modifier.height(12.dp))
            // Reenviar correo.
            OutlinedButton(onClick = alReenviarCorreo, enabled = !cargando,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = textos.texto(R.string.verification_resend_button))
            }
            Spacer(modifier = Modifier.height(6.dp))
            // Cambiar de usuario
            TextButton(onClick = alCambiarCuenta, enabled = !cargando
            ) { Text(text = textos
                .texto(R.string.verification_change_account),
                fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun PantallaVerificacionPreview() {
    MaterialTheme {
        ContenidoVerificacion(
            correoUsuario = "familia@correo.com",
            errorRecurso = null,
            mensajeRecurso =
                R.string.verification_message_email_sent,
            cargando = false,
            idiomaEfectivo = IdiomaRepository.ESPANOL,
            traducir = { texto, _, alCompletar ->
                alCompletar(Result.success(texto)) },
            alComprobarVerificacion = {},
            alReenviarCorreo = {},
            alCambiarCuenta = {}
        )
    }
}