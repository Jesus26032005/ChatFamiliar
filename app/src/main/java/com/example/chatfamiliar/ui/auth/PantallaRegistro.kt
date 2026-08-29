package com.example.chatfamiliar.ui.auth
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun PantallaRegistro(
    viewModel: RegistroViewModel = viewModel(),
    alNavegarLogin: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PersonAdd,
            contentDescription = "Crear cuenta",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Únete a FamiliaChat",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text("Crea tu cuenta ahora", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(32.dp))


        CampoCorreo(
            valor = "Correo electrónico",
            alCambiarValor = {viewModel.correo = it},
            habilitado = !viewModel.cargando,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        CampoPassword(
            valor = viewModel.password,
            alCambiarValor = {viewModel.password = it},
            etiqueta = "Contraseña",
            habilitado = !viewModel.cargando,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        CampoPassword(
            valor = viewModel.confirmarPassword,
            alCambiarValor = {viewModel.confirmarPassword = it},
            etiqueta = "Confirmar contraseña",
            habilitado = !viewModel.cargando,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        MensajeError(viewModel.errorVisual)

        BotonAuth(
            texto = "Registrarse",
            cargando = viewModel.cargando,
            onClick = {viewModel.crearCuenta(alTenerExito = alNavegarLogin)}
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = alNavegarLogin,
            enabled = !viewModel.cargando
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión aquí")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPantallaRegistro() {
    MaterialTheme {
        PantallaRegistro(
            alNavegarLogin = {}
        )
    }
}