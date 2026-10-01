package com.example.chatfamiliar.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatfamiliar.R
import com.example.chatfamiliar.model.ContactoPrivado
import com.example.chatfamiliar.ui.chat.components.EstadoCargaConversaciones
import com.example.chatfamiliar.ui.chat.components.EstadoErrorConversaciones
import com.example.chatfamiliar.ui.chat.components.EstadoVacioConversaciones
import com.example.chatfamiliar.ui.language.ControlIdiomaCompacto
import com.example.chatfamiliar.ui.language.IdiomaViewModel
import com.example.chatfamiliar.ui.language.recordarIdiomaEfectivo
import com.example.chatfamiliar.ui.language.recordarTextosApp
import com.example.chatfamiliar.ui.comun.AvatarIdentidad
import com.example.chatfamiliar.ui.theme.funcional

@Composable
fun PantallaNuevoMensaje(
    idiomaViewModel: IdiomaViewModel,
    alVolver: () -> Unit,
    alAbrirChatPrivado: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NuevoMensajeViewModel = viewModel()) {
    val idiomaEfectivo = recordarIdiomaEfectivo(
        idiomaViewModel.idiomaSeleccionado)
    val textos = recordarTextosApp(
        idiomaEfectivo = idiomaEfectivo,
        traducir = idiomaViewModel::traducir)
    DisposableEffect(viewModel) {
        viewModel.cargarContactos()
        onDispose { viewModel.limpiar() } }
    val conversacionPendiente = viewModel.conversacionParaAbrir
    LaunchedEffect(conversacionPendiente) {
        conversacionPendiente?.let { conversacionId ->
            viewModel.consumirNavegacion(conversacionId)
            alAbrirChatPrivado(conversacionId) } }
    val bloqueado = viewModel.abriendoConversacion ||
            conversacionPendiente != null
    val contactos = viewModel.contactosFiltrados
    val errorCarga = viewModel.errorCargaRecurso
    val errorApertura = viewModel.errorAperturaRecurso
    Column(modifier = modifier.fillMaxSize()
        .safeDrawingPadding().imePadding()) {
        Row(modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = alVolver) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = textos.texto(R.string.new_message_back)) }
            Text(text = textos.texto(R.string.chat_list_new_message),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.funcional.encabezado)
            ControlIdiomaCompacto(idiomaSeleccionado = idiomaViewModel.idiomaSeleccionado,
                idiomaEfectivo = idiomaEfectivo,
                traducir = idiomaViewModel::traducir,
                alSeleccionarIdioma = idiomaViewModel::seleccionarIdioma) }
        OutlinedTextField(
            value = viewModel.busqueda,
            onValueChange = viewModel::actualizarBusqueda,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            enabled = !bloqueado,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            label = { Text(text = textos.texto(R.string.new_message_search)) },
            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (viewModel.busqueda.isNotEmpty()) {
                    IconButton(onClick = viewModel::limpiarBusqueda, enabled = !bloqueado) {
                        Icon(imageVector = Icons.Filled.Close,
                            contentDescription = textos.texto(R.string.chat_search_clear))}
                } })
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp, top = 16.dp,
                end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item(key = "descripcion") {
                Text(text = textos.texto(R.string.new_message_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (errorCarga != null) {
                item(key = "error_carga") { EstadoErrorConversaciones(
                    titulo = textos.texto(R.string.new_message_load_error),
                    mensaje = textos.texto(errorCarga),
                    textoReintentar = textos.texto(R.string.chat_item_retry),
                    alReintentar = viewModel::cargarContactos) } }
            if (viewModel.listaIncompleta && !viewModel.cargando) {
                item(key = "lista_incompleta") {
                    EstadoVacioConversaciones(
                        titulo = textos.texto(R.string.new_message_partial_title),
                        descripcion = textos.texto(
                            R.string.new_message_partial_description),
                        textoAccion = if (!bloqueado) {
                            textos.texto(R.string.chat_item_retry)
                        } else { null },
                        alAccion = if (!bloqueado) {
                            { viewModel.cargarContactos() }
                        } else { null }) } }
            if (errorApertura != null) {
                item(key = "error_apertura") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = textos.texto(errorApertura),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium)
                        Text(text = textos.texto(
                            R.string.new_message_open_retry),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
            items(items = contactos, key = { contacto -> "contacto:${contacto.uid}" }
            ) { contacto -> TarjetaContactoPrivado(
                contacto = contacto,
                nombreVisible = contacto.nombre.ifBlank {
                    textos.texto(R.string.chat_item_unknown_user) },
                textoAccion = textos.texto(R.string.new_message_write),
                textoAbriendo = textos.texto(R.string.new_message_opening),
                abriendo = viewModel.contactoAbriendoUid == contacto.uid,
                habilitado = !bloqueado,
                alSeleccionar = { viewModel.abrirConversacion(contacto) }) }
            if (viewModel.cargando) {
                item(key = "cargando") { EstadoCargaConversaciones(
                    titulo = textos.texto(R.string.new_message_loading),
                    descripcion = textos.texto(
                        R.string.new_message_loading_description)) } }
            if (contactos.isEmpty() && viewModel.cargaConfirmada &&
                !viewModel.cargando && errorCarga == null && !viewModel.listaIncompleta) {
                item(key = "sin_resultados") {
                    if (viewModel.busqueda.isNotBlank()) {
                        EstadoVacioConversaciones(
                            titulo = textos.texto(
                                R.string.chat_list_no_results_title),
                            descripcion = textos.texto(
                                R.string.new_message_no_results),
                            textoAccion = textos.texto(
                                R.string.chat_search_clear),
                            alAccion = viewModel::limpiarBusqueda)
                    } else { EstadoVacioConversaciones(
                        titulo = textos.texto(
                            R.string.new_message_empty_title),
                        descripcion = textos.texto(
                            R.string.new_message_empty_description)) } } } }
    }
}

@Composable
private fun TarjetaContactoPrivado(contacto: ContactoPrivado,
            nombreVisible: String, textoAccion: String,
            textoAbriendo: String, abriendo: Boolean,
            habilitado: Boolean, alSeleccionar: () -> Unit) {

    Card(onClick = alSeleccionar, enabled = habilitado,
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor =
            MaterialTheme.funcional.tarjeta)) {
        Row(modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) { AvatarIdentidad(semilla = contacto.uid,
            nombre = nombreVisible,
            tamano = 48.dp)
            Column(modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = nombreVisible,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                val nombresFamilias = contacto.familiasCompartidas
                    .map { it.nombre }.filter { it.isNotBlank() }
                    .distinct().joinToString(", ")
                if (nombresFamilias.isNotEmpty()) {
                    Text(text = nombresFamilias,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Text(text = if (abriendo) textoAbriendo else textoAccion,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary) }
            if (abriendo) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp)
            }
        }
    }
}