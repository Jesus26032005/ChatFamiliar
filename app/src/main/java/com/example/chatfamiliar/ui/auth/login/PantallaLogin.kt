package com.example.chatfamiliar.ui.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatfamiliar.ui.auth.components.BotonAuth
import com.example.chatfamiliar.ui.auth.components.CampoCorreo
import com.example.chatfamiliar.ui.auth.components.CampoPassword
import com.example.chatfamiliar.ui.auth.components.MensajeError
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface


@Composable
fun PantallaLogin(
    viewModel: LoginViewModel = viewModel(),
    alNavegarHome: () -> Unit,
    alNavegarRegistro: () -> Unit,
    alNavegarVerificacion: () -> Unit
) {
    ContenidoLogin(
        correo = viewModel.correo,
        password = viewModel.password,
        errorVisual = viewModel.errorVisual,
        cargando = viewModel.cargando,

        alCambiarCorreo = viewModel::actualizarCorreo,
        alCambiarPassword = viewModel::actualizarPassword,

        alIniciarSesion = {
            viewModel.iniciarSesion(
                alTenerExito = alNavegarHome,
                alRequerirVerificacion =
                    alNavegarVerificacion
            )
        },
        alNavegarRegistro = alNavegarRegistro
    ) }


@Composable
fun ContenidoLogin(
    correo: String,
    password: String,
    errorVisual: String?,
    cargando: Boolean,
    alCambiarCorreo: (String) -> Unit,
    alCambiarPassword: (String) -> Unit,
    alIniciarSesion: () -> Unit,
    alNavegarRegistro: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize().safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer) {
            Box(
                contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Forum,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                ) } }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "FamiliaChat",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Text(text = "Un chat solo para la familia",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Mantente conectado con los tuyos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(20.dp))
        Card(modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme
                    .colorScheme.surfaceContainer)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Inicia sesión",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Ingresa tus datos para continuar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme
                        .onSurfaceVariant)
                Spacer(modifier = Modifier.height(20.dp))
                CampoCorreo(valor = correo,
                    alCambiarValor = alCambiarCorreo,
                    habilitado = !cargando)
                Spacer(modifier = Modifier.height(16.dp))
                CampoPassword(valor = password,
                    alCambiarValor = alCambiarPassword,
                    etiqueta = "Contraseña",
                    habilitado = !cargando,
                    modifier = Modifier.padding(bottom = 16.dp))
                Spacer(modifier = Modifier.height(4.dp))
                MensajeError(mensaje = errorVisual)
                Spacer(modifier = Modifier.height(8.dp))
                BotonAuth(texto = "Iniciar sesión",
                    cargando = cargando,
                    onClick = alIniciarSesion)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Inicio de sesión seguro",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ) } } }
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "¿No tienes una cuenta?",
                style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = alNavegarRegistro, enabled = !cargando) {
                Text(text = "Regístrate") } }
    } }


@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun PantallaLoginPreview() {
    MaterialTheme {
        ContenidoLogin(
            correo = "familia@correo.com", password = "12345678", errorVisual = null, cargando = false,
            alCambiarCorreo = {}, alCambiarPassword = {}, alIniciarSesion = {}, alNavegarRegistro = {}
        )
    }
}