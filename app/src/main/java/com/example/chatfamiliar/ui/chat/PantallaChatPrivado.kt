package com.example.chatfamiliar.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatfamiliar.R
import com.example.chatfamiliar.model.ConversacionResumen
import com.example.chatfamiliar.ui.language.IdiomaViewModel
import com.example.chatfamiliar.ui.language.recordarIdiomaEfectivo
import com.example.chatfamiliar.ui.language.recordarTextosApp

@Composable
fun PantallaChatPrivado(conversacionId: String,
    idiomaViewModel: IdiomaViewModel, alVolver: () -> Unit,
    chatViewModel: ChatPrivadoViewModel = viewModel(),
    lecturaViewModel: LecturaChatViewModel = viewModel()) {
    val idioma = recordarIdiomaEfectivo(
        idiomaViewModel.idiomaSeleccionado)
    val textos = recordarTextosApp(idiomaEfectivo = idioma,
        traducir = idiomaViewModel::traducir)
    val ciclo = LocalView.current.findViewTreeLifecycleOwner()
        ?.lifecycle
    DisposableEffect(conversacionId, chatViewModel,
        lecturaViewModel) {
        chatViewModel.iniciar(conversacionId)
        lecturaViewModel.iniciar(tipo = ConversacionResumen.TIPO_PRIVADO,
            id = conversacionId)
        onDispose { chatViewModel.detener()
            lecturaViewModel.detener() } }
    // Al regresar a la aplicación, revisamos nuevamente
    // si los participantes comparten una familia.
    DisposableEffect(ciclo, chatViewModel) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) { chatViewModel.comprobarPermiso() } }
        ciclo?.addObserver(observador)
        onDispose { ciclo?.removeObserver(observador) } }
    LaunchedEffect(chatViewModel.mensajes, chatViewModel.historialCompleto) {
        lecturaViewModel.actualizarHistorial(mensajes = chatViewModel.mensajes,
            completo = chatViewModel.historialCompleto) }
    LaunchedEffect(lecturaViewModel.necesitaHistorialAnterior,
        chatViewModel.mensajes.firstOrNull()?.id,
        chatViewModel.cargandoAnteriores,
        chatViewModel.historialCompleto,
        chatViewModel.errorHistorialRecurso) {
        if (lecturaViewModel.necesitaHistorialAnterior &&
            chatViewModel.puedeCargarAnteriores &&
            chatViewModel.errorHistorialRecurso == null) {
            chatViewModel.cargarAnteriores() } }
    val motivoBloqueo = when {
        chatViewModel.puedeEnviar -> null
        chatViewModel.errorConversacionRecurso != null -> chatViewModel.errorConversacionRecurso
        chatViewModel.errorPermisoRecurso != null -> chatViewModel.errorPermisoRecurso
        chatViewModel.verificandoPermiso || !chatViewModel.permisoConsultado ->
            R.string.chat_private_checking_permission
        else -> R.string.chat_private_no_shared_family
    }
    val mostrarActualizarDisponibilidad =
        !chatViewModel.puedeEnviar && !chatViewModel.enviando &&
                !chatViewModel.verificandoPermiso && (chatViewModel.permisoConsultado ||
                chatViewModel.errorPermisoRecurso != null ||
                chatViewModel.errorConversacionRecurso != null)
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()
        .imePadding()) {
        ContenidoChat(
            claveConversacion = "privado:$conversacionId",
            titulo = chatViewModel.nombreContacto.ifBlank { textos.texto(
                R.string.chat_item_unknown_user) },
            uidActual = chatViewModel.uidActual, mensajes = chatViewModel.mensajes,
            mostrarRemitentes = false, borrador = chatViewModel.borrador,
            cargandoMensajes = chatViewModel.cargandoMensajes,
            cargandoAnteriores = chatViewModel.cargandoAnteriores,
            puedeCargarAnteriores = chatViewModel.puedeCargarAnteriores,
            historialCompleto = chatViewModel.historialCompleto,
            puedeEnviar = chatViewModel.puedeEnviar,
            enviando = chatViewModel.enviando,
            errorMensajesRecurso = chatViewModel.errorConversacionRecurso
                ?: chatViewModel.errorMensajesRecurso
                ?: chatViewModel.errorNombreRecurso,
            errorHistorialRecurso = chatViewModel.errorHistorialRecurso,
            errorEnvioRecurso = chatViewModel.errorEnvioRecurso,
            lecturaViewModel = lecturaViewModel,
            idiomaViewModel = idiomaViewModel,
            alVolver = alVolver,
            alCambiarBorrador = chatViewModel::actualizarBorrador,
            alEnviar = chatViewModel::enviarMensaje,
            alCargarAnteriores = chatViewModel::cargarAnteriores,
            alReintentarMensajes = chatViewModel::reintentar,
            modifier = Modifier.weight(1f),
            motivoBloqueoRecurso = motivoBloqueo,
            semillaAvatar = chatViewModel.otroUsuarioId.ifBlank { conversacionId })
        if (mostrarActualizarDisponibilidad) {
            TextButton(onClick = chatViewModel::reintentar, modifier = Modifier.fillMaxWidth()) {
                Text(text = textos.texto(R.string.chat_private_refresh_permission)) }
        }
    }
}