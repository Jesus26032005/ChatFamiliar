package com.example.chatfamiliar.ui.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatfamiliar.R
import com.example.chatfamiliar.model.ConversacionResumen
import com.example.chatfamiliar.ui.language.IdiomaViewModel
import com.example.chatfamiliar.ui.language.recordarIdiomaEfectivo
import com.example.chatfamiliar.ui.language.recordarTextosApp

@Composable
fun PantallaChatFamilia(familiaId: String, nombreFamilia: String,
    idiomaViewModel: IdiomaViewModel, alVolver: () -> Unit,
    chatViewModel: ChatFamiliaViewModel = viewModel(),
    lecturaViewModel: LecturaChatViewModel = viewModel()) {
    val idioma = recordarIdiomaEfectivo(
        idiomaViewModel.idiomaSeleccionado)
    val textos = recordarTextosApp(
        idiomaEfectivo = idioma,
        traducir = idiomaViewModel::traducir)
    DisposableEffect(familiaId, chatViewModel, lecturaViewModel) {
        chatViewModel.iniciar(familiaId)
        lecturaViewModel.iniciar(tipo = ConversacionResumen.TIPO_FAMILIA,
            id = familiaId)
        onDispose { chatViewModel.detener()
            lecturaViewModel.detener() }
    }
    LaunchedEffect(chatViewModel.mensajes, chatViewModel.historialCompleto) {
        lecturaViewModel.actualizarHistorial(mensajes = chatViewModel.mensajes,
            completo = chatViewModel.historialCompleto)
    }
    // Cargamos hasta encontrar el marcador anterior o alcanzar
    // el comienzo del historial. Un error detiene los reintentos.
    LaunchedEffect(lecturaViewModel.necesitaHistorialAnterior,
        chatViewModel.mensajes.firstOrNull()?.id,
        chatViewModel.cargandoAnteriores,
        chatViewModel.historialCompleto,
        chatViewModel.errorHistorialRecurso) {
        if (lecturaViewModel.necesitaHistorialAnterior &&
            chatViewModel.puedeCargarAnteriores &&
            chatViewModel.errorHistorialRecurso == null) {
            chatViewModel.cargarAnteriores() }
    }

    ContenidoChat(
        claveConversacion = "familia:$familiaId",
        titulo = nombreFamilia.ifBlank { textos.texto(R.string.chat_item_unnamed_family) },
        uidActual = chatViewModel.uidActual,
        mensajes = chatViewModel.mensajes,
        mostrarRemitentes = true,
        borrador = chatViewModel.borrador,
        cargandoMensajes = chatViewModel.cargandoMensajes,
        cargandoAnteriores = chatViewModel.cargandoAnteriores,
        puedeCargarAnteriores = chatViewModel.puedeCargarAnteriores,
        historialCompleto = chatViewModel.historialCompleto,
        puedeEnviar = chatViewModel.puedeEnviar,
        enviando = chatViewModel.enviando,
        errorMensajesRecurso = chatViewModel.errorMensajesRecurso,
        errorHistorialRecurso = chatViewModel.errorHistorialRecurso,
        errorEnvioRecurso = chatViewModel.errorEnvioRecurso,
        lecturaViewModel = lecturaViewModel,
        idiomaViewModel = idiomaViewModel,
        alVolver = alVolver,
        alCambiarBorrador = chatViewModel::actualizarBorrador,
        alEnviar = chatViewModel::enviarMensaje,
        alCargarAnteriores = chatViewModel::cargarAnteriores,
        alReintentarMensajes = chatViewModel::reintentarMensajes
    )
}