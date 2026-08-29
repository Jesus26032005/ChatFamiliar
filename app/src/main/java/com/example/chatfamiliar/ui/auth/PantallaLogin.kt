package com.example.chatfamiliar.ui.auth
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PantallaLogin(
    viewModel: LoginViewModel = viewModel(),
    alNavegarHome: () -> Unit,
    alNavegarRegistro: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Forum,
            contentDescription = "Logo",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "FamiliaChat",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text("Un chat solo para la familia", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(32.dp))


        CampoCorreo(valor = viewModel.correo,
            alCambiarValor = {viewModel.correo = it},
            habilitado = !viewModel.cargando,
            modifier = Modifier.padding(bottom = 16.dp))

        CampoPassword(valor = viewModel.password,
            alCambiarValor = {viewModel.password = it},
            etiqueta = "Contraseña",
            habilitado = !viewModel.cargando,
            modifier= Modifier.padding(bottom = 16.dp))

        MensajeError(viewModel.errorVisual)

        BotonAuth(
            texto = "Iniciar sesión",
            cargando = viewModel.cargando,
            onClick = {viewModel.iniciarSesion(alTenerExito = alNavegarHome)}
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = alNavegarRegistro,
            enabled = !viewModel.cargando
        ) {
            Text(text = "¿No tienes cuenta? Regístrate aquí")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaLoginPreview() {
    MaterialTheme {
        PantallaLogin(
            alNavegarHome = {}, alNavegarRegistro = {}
        )
    }
}