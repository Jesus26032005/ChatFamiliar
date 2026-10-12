package com.example.chatfamiliar.ui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.chatfamiliar.model.ConversacionResumen
import java.text.Normalizer
import java.util.Locale

/** Une los chats familiares y privados y aplica la búsqueda y el filtro elegido. */
class InicioConversacionesViewModel : ViewModel() {
    var conversaciones by mutableStateOf<List<EstadoConversacionInicio>>(emptyList())
        private set
    var busqueda by mutableStateOf("")
        private set
    var filtroSeleccionado by mutableStateOf(FiltroConversaciones.TODOS)
        private set

    val conversacionesFiltradas: List<EstadoConversacionInicio>
        get() { val consulta = normalizar(busqueda)
            return conversaciones.filter { estado ->
                coincideConFiltro(estado) &&
                        (consulta.isEmpty() || normalizar(estado.resumen.titulo)
                            .contains(consulta)) } }

    val hayConteosPendientes: Boolean
        get() = conversaciones.any { estado -> !estado.conteoConfirmado ||
                estado.actualizandoConteo || estado.errorConteoRecurso != null }
    /** Combina ambas listas, quita duplicados y ordena por el mensaje más reciente. */
    fun actualizarConversaciones(familiares: List<EstadoConversacionInicio>,
        privadas: List<EstadoConversacionInicio>) { conversaciones = (familiares + privadas)
            .distinctBy { it.clave }
            .sortedWith(compareByDescending<EstadoConversacionInicio> { it.resumen.fechaUltimoMensaje
                }.thenBy { normalizar(it.resumen.titulo) }.thenBy { it.clave }) }
    fun actualizarBusqueda(texto: String) { busqueda = texto }
    fun seleccionarFiltro(filtro: FiltroConversaciones) { filtroSeleccionado = filtro }
    fun limpiarBusqueda() { busqueda = "" }
    fun limpiar() { conversaciones = emptyList()
        busqueda = ""
        filtroSeleccionado = FiltroConversaciones.TODOS }

    /** true si la conversación pasa el filtro seleccionado. */
    private fun coincideConFiltro(estado: EstadoConversacionInicio): Boolean { return when (filtroSeleccionado) {
            FiltroConversaciones.TODOS -> true
            FiltroConversaciones.NO_LEIDOS ->
                (estado.mensajesNoLeidosVisibles ?: 0) > 0
            FiltroConversaciones.FAMILIAS ->
                estado.resumen.tipo == ConversacionResumen.TIPO_FAMILIA
            FiltroConversaciones.PRIVADOS ->
                estado.resumen.tipo == ConversacionResumen.TIPO_PRIVADO } }
    /** Quita acentos y mayúsculas para que la búsqueda no dependa de ellos. */
    private fun normalizar(texto: String): String { return Normalizer
            .normalize(texto, Normalizer.Form.NFD)
            .replace(MARCAS_DIACRITICAS, "")
            .lowercase(Locale.ROOT)
            .trim() }
    companion object {
        private val MARCAS_DIACRITICAS = Regex("\\p{M}+") } }