package com.example.chatfamiliar.ui.auth.register

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
import androidx.compose.material.icons.filled.PersonAdd
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

@Composable
fun PantallaRegistro(viewModel: RegistroViewModel = viewModel(), alNavegarVerificacion: () -> Unit, alNavegarLogin: () -> Unit) {
    ContenidoRegistro(
        correo = viewModel.correo,
        password = viewModel.password,
        confirmarPassword = viewModel.confirmarPassword,
        errorVisual = viewModel.errorVisual,
        cargando = viewModel.cargando,

        alCambiarCorreo = viewModel::actualizarCorreo,
        alCambiarPassword = viewModel::actualizarPassword,
        alCambiarConfirmarPassword = viewModel::actualizarConfirmarPassword,

        alRegistrar = { viewModel.crearCuenta(alTenerExito = alNavegarVerificacion) },
        alNavegarLogin = alNavegarLogin
    )
}

@Composable
fun ContenidoRegistro(
    correo: String, password: String, confirmarPassword: String, errorVisual: String?, cargando: Boolean, alCambiarCorreo: (String) -> Unit,
    alCambiarPassword: (String) -> Unit, alCambiarConfirmarPassword: (String) -> Unit, alRegistrar: () -> Unit, alNavegarLogin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Filled.PersonAdd, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 16.dp))

        Text(text = "Únete a FamiliaChat", style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        Text(text = "Crea tu cuenta para mantenerte en contacto con tu familia", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(32.dp))

        CampoCorreo(valor = correo, alCambiarValor = alCambiarCorreo, habilitado = !cargando,
            modifier = Modifier.padding(bottom = 16.dp))

        CampoPassword(valor = password, alCambiarValor = alCambiarPassword, etiqueta = "Contraseña",
            habilitado = !cargando, modifier = Modifier.padding(bottom = 16.dp))

        CampoPassword(valor = confirmarPassword, alCambiarValor = alCambiarConfirmarPassword, etiqueta = "Confirmar contraseña",
            habilitado = !cargando, modifier = Modifier.padding(bottom = 16.dp))

        MensajeError(mensaje = errorVisual)

        BotonAuth(texto = "Registrarse", cargando = cargando, onClick = alRegistrar)

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = alNavegarLogin, enabled = !cargando) {
            Text(text = "¿Ya tienes cuenta? Inicia sesión") }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PantallaRegistroPreview() {
    MaterialTheme {
        ContenidoRegistro(
            correo = "familia@correo.com", password = "12345678", confirmarPassword = "12345678", errorVisual = null, cargando = false,
            alCambiarCorreo = {}, alCambiarPassword = {}, alCambiarConfirmarPassword = {}, alRegistrar = {}, alNavegarLogin = {})
    }
}