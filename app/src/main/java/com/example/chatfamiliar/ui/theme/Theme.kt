package com.example.chatfamiliar.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = FamiliaPrimary,
    onPrimary = FamiliaOnPrimary,
    primaryContainer = FamiliaPrimaryContainer,
    onPrimaryContainer = FamiliaOnPrimaryContainer,

    secondary = FamiliaSecondary,
    onSecondary = FamiliaOnSecondary,
    secondaryContainer = FamiliaSecondaryContainer,
    onSecondaryContainer = FamiliaOnSecondaryContainer,

    tertiary = FamiliaTertiary,
    onTertiary = FamiliaOnTertiary,
    tertiaryContainer = FamiliaTertiaryContainer,
    onTertiaryContainer = FamiliaOnTertiaryContainer,

    background = FamiliaBackground,
    onBackground = FamiliaOnBackground,

    surface = FamiliaSurface,
    onSurface = FamiliaOnSurface,
    surfaceContainer = FamiliaSurfaceContainer,

    surfaceVariant = FamiliaSurfaceVariant,
    onSurfaceVariant = FamiliaOnSurfaceVariant,

    outline = FamiliaOutline,

    error = FamiliaError,
    onError = FamiliaOnError,
    errorContainer = FamiliaErrorContainer,
    onErrorContainer = FamiliaOnErrorContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = FamiliaPrimaryDark,
    onPrimary = FamiliaOnPrimaryDark,
    primaryContainer = FamiliaPrimaryContainerDark,
    onPrimaryContainer = FamiliaOnPrimaryContainerDark,

    secondary = FamiliaSecondaryDark,
    onSecondary = FamiliaOnSecondaryDark,
    secondaryContainer = FamiliaSecondaryContainerDark,
    onSecondaryContainer = FamiliaOnSecondaryContainerDark,

    tertiary = FamiliaTertiaryDark,
    onTertiary = FamiliaOnTertiaryDark,
    tertiaryContainer = FamiliaTertiaryContainerDark,
    onTertiaryContainer = FamiliaOnTertiaryContainerDark,

    background = FamiliaBackgroundDark,
    onBackground = FamiliaOnBackgroundDark,

    surface = FamiliaSurfaceDark,
    onSurface = FamiliaOnSurfaceDark,
    surfaceContainer = FamiliaSurfaceContainerDark,

    surfaceVariant = FamiliaSurfaceVariantDark,
    onSurfaceVariant = FamiliaOnSurfaceVariantDark,

    outline = FamiliaOutlineDark,

    error = FamiliaErrorDark,
    onError = FamiliaOnErrorDark,
    errorContainer = FamiliaErrorContainerDark,
    onErrorContainer = FamiliaOnErrorContainerDark
)

@Composable
fun ChatFamiliarTheme(darkTheme: Boolean = isSystemInDarkTheme(),
                      dynamicColor: Boolean = false,
                      content: @Composable () -> Unit) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) { dynamicDarkColorScheme(context)
            } else { dynamicLightColorScheme(context)
            }
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(colorScheme = colorScheme,
        typography = Typography, content = content)
}