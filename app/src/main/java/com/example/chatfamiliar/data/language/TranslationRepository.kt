package com.example.chatfamiliar.data.language

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

/**
 * Traducción en el dispositivo con ML Kit desde español al idioma elegido.
 * Guarda en memoria los traductores, las descargas de modelos y las traducciones ya hechas.
 */
class TranslationRepository {
    private val traductores =
        mutableMapOf<String, Translator>()
    private val descargas =
        mutableMapOf<String, Task<Void>>()
    private val reinicios =
        mutableMapOf<String, Task<Void>>()
    private val cacheTraducciones =
        mutableMapOf<String, String>()
    private val gestorModelos = RemoteModelManager.getInstance()

    /** Traduce un texto; si ya se tradujo antes responde desde la caché. */
    fun traducir(texto: String, codigoDestino: String,
        alCompletar: (Result<String>) -> Unit) { if (codigoDestino == IdiomaRepository.ESPANOL) {
            alCompletar(Result.success(texto))
            return }
        val claveCache = "$codigoDestino::$texto"
        cacheTraducciones[claveCache]?.let { traduccion ->
            alCompletar(Result.success(traduccion))
            return }
        val idiomaDestino = TranslateLanguage
            .fromLanguageTag(codigoDestino)
        if (idiomaDestino == null) { alCompletar(Result.failure(IllegalArgumentException(
                        "El idioma seleccionado no es " +
                                "compatible con la traducción.")))
            return }
        traducirConReintento(texto = texto,
            codigoDestino = codigoDestino,
            idiomaDestino = idiomaDestino,
            claveCache = claveCache,
            puedeReintentar = true,
            alCompletar = alCompletar) }

    /** Si el modelo falla, lo borra, lo vuelve a descargar y reintenta una vez. */
    private fun traducirConReintento(texto: String, codigoDestino: String,
        idiomaDestino: String, claveCache: String,
        puedeReintentar: Boolean, alCompletar: (Result<String>) -> Unit) {
        val traductor = obtenerTraductor(codigoDestino, idiomaDestino)
        descargarModelo(codigoDestino, traductor)
            .addOnSuccessListener { traductor
                    .translate(texto)
                    .addOnSuccessListener { textoTraducido ->
                        cacheTraducciones[claveCache] = textoTraducido
                        alCompletar(Result.success(textoTraducido)) }
                    .addOnFailureListener { excepcion ->
                        if (puedeReintentar && excepcion is MlKitException) {
                            reiniciarModelo(codigoDestino, idiomaDestino)
                                .addOnCompleteListener { traducirConReintento(
                                        texto = texto, codigoDestino = codigoDestino,
                                        idiomaDestino = idiomaDestino, claveCache = claveCache,
                                        puedeReintentar = false, alCompletar = alCompletar) }
                        } else { alCompletar(Result.failure(excepcion)) } } }
            .addOnFailureListener { excepcion ->
                descargas.remove(codigoDestino)
                alCompletar(Result.failure(excepcion)) } }

    /** Reutiliza el traductor de cada idioma destino. */
    private fun obtenerTraductor(codigoDestino: String, idiomaDestino: String): Translator =
        traductores.getOrPut(codigoDestino) { val opciones = TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage
                        .SPANISH)
                    .setTargetLanguage(idiomaDestino)
                    .build()
            Translation.getClient(opciones) }

    /** Descarga el modelo del idioma solo si hace falta (una descarga por idioma). */
    private fun descargarModelo(codigoDestino: String,
        traductor: Translator): Task<Void> =
        descargas.getOrPut(codigoDestino) { android.util.Log.d("TraduccionApp", "Descargando modelo: $codigoDestino")
            val condiciones = DownloadConditions.Builder().build()
            traductor.downloadModelIfNeeded(condiciones)
                .addOnSuccessListener { android.util.Log.d("TraduccionApp", "Modelo listo: $codigoDestino") }
                .addOnFailureListener { error ->
                    android.util.Log.w("TraduccionApp",
                        "Falló la descarga de $codigoDestino: ${error.message}", error) } }

    /** Cierra el traductor y elimina el modelo descargado para forzar una descarga limpia. */
    private fun reiniciarModelo(codigoDestino: String, idiomaDestino: String): Task<Void> =
        reinicios.getOrPut(codigoDestino) { traductores.remove(codigoDestino)?.close()
            descargas.remove(codigoDestino)
            val modelo = TranslateRemoteModel.Builder(idiomaDestino).build()
            gestorModelos.deleteDownloadedModel(modelo) }
    /** Libera los traductores y limpia las cachés. */
    fun cerrar() { traductores.values.forEach { traductor ->
            traductor.close() }
        traductores.clear()
        descargas.clear()
        reinicios.clear()
        cacheTraducciones.clear() } }