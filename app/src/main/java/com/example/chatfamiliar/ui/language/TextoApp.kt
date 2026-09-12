package com.example.chatfamiliar.ui.language

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.example.chatfamiliar.data.language.IdiomaRepository
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat

typealias TraducirTexto = (
    texto: String,
    codigoDestino: String,
    alCompletar: (Result<String>) -> Unit
) -> Unit


class TextosApp internal constructor(
    private val idiomaEfectivo: String,
    private val traducir: TraducirTexto
) {
    @Composable
    fun texto(@StringRes recurso: Int): String {
        return textoApp(recurso = recurso,
            idiomaEfectivo = idiomaEfectivo,
            traducir = traducir
        )
    }
}

@Composable
fun recordarTextosApp(
    idiomaEfectivo: String,
    traducir: TraducirTexto
): TextosApp {
    return remember(idiomaEfectivo, traducir
    ) {
        TextosApp(idiomaEfectivo = idiomaEfectivo, traducir = traducir)
    }
}

@Composable
fun textoApp(
    @StringRes recurso: Int,
    idiomaEfectivo: String,
    traducir: TraducirTexto
): String {
    val textoLocal = stringResource(recurso)
    val esIdiomaLocal = idiomaEfectivo == IdiomaRepository.ESPANOL
            || idiomaEfectivo == IdiomaRepository.INGLES
    if (esIdiomaLocal) { return textoLocal }
    var textoTraducido by remember(recurso,
        idiomaEfectivo, textoLocal
    ) { mutableStateOf(textoLocal) }

    LaunchedEffect(recurso,
        idiomaEfectivo, textoLocal
    ) {
        traducir(textoLocal,
            idiomaEfectivo
        ) { resultado ->
            resultado
                .onSuccess { traduccion ->
                    textoTraducido = traduccion
                }
                .onFailure {
                    textoTraducido = textoLocal
                }
        }
    }
    return textoTraducido
}


@Composable
fun recordarIdiomaEfectivo(
    idiomaSeleccionado: String?
): String {

    val configuracion =
        LocalConfiguration.current

    val idiomaSistema =
        ConfigurationCompat
            .getLocales(configuracion)[0]
            ?.language
            ?: IdiomaRepository.ESPANOL

    return idiomaSeleccionado
        ?: idiomaSistema
}