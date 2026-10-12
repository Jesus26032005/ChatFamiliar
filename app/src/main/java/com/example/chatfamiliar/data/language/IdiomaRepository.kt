package com.example.chatfamiliar.data.language

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/** Guarda el idioma elegido por el usuario y lo aplica a la aplicación. */
class IdiomaRepository(
    context: Context) {
    private val preferencias =
        context.getSharedPreferences(NOMBRE_PREFERENCIAS,
            Context.MODE_PRIVATE)
    /**
     * Guarda y aplica el idioma. Español e inglés usan los recursos de la app;
     * los idiomas traducidos con ML Kit usan español como base. null = idioma del sistema.
     */
    fun cambiarIdioma(codigoIdioma: String?) { guardarIdiomaSeleccionado(codigoIdioma)
        val idiomas = when (codigoIdioma) { null -> { LocaleListCompat.getEmptyLocaleList() }
            ESPANOL, INGLES -> { LocaleListCompat.forLanguageTags(codigoIdioma) }
            else -> { LocaleListCompat.forLanguageTags(ESPANOL) } }
        AppCompatDelegate.setApplicationLocales(idiomas) }

    /** Idioma guardado, o null si se sigue el idioma del sistema. */
    fun obtenerIdiomaSeleccionado(): String? { if (!preferencias.contains(CLAVE_IDIOMA)) { return null }
        val idiomaGuardado = preferencias.getString(CLAVE_IDIOMA,
            SISTEMA)
        return if (idiomaGuardado ==
            SISTEMA) { null } else { idiomaGuardado } }
    /** Persiste la elección en SharedPreferences. */
    private fun guardarIdiomaSeleccionado(codigoIdioma: String?) { preferencias.edit()
            .putString(CLAVE_IDIOMA, codigoIdioma ?: SISTEMA)
            .apply() }
    companion object {
        const val ESPANOL = "es"
        const val INGLES = "en"
        // Traducción automática con ML Kit.
        const val ITALIANO = "it"
        const val FRANCES = "fr"
        private const val NOMBRE_PREFERENCIAS = "preferencias_idioma"
        private const val CLAVE_IDIOMA = "idioma_seleccionado"
        private const val SISTEMA = "sistema"
    } }