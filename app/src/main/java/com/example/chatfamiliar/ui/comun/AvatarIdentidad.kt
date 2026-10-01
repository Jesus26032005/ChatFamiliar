package com.example.chatfamiliar.ui.comun

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.ui.theme.funcional
import java.util.Locale

@Composable
fun AvatarIdentidad(
    semilla: String,
    modifier: Modifier = Modifier,
    nombre: String = "",
    icono: ImageVector? = null,
    esGrupo: Boolean = false,
    tamano: Dp = 54.dp,
    idioma: String = Locale.getDefault().toLanguageTag()
) {
    val tono = MaterialTheme.funcional.tonoPara(semilla)
    val forma: Shape = if (esGrupo) RoundedCornerShape(tamano * 0.3f) else CircleShape
    val iniciales = inicialesDe(nombre, idioma)
    Surface(modifier = modifier.size(tamano).clearAndSetSemantics { },
        shape = forma, color = tono.contenedor, contentColor = tono.sobreContenedor) {
        Box(contentAlignment = Alignment.Center) {
            if (icono != null || iniciales.isBlank()) {
                Icon(imageVector = icono ?: Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(tamano * 0.5f))
            } else {
                Text(text = iniciales,
                    style = if (tamano >= 48.dp) { MaterialTheme.typography.titleMedium
                    } else { MaterialTheme.typography.labelLarge },
                    fontWeight = FontWeight.SemiBold) } } }
}

fun inicialesDe(nombre: String, idioma: String): String = nombre
    .trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    .take(2).joinToString(separator = "") { it.take(1) }
    .uppercase(Locale.forLanguageTag(idioma))