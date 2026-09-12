package com.example.chatfamiliar.ui.language

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatfamiliar.data.language.IdiomaRepository
import com.example.chatfamiliar.data.language.TranslationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IdiomaViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val idiomaRepository = IdiomaRepository(application
        .applicationContext)
    private val translationRepository =
        TranslationRepository()
    var idiomaSeleccionado by mutableStateOf(
        idiomaRepository.obtenerIdiomaSeleccionado())
        private set
    private var traduccionesPendientes by mutableStateOf(0)
    private var preparandoIdioma by mutableStateOf(false)
    private var generacionTraduccion = 0
    private var ocultarIndicadorJob: Job? = null
    val traduciendo: Boolean
        get() = preparandoIdioma || traduccionesPendientes > 0


    fun seleccionarIdioma(
        codigoIdioma: String?
    ) {
        if (idiomaSeleccionado == codigoIdioma) { return }
        generacionTraduccion++
        traduccionesPendientes = 0
        ocultarIndicadorJob?.cancel()

        preparandoIdioma = codigoIdioma != null &&
                codigoIdioma != IdiomaRepository.ESPANOL &&
                codigoIdioma != IdiomaRepository.INGLES
        idiomaSeleccionado = codigoIdioma
        idiomaRepository.cambiarIdioma(codigoIdioma)
    }


    fun traducir(
        texto: String,
        codigoDestino: String,
        alCompletar: (Result<String>) -> Unit
    ) {
        val generacionActual = generacionTraduccion
        ocultarIndicadorJob?.cancel()
        traduccionesPendientes++
        translationRepository.traducir(
            texto = texto,
            codigoDestino = codigoDestino
        ) { resultado ->
            if (generacionActual == generacionTraduccion) {
                traduccionesPendientes = (traduccionesPendientes - 1)
                    .coerceAtLeast(0)
                if (traduccionesPendientes == 0
                ) { ocultarIndicadorJob =
                        viewModelScope.launch {
                            delay(180)
                            if (traduccionesPendientes == 0 &&
                                generacionActual == generacionTraduccion
                            ) { preparandoIdioma = false }
                        }
                }
            }
            alCompletar(resultado)
        }
    }
    override fun onCleared() {
        ocultarIndicadorJob?.cancel()
        translationRepository.cerrar()
        super.onCleared()
    }
}