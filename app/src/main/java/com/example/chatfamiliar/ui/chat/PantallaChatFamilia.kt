package com.example.chatfamiliar.ui.chat

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatfamiliar.R
import com.example.chatfamiliar.data.llamada.DestinoLlamada
import com.example.chatfamiliar.model.ConversacionResumen
import com.example.chatfamiliar.ui.language.IdiomaViewModel
import com.example.chatfamiliar.ui.language.recordarIdiomaEfectivo
import com.example.chatfamiliar.ui.language.recordarTextosApp
import com.example.chatfamiliar.ui.llamada.AvisoLlamadaEnCurso
import com.example.chatfamiliar.ui.llamada.BotonVideollamada
import com.example.chatfamiliar.ui.llamada.LlamadaViewModel

@Composable
fun PantallaChatFamilia(familiaId: String, nombreFamilia: String,
                        idiomaViewModel: IdiomaViewModel, llamadaViewModel: LlamadaViewModel,
                        alVolver: () -> Unit,
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
    // Mientras este chat está abierto, revisamos si hay una
    // videollamada familiar en curso para ofrecer "Unirse".
    DisposableEffect(familiaId, llamadaViewModel) {
        llamadaViewModel.vigilarLlamadaFamiliar(familiaId)
        onDispose { llamadaViewModel.dejarDeVigilarLlamadaFamiliar() }
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
        alReintentarMensajes = chatViewModel::reintentarMensajes,
        accionesCabecera = {
            // Llama a todos los miembros de la familia
            BotonVideollamada(textos = textos,
                habilitado = chatViewModel.puedeEnviar,
                iniciando = llamadaViewModel.iniciando,
                alLlamar = {
                    llamadaViewModel.llamar(DestinoLlamada.Familia(
                        familiaId = familiaId,
                        nombreFamilia = nombreFamilia))
                },
                modifier = Modifier.padding(end = 6.dp),
                // Si solo estás tú en la familia, se ve atenuado y explica por qué.
                atenuado = llamadaViewModel.familiaSinOtrosIntegrantes,
                alPulsarAtenuado = llamadaViewModel::avisarFamiliaSinIntegrantes)
        },
        avisoSuperior = {
            val llamadaEnCurso = llamadaViewModel.llamadaFamiliarEnCurso
            if (llamadaEnCurso != null && chatViewModel.puedeEnviar) {
                AvisoLlamadaEnCurso(textos = textos,
                    conectando = llamadaViewModel.conectando,
                    alUnirse = { llamadaViewModel.unirse(llamadaEnCurso) })
            }
        }
    )
}