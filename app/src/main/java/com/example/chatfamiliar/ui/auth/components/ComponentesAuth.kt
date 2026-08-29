package com.example.chatfamiliar.ui.auth.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CampoCorreo(valor: String, alCambiarValor: (String) -> Unit, habilitado: Boolean, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = valor, onValueChange = alCambiarValor,
        label = { Text(text = "Correo electrónico") },
        leadingIcon = { Icon(imageVector = Icons.Filled.Email, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        enabled = habilitado,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun CampoPassword(valor: String, alCambiarValor: (String) -> Unit, etiqueta: String, habilitado: Boolean, modifier: Modifier = Modifier) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = alCambiarValor,
        label = { Text(text = etiqueta) },
        leadingIcon = { Icon(imageVector = Icons.Filled.Lock, contentDescription = null) },
        trailingIcon = {
            val imagen = if (passwordVisible) { Icons.Filled.Visibility } else { Icons.Filled.VisibilityOff }
            val descripcion = if (passwordVisible) { "Ocultar contraseña" } else { "Mostrar contraseña" }
            IconButton(onClick = { passwordVisible = !passwordVisible }, enabled = habilitado) { Icon(imageVector = imagen, contentDescription = descripcion) }},
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (passwordVisible) { VisualTransformation.None } else { PasswordVisualTransformation() },
        singleLine = true,
        enabled = habilitado,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun MensajeError(mensaje: String?, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible = mensaje != null, enter = fadeIn(), exit = fadeOut()) {
        Text(text = mensaje.orEmpty(), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = modifier.padding(bottom = 16.dp))
    }
}

@Composable
fun BotonAuth(texto: String, cargando: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, enabled = !cargando, shape = RoundedCornerShape(16.dp), modifier = modifier.fillMaxWidth().height(55.dp)) {
        if (cargando) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            Text(text = texto, style = MaterialTheme.typography.titleMedium)
        }
    }
}