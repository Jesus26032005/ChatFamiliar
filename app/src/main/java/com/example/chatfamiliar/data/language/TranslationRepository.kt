package com.example.chatfamiliar.data.language

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

class TranslationRepository {
    // Un traductor por idioma de destino.
    private val traductores =
        mutableMapOf<String, Translator>()

    // Guarda temporalmente las traducciones ya realizadas.
    private val cacheTraducciones =
        mutableMapOf<String, String>()

    fun traducir(
        texto: String,
        codigoDestino: String,
        alCompletar: (Result<String>) -> Unit
    ) {
        // Español es nuestro idioma base.
        if (codigoDestino == IdiomaRepository.ESPANOL) {
            alCompletar(Result.success(texto))
            return
        }

        val claveCache = "$codigoDestino::$texto"
        // Si ya fue traducido, regresamos inmediatamente.
        cacheTraducciones[claveCache]?.let { traduccion ->
            alCompletar(Result.success(traduccion))
            return
        }

        val idiomaDestino = TranslateLanguage.fromLanguageTag(codigoDestino)

        if (idiomaDestino == null) {
            alCompletar(
                Result.failure(
                    IllegalArgumentException(
                        "El idioma seleccionado no es compatible con la traducción.")
                )
            )
            return
        }


        val traductor =
            traductores.getOrPut(codigoDestino) {
                val opciones =
                    TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.SPANISH)
                        .setTargetLanguage(idiomaDestino)
                        .build()
                Translation.getClient(opciones)
            }


        val condiciones = DownloadConditions.Builder().build()


        traductor
            .downloadModelIfNeeded(condiciones)
            .addOnSuccessListener {
                traductor
                    .translate(texto)
                    .addOnSuccessListener { textoTraducido ->
                        // Guardamos el resultado para futuras consultas.
                        cacheTraducciones[claveCache] = textoTraducido
                        alCompletar(Result.success(textoTraducido))
                    }
                    .addOnFailureListener { excepcion ->
                        alCompletar(Result.failure(excepcion)
                        )
                    }
            }
            .addOnFailureListener { excepcion ->
                alCompletar(Result.failure(excepcion)
                )
            }
    }


    fun cerrar() {
        traductores.values.forEach { traductor ->
            traductor.close() }
        traductores.clear()
        cacheTraducciones.clear()
    }
}