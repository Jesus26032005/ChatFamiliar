package com.example.chatfamiliar.data.language

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class IdiomaRepository(
    context: Context
) {
    private val preferencias =
        context.getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE)
    fun cambiarIdioma(
        codigoIdioma: String?
    ) {
        guardarIdiomaSeleccionado(codigoIdioma)
        val idiomas = when (codigoIdioma) {
            null -> { LocaleListCompat.getEmptyLocaleList() }
            ESPANOL, INGLES -> { LocaleListCompat.forLanguageTags(codigoIdioma) }
            else -> { LocaleListCompat.forLanguageTags(ESPANOL) }
        }
        AppCompatDelegate.setApplicationLocales(
            idiomas
        )
    }


    fun obtenerIdiomaSeleccionado(): String? {
        /*Si todavía no existe esta preferencia,
        significa que queremos seguir el sistema */
        if (!preferencias.contains(CLAVE_IDIOMA)) { return null }
        val idiomaGuardado = preferencias.getString(CLAVE_IDIOMA, SISTEMA)
        return if (idiomaGuardado == SISTEMA) { null } else { idiomaGuardado }
    }


    private fun guardarIdiomaSeleccionado(
        codigoIdioma: String?
    ) {
        preferencias
            .edit()
            .putString(
                CLAVE_IDIOMA,
                codigoIdioma ?: SISTEMA
            )
            .apply()
    }


    companion object {
        const val ESPANOL = "es"
        const val INGLES = "en"

        // Traducción automática con ML Kit.
        const val ITALIANO = "it"
        const val FRANCES = "fr"

        private const val NOMBRE_PREFERENCIAS = "preferencias_idioma"
        private const val CLAVE_IDIOMA = "idioma_seleccionado"
        private const val SISTEMA = "sistema"
    }
}