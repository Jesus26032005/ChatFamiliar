package com.example.chatfamiliar.ui.chat

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatfamiliar.model.ConversacionResumen
import com.example.chatfamiliar.model.Familia
import com.example.chatfamiliar.ui.language.TraducirTexto

@Composable
fun PantallaInicioConversaciones(
    uidActual: String,
    familiasConfirmadas: List<Familia>?,
    cargandoFamilias: Boolean,
    @StringRes errorFamiliasRecurso: Int?,
    idiomaSeleccionado: String?,
    idiomaEfectivo: String,
    traducir: TraducirTexto,
    alSeleccionarIdioma: (String?) -> Unit,
    alAbrirConversacion: (ConversacionResumen) -> Unit,
    alReintentarFamilias: () -> Unit,
    alVerFamilias: () -> Unit,
    alNuevoMensaje: () -> Unit,
    modifier: Modifier = Modifier,
    familiasViewModel: FamiliasInicioViewModel = viewModel(),
    privadosViewModel: PrivadosInicioViewModel = viewModel(),
    inicioViewModel: InicioConversacionesViewModel = viewModel()
) {
    DisposableEffect(uidActual,
        familiasViewModel,
        privadosViewModel,
        inicioViewModel
    ) {
        inicioViewModel.limpiar()
        familiasViewModel.detener()
        privadosViewModel.detener()
        if (uidActual.isNotBlank()) { privadosViewModel.iniciar() }
        onDispose { familiasViewModel.detener()
            privadosViewModel.detener()
            inicioViewModel.limpiar()
        }
    }

    LaunchedEffect(uidActual, familiasConfirmadas, familiasViewModel) {
        if (uidActual.isNotBlank()) {
            familiasConfirmadas?.let { familias ->
                familiasViewModel.sincronizarFamilias(familias) } }
    }

    LaunchedEffect(uidActual,
        familiasViewModel.conversaciones,
        privadosViewModel.conversaciones,
        inicioViewModel
    ) {
        if (uidActual.isNotBlank()) {
            inicioViewModel.actualizarConversaciones(
                familiasViewModel.conversaciones,
                privadosViewModel.conversaciones) }
    }

    val errorFamiliasVisible =
        errorFamiliasRecurso
            ?: familiasViewModel.errorSesionRecurso
    ContenidoInicioConversaciones(
        uidActual = uidActual,
        conversaciones = inicioViewModel.conversaciones,
        conversacionesFiltradas =
            inicioViewModel.conversacionesFiltradas,
        busqueda = inicioViewModel.busqueda,
        filtroSeleccionado =
            inicioViewModel.filtroSeleccionado,
        hayConteosPendientes =
            inicioViewModel.hayConteosPendientes,
        hayFamilias = !familiasConfirmadas.isNullOrEmpty(),
        cargandoFamilias = cargandoFamilias,
        cargandoPrivados = privadosViewModel.cargando,
        errorFamiliasRecurso = errorFamiliasVisible,
        errorPrivadosRecurso =
            privadosViewModel.errorListadoRecurso,
        idiomaSeleccionado = idiomaSeleccionado,
        idiomaEfectivo = idiomaEfectivo,
        traducir = traducir,
        alSeleccionarIdioma = alSeleccionarIdioma,
        alCambiarBusqueda = inicioViewModel::actualizarBusqueda,
        alLimpiarBusqueda = inicioViewModel::limpiarBusqueda,
        alSeleccionarFiltro =
            inicioViewModel::seleccionarFiltro,
        alAbrirConversacion = alAbrirConversacion,
        alReintentarConversacion = { conversacion ->
            when (conversacion.tipo) {
                ConversacionResumen.TIPO_FAMILIA -> {
                    familiasViewModel.reintentarFamilia(
                        conversacion.id) }
                ConversacionResumen.TIPO_PRIVADO -> {
                    privadosViewModel.reintentarConversacion(
                        conversacion.id) } } },
        alReintentarFamilias = alReintentarFamilias,
        alReintentarPrivados = privadosViewModel::recargar,
        alVerFamilias = alVerFamilias,
        alNuevoMensaje = alNuevoMensaje,
        modifier = modifier
    )
}