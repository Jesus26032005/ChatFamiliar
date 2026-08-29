package com.example.chatfamiliar.ui.auth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

@Composable
fun CampoCorreo(valor: String,  alCambiarValor: (String) -> Unit, habilitado: Boolean, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiarValor,
        label = { Text("Correo electrónico") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Email,
                contentDescription = "Icono correo"
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        enabled = habilitado,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun CampoPassword(valor: String, alCambiarValor: (String) -> Unit, etiqueta: String, habilitado: Boolean, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = alCambiarValor,
        label = { Text(etiqueta) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Icono candado"
            )
        },
        trailingIcon = {
            val imagen = if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val descripcion = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = imagen,
                    contentDescription = descripcion
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visible) { VisualTransformation.None } else { PasswordVisualTransformation() },
        singleLine = true,
        enabled = habilitado,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun BotonAuth(texto: String, cargando: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        enabled = !cargando,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth().height(55.dp)) {
        if (cargando) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            Text(text = texto, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun MensajeError(mensaje: String?) {
    AnimatedVisibility(visible = mensaje != null, enter = fadeIn(), exit = fadeOut()) {
        Text(text = mensaje.orEmpty(), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
    }
}
