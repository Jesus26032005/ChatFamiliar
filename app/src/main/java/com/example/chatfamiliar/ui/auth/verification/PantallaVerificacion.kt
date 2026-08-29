package com.example.chatfamiliar.ui.auth.verification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.chatfamiliar.ui.auth.components.BotonAuth
import com.example.chatfamiliar.ui.auth.components.MensajeError


@Composable
fun PantallaVerificacion(viewModel: VerificacionViewModel = viewModel(), alNavegarHome: () -> Unit, alNavegarLogin: () -> Unit) {
    LaunchedEffect(Unit) { viewModel.prepararPantalla() }

    ContenidoVerificacion(
        correoUsuario = viewModel.correoUsuario,
        errorVisual = viewModel.errorVisual,
        mensajeVisual = viewModel.mensajeVisual,
        cargando = viewModel.cargando,

        alComprobarVerificacion = { viewModel.comprobarVerificacion(alEstarVerificado = alNavegarHome) },
        alReenviarCorreo = { viewModel.reenviarCorreo() },
        alCambiarCuenta = { viewModel.cerrarSesion(alCerrarSesion = alNavegarLogin) }
    )
}


@Composable
fun ContenidoVerificacion(
    correoUsuario: String, errorVisual: String?, mensajeVisual: String?, cargando: Boolean,
    alComprobarVerificacion: () -> Unit, alReenviarCorreo: () -> Unit, alCambiarCuenta: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Filled.Email, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 16.dp))

        Text(text = "!Falta tu verificacion¡", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)

        Text(text = "Checa tu bandeja de spam", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Enviamos un enlace de verificación a:", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = correoUsuario.ifBlank { "Tu correo electrónico" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Abre el correo y pulsa el enlace para verificar tu cuenta. Después vuelve a la aplicación y presiona el botón de abajo.",
            style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(visible = mensajeVisual != null, enter = fadeIn(), exit = fadeOut()) {
            Text(text = mensajeVisual.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 16.dp)) }

        MensajeError(mensaje = errorVisual)

        BotonAuth(texto = "Ya verifiqué mi correo", cargando = cargando, onClick = alComprobarVerificacion)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(onClick = alReenviarCorreo, enabled = !cargando) { Text(text = "Reenviar correo de verificación") }

        TextButton(onClick = alCambiarCuenta, enabled = !cargando) { Text(text = "Cambiar de cuenta") }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PantallaVerificacionPreview() {
    MaterialTheme {
        ContenidoVerificacion(
            correoUsuario = "familia@correo.com", errorVisual = null, mensajeVisual = "Enviamos un enlace de verificación a tu correo electrónico.",
            cargando = false, alComprobarVerificacion = {}, alReenviarCorreo = {}, alCambiarCuenta = {})
    }
}