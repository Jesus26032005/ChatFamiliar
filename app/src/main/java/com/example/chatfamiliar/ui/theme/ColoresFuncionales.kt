package com.example.chatfamiliar.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class TonoIdentidad(
    val contenedor: Color,
    val sobreContenedor: Color,
    val suave: Color,
    val texto: Color
)

@Immutable
data class ColoresFuncionales(
    val encabezado: Color,
    val accionPrincipal: Color,
    val sobreAccionPrincipal: Color,
    val burbujaPropia: Color,
    val sobreBurbujaPropia: Color,
    val metaBurbujaPropia: Color,
    val burbujaRecibida: Color,
    val sobreBurbujaRecibida: Color,
    val metaBurbujaRecibida: Color,
    val filtroSeleccionado: Color,
    val sobreFiltroSeleccionado: Color,
    val bordeFiltroSeleccionado: Color,
    val acento: Color,
    val insignia: Color,
    val sobreInsignia: Color,
    val bordePendiente: Color,
    val etiquetaNeutra: Color,
    val sobreEtiquetaNeutra: Color,
    val tarjeta: Color,
    val tarjetaInterior: Color,
    val destructivo: Color,
    val sobreDestructivo: Color,

    val tonos: List<TonoIdentidad>
) {
    fun tonoPara(semilla: String): TonoIdentidad {
        if (semilla.isBlank()) return tonos.first()
        return tonos[Math.floorMod(semilla.hashCode(), tonos.size)]
    }
}

private val TonosClaros = listOf(
    TonoIdentidad(Color(0xFFE2E5FF), Color(0xFF101B69), Color(0xFFF1F2FF), Color(0xFF4F5FD7)),
    TonoIdentidad(Color(0xFFBCEBEB), Color(0xFF002020), Color(0xFFEAF6F6), Color(0xFF2F6E6E)),
    TonoIdentidad(Color(0xFFFFD9E1), Color(0xFF3F001D), Color(0xFFFFF0F3), Color(0xFFA94F64)),
    TonoIdentidad(Color(0xFFEADDFF), Color(0xFF25005A), Color(0xFFF5EEFF), Color(0xFF6D4FC0)),
    TonoIdentidad(Color(0xFFCFE5FF), Color(0xFF001D34), Color(0xFFEEF5FF), Color(0xFF2B6497))
)

private val TonosOscuros = listOf(
    TonoIdentidad(Color(0xFF37439E), Color(0xFFE2E5FF), Color(0xFF1C1F33), Color(0xFFBCC3FF)),
    TonoIdentidad(Color(0xFF1E5050), Color(0xFFBCEBEB), Color(0xFF162426), Color(0xFFA0CFCF)),
    TonoIdentidad(Color(0xFF882F47), Color(0xFFFFD9E1), Color(0xFF2A1A20), Color(0xFFFFB1C1)),
    TonoIdentidad(Color(0xFF553A8F), Color(0xFFEADDFF), Color(0xFF221B30), Color(0xFFD0BCFF)),
    TonoIdentidad(Color(0xFF1F4A70), Color(0xFFCFE5FF), Color(0xFF172230), Color(0xFF9CCAFF))
)

fun coloresFuncionales(esquema: ColorScheme, oscuro: Boolean): ColoresFuncionales =
    ColoresFuncionales(
        encabezado = esquema.primary,
        accionPrincipal = esquema.primary,
        sobreAccionPrincipal = esquema.onPrimary,
        burbujaPropia = if (oscuro) FamiliaBurbujaPropiaDark else esquema.primary,
        sobreBurbujaPropia = if (oscuro) Color.White else esquema.onPrimary,
        metaBurbujaPropia = (if (oscuro) Color.White else esquema.onPrimary).copy(alpha = 0.78f),
        burbujaRecibida = if (oscuro) FamiliaBurbujaRecibidaDark else FamiliaBurbujaRecibida,
        sobreBurbujaRecibida = esquema.onSurface,
        metaBurbujaRecibida = esquema.onSurfaceVariant,
        filtroSeleccionado = esquema.primaryContainer,
        sobreFiltroSeleccionado = esquema.onPrimaryContainer,
        bordeFiltroSeleccionado = esquema.primary.copy(alpha = 0.5f),
        acento = esquema.tertiary,
        insignia = if (oscuro) esquema.tertiaryContainer else esquema.tertiary,
        sobreInsignia = if (oscuro) esquema.onTertiaryContainer else esquema.onTertiary,
        bordePendiente = esquema.tertiary.copy(alpha = if (oscuro) 0.35f else 0.55f),

        etiquetaNeutra = esquema.surfaceContainerHighest,
        sobreEtiquetaNeutra = esquema.onSurfaceVariant,

        tarjeta = esquema.surfaceContainerLow,
        tarjetaInterior = if (oscuro) esquema.surfaceContainerHigh
        else esquema.surfaceContainerLowest,

        destructivo = esquema.error,
        sobreDestructivo = esquema.onError,

        tonos = if (oscuro) TonosOscuros else TonosClaros
    )

val LocalColoresFuncionales = staticCompositionLocalOf {
    coloresFuncionales(EsquemaClaroBase, oscuro = false)
}

val MaterialTheme.funcional: ColoresFuncionales
    @Composable
    @ReadOnlyComposable
    get() = LocalColoresFuncionales.current