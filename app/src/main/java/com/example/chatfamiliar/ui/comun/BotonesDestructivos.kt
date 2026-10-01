package com.example.chatfamiliar.ui.comun

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.ui.theme.funcional

@Composable
fun BotonDestructivo(onClick: () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable RowScope.() -> Unit) {
    val colores = MaterialTheme.funcional
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        border = BorderStroke(width = 1.dp,
            color = if (enabled) { colores.destructivo.copy(alpha = 0.6f)
            } else { MaterialTheme.colorScheme.outlineVariant }),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = colores.destructivo),
        content = content) }
@Composable
fun BotonConfirmarDestructivo(
    onClick: () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, content: @Composable RowScope.() -> Unit) {
    val colores = MaterialTheme.funcional
    Button(onClick = onClick, modifier = modifier, enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = colores.destructivo,
            contentColor = colores.sobreDestructivo), content = content)
}