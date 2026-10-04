package com.example.chatfamiliar.ui.chat

import android.text.format.DateFormat
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.chatfamiliar.model.Mensaje
import com.example.chatfamiliar.R
import com.example.chatfamiliar.ui.chat.components.BarraEscrituraMensaje
import com.example.chatfamiliar.ui.chat.components.BurbujaMensaje
import com.example.chatfamiliar.ui.comun.AvatarIdentidad
import com.example.chatfamiliar.ui.language.ControlIdiomaCompacto
import com.example.chatfamiliar.ui.language.IdiomaViewModel
import com.example.chatfamiliar.ui.language.recordarIdiomaEfectivo
import com.example.chatfamiliar.ui.language.recordarTextosApp
import com.example.chatfamiliar.ui.theme.funcional
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun ContenidoChat(claveConversacion: String, titulo: String,
                  uidActual: String, mensajes: List<Mensaje>,
                  mostrarRemitentes: Boolean, borrador: String,
                  cargandoMensajes: Boolean, cargandoAnteriores: Boolean,
                  puedeCargarAnteriores: Boolean, historialCompleto: Boolean,
                  puedeEnviar: Boolean, enviando: Boolean,
                  @StringRes errorMensajesRecurso: Int?,
                  @StringRes errorHistorialRecurso: Int?,
                  @StringRes errorEnvioRecurso: Int?,
                  lecturaViewModel: LecturaChatViewModel,
                  idiomaViewModel: IdiomaViewModel,
                  alVolver: () -> Unit, alCambiarBorrador: (String) -> Unit,
                  alEnviar: () -> Unit, alCargarAnteriores: () -> Unit,
                  alReintentarMensajes: () -> Unit, modifier: Modifier = Modifier,
                  @StringRes motivoBloqueoRecurso: Int? = null,
                  semillaAvatar: String = claveConversacion.substringAfter(':'),
                  accionesCabecera: @Composable () -> Unit = {},
                  avisoSuperior: @Composable () -> Unit = {}) {
    val idioma = recordarIdiomaEfectivo(
        idiomaViewModel.idiomaSeleccionado)
    val textos = recordarTextosApp(
        idiomaEfectivo = idioma,
        traducir = idiomaViewModel::traducir)
    val contexto = LocalContext.current
    val ciclo = LocalView.current
        .findViewTreeLifecycleOwner()
        ?.lifecycle
    val lista = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var posicionada by remember(claveConversacion) {
        mutableStateOf(false) }
    var primerPendienteId by remember(claveConversacion) {
        mutableStateOf<String?>(null) }
    val locale = remember(idioma) {
        Locale.forLanguageTag(idioma) }
    val formato24Horas = DateFormat.is24HourFormat(contexto)
    val formatoHora = remember(locale, formato24Horas) {
        SimpleDateFormat(DateFormat.getBestDateTimePattern(locale,
            if (formato24Horas) "Hm" else "hm"),
            locale) }
    val formatoFecha = remember(locale) {
        java.text.DateFormat.getDateInstance(
            java.text.DateFormat.MEDIUM,
            locale)
    }
    val mensajesActuales by rememberUpdatedState(mensajes)
    LaunchedEffect(
        claveConversacion, mensajes, lecturaViewModel.lectura,
        lecturaViewModel.necesitaHistorialAnterior, lecturaViewModel.errorLecturaRecurso,
        historialCompleto, errorHistorialRecurso) {
        if (posicionada || mensajes.isEmpty()) { return@LaunchedEffect }
        val lectura = lecturaViewModel.lectura
        if (lectura == null && lecturaViewModel.errorLecturaRecurso == null) {
            return@LaunchedEffect
        }
        if (lecturaViewModel.necesitaHistorialAnterior && !historialCompleto &&
            errorHistorialRecurso == null) {
            return@LaunchedEffect
        }
        val indicePendiente = if (lectura != null) {
            mensajes.indexOfFirst { mensaje ->
                val fecha = mensaje.fechaEnvio
                val cursor = lectura.fechaUltimoMensajeLeido
                val posterior = when {
                    fecha == null -> false
                    cursor == null -> true
                    fecha > cursor -> true
                    fecha < cursor -> false
                    else -> mensaje.id > lectura.ultimoMensajeLeidoId }
                mensaje.remitenteId != uidActual && posterior }
        } else { -1 }
        primerPendienteId = mensajes.getOrNull(indicePendiente)?.id
        val destino = if (indicePendiente >= 0) { indicePendiente
        } else { mensajes.lastIndex }
        lista.scrollToItem(destino + 1)
        posicionada = true
    }
    LaunchedEffect(claveConversacion, posicionada, ciclo) {
        if (!posicionada || ciclo == null) { return@LaunchedEffect }
        val coberturas = mutableMapOf<String, CoberturaMensaje>()
        ciclo.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            snapshotFlow {
                if (lista.isScrollInProgress) { emptyList() } else {
                    val layout = lista.layoutInfo
                    layout.visibleItemsInfo.mapNotNull { item ->
                        val clave = item.key as? String
                        val id = clave?.takeIf { it.startsWith("mensaje:") }
                            ?.removePrefix("mensaje:")
                        if (id == null || item.size <= 0) { null
                        } else {
                            val inicio = maxOf(0, layout.viewportStartOffset - item.offset)
                            val fin = minOf(item.size, layout.viewportEndOffset - item.offset)
                            if (fin > inicio) {
                                TramoMensajeVisible(id = id, alto = item.size,
                                    inicio = inicio, fin = fin)
                            } else { null } }
                    }
                }
            }.collectLatest { tramos ->
                if (tramos.isEmpty()) return@collectLatest
                delay(350)
                val idsConfirmados = mensajesActuales.filter { it.fechaEnvio != null }
                    .mapTo(mutableSetOf()) { it.id }
                val vistos = mutableSetOf<String>()
                tramos.forEach { tramo ->
                    if (tramo.id in idsConfirmados) {
                        val anterior = coberturas[tramo.id]
                        val cobertura = if (anterior == null || anterior.alto != tramo.alto) {
                            CoberturaMensaje(tramo.alto).also {
                                coberturas[tramo.id] = it }
                        } else { anterior }
                        if (cobertura.agregar(tramo.inicio, tramo.fin)) {
                            vistos.add(tramo.id) } } }
                if (vistos.isNotEmpty()) {
                    lecturaViewModel.registrarMensajesVisibles(vistos) }
            }
        }
    }
    // Último elemento visible de la lista, para saber si el usuario
    // estaba viendo el final de la conversación.
    var claveVisibleAlFinal by remember(claveConversacion) {
        mutableStateOf<String?>(null) }
    LaunchedEffect(lista) {
        snapshotFlow {
            lista.layoutInfo.visibleItemsInfo.lastOrNull()?.key as? String
        }.collect { claveVisibleAlFinal = it }
    }
    // Al llegar un mensaje nuevo: si es propio, o si el usuario ya estaba
    // al final, la lista baja hasta él. Si estaba leyendo mensajes
    // anteriores, se respeta su posición y queda el botón para bajar.
    var ultimoIdAnterior by remember(claveConversacion) {
        mutableStateOf<String?>(null) }
    val ultimoMensaje = mensajes.lastOrNull()
    LaunchedEffect(claveConversacion, ultimoMensaje?.id, posicionada) {
        val anterior = ultimoIdAnterior
        ultimoIdAnterior = ultimoMensaje?.id
        if (!posicionada || ultimoMensaje == null || anterior == null ||
            anterior == ultimoMensaje.id) { return@LaunchedEffect }
        val esPropio = ultimoMensaje.remitenteId == uidActual
        val estabaAlFinal = claveVisibleAlFinal == "mensaje:$anterior"
        if (esPropio || estabaAlFinal) {
            lista.animateScrollToItem(mensajes.size) }
    }
    // Al abrir el teclado, si se estaba viendo el final, se mantiene
    // visible el último mensaje en lugar de quedar oculto detrás.
    val densidad = LocalDensity.current
    val tecladoAbierto = WindowInsets.ime.getBottom(densidad) > 0
    LaunchedEffect(tecladoAbierto) {
        val ultimo = mensajesActuales.lastOrNull() ?: return@LaunchedEffect
        if (tecladoAbierto && posicionada &&
            claveVisibleAlFinal == "mensaje:${ultimo.id}") {
            delay(250)
            lista.animateScrollToItem(mensajesActuales.size) }
    }
    Column(modifier = modifier.fillMaxSize().safeDrawingPadding()
        .imePadding()) {
        CabeceraChat(titulo = titulo,
            subtitulo = textos.texto(if (mostrarRemitentes) {
                R.string.chat_item_family } else { R.string.chat_item_private }),
            semillaAvatar = semillaAvatar,
            esGrupo = mostrarRemitentes,
            idioma = idioma,
            descripcionVolver = textos.texto(R.string.new_message_back),
            alVolver = alVolver) {
            // Acciones extra de cada chat (por ejemplo, videollamada)
            accionesCabecera()
            ControlIdiomaCompacto(idiomaSeleccionado = idiomaViewModel.idiomaSeleccionado,
                idiomaEfectivo = idioma,
                traducir = idiomaViewModel::traducir,
                alSeleccionarIdioma = idiomaViewModel::seleccionarIdioma) }
        // Aviso opcional debajo de la cabecera (por ejemplo, llamada en curso)
        avisoSuperior()
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            LazyColumn(state = lista, modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 12.dp)) {
                item(key = "controles") {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (cargandoMensajes || cargandoAnteriores) {
                            CircularProgressIndicator()
                            Text(text = textos.texto(R.string.chat_screen_loading),
                                style = MaterialTheme.typography.bodySmall) }
                        if (errorMensajesRecurso != null) {
                            Text(text = textos.texto(errorMensajesRecurso),
                                color = MaterialTheme.colorScheme.error)
                            TextButton(onClick = alReintentarMensajes) {
                                Text(textos.texto(R.string.chat_item_retry)) } }
                        if (errorHistorialRecurso != null) {
                            Text(text = textos.texto(errorHistorialRecurso),
                                color = MaterialTheme.colorScheme.error) }
                        if (puedeCargarAnteriores) {
                            TextButton(onClick = alCargarAnteriores) {
                                Text(textos.texto(R.string.chat_screen_load_previous)) } } } }
                if (mensajes.isEmpty() && !cargandoMensajes && errorMensajesRecurso == null) {
                    item(key = "vacio") {
                        EstadoChatVacio(titulo = titulo,
                            descripcion = textos.texto(R.string.chat_screen_empty),
                            semillaAvatar = semillaAvatar,
                            esGrupo = mostrarRemitentes,
                            idioma = idioma,
                            modifier = Modifier.fillParentMaxSize()) } }
                itemsIndexed(items = mensajes,
                    key = { _, mensaje -> "mensaje:${mensaje.id}" }) { indice, mensaje ->
                    val fecha = mensaje.fechaEnvio?.toDate()
                    val fechaAnterior = mensajes.getOrNull(indice - 1)
                        ?.fechaEnvio?.toDate()
                    val textoFecha = fecha?.let(formatoFecha::format)
                    val textoFechaAnterior =
                        fechaAnterior?.let(formatoFecha::format)
                    val iniciaDia = textoFecha != null && textoFecha != textoFechaAnterior
                    val anterior = mensajes.getOrNull(indice - 1)
                    val siguiente = mensajes.getOrNull(indice + 1)
                    val textoFechaSiguiente = siguiente?.fechaEnvio?.toDate()
                        ?.let(formatoFecha::format)
                    val primeroDelGrupo = anterior == null ||
                            anterior.remitenteId != mensaje.remitenteId ||
                            iniciaDia || mensaje.id == primerPendienteId
                    val ultimoDelGrupo = siguiente == null ||
                            siguiente.remitenteId != mensaje.remitenteId ||
                            (textoFechaSiguiente != null && textoFechaSiguiente != textoFecha) ||
                            siguiente.id == primerPendienteId
                    Column {
                        if (iniciaDia) {
                            SeparadorFecha(texto = textoFecha.orEmpty(),
                                modifier = Modifier.align(Alignment.CenterHorizontally)) }
                        if (mensaje.id == primerPendienteId) {
                            SeparadorPendientes(
                                texto = textos.texto(R.string.chat_screen_unread_divider)) }
                        BurbujaMensaje(mensaje = mensaje,
                            esPropio = mensaje.remitenteId == uidActual,
                            mostrarRemitente = mostrarRemitentes,
                            hora = fecha?.let(formatoHora::format).orEmpty(),
                            idiomaEfectivo = idioma,
                            traducir = idiomaViewModel::traducir,
                            primeroDelGrupo = primeroDelGrupo,
                            ultimoDelGrupo = ultimoDelGrupo) } } }
            if (mensajes.isNotEmpty() && lista.canScrollForward) {
                SmallFloatingActionButton(onClick = { scope.launch {
                    lista.scrollToItem(mensajes.size) } },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.funcional.encabezado) {
                    Icon(imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = textos.texto(R.string.chat_screen_latest)) } }
        }
        val errorLectura = lecturaViewModel.errorLecturaRecurso
            ?: lecturaViewModel.errorGuardadoRecurso
        if (errorLectura != null) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(text = textos.texto(R.string.chat_screen_read_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error)
                Text(text = textos.texto(errorLectura),
                    style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = lecturaViewModel::reintentar) {
                    Text(textos.texto(R.string.chat_item_retry)) } } }
        BarraEscrituraMensaje(
            texto = borrador, puedeEnviar = puedeEnviar,
            enviando = enviando, idiomaEfectivo = idioma,
            traducir = idiomaViewModel::traducir, alCambiarTexto = alCambiarBorrador,
            alEnviar = alEnviar, errorRecurso = errorEnvioRecurso,
            motivoBloqueoRecurso = motivoBloqueoRecurso)
    }
}

@Composable
private fun CabeceraChat(titulo: String, subtitulo: String,
                         semillaAvatar: String, esGrupo: Boolean, idioma: String,
                         descripcionVolver: String, alVolver: () -> Unit,
                         acciones: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()
            .padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = alVolver) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = descripcionVolver) }
            AvatarIdentidad(semilla = semillaAvatar, nombre = titulo,
                icono = if (esGrupo) Icons.Filled.Groups else null,
                esGrupo = esGrupo, tamano = 42.dp, idioma = idioma)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = titulo, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.funcional.encabezado,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = subtitulo, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1) }
            Spacer(modifier = Modifier.width(8.dp))
            acciones() }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun EstadoChatVacio(titulo: String, descripcion: String,
                            semillaAvatar: String, esGrupo: Boolean, idioma: String,
                            modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        AvatarIdentidad(semilla = semillaAvatar, nombre = titulo,
            icono = if (esGrupo) Icons.Filled.Groups else null,
            esGrupo = esGrupo, tamano = 72.dp, idioma = idioma)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = titulo, style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
            maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = descripcion, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
    }
}

@Composable
private fun SeparadorFecha(texto: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.padding(top = 16.dp, bottom = 4.dp),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.funcional.etiquetaNeutra,
        contentColor = MaterialTheme.funcional.sobreEtiquetaNeutra) {
        Text(text = texto,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium) }
}

@Composable
private fun SeparadorPendientes(texto: String) {
    val acento = MaterialTheme.funcional.acento
    Row(modifier = Modifier.fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(modifier = Modifier.weight(1f),
            color = acento.copy(alpha = 0.4f))
        Text(text = texto,
            modifier = Modifier.padding(horizontal = 12.dp),
            style = MaterialTheme.typography.labelLarge,
            color = acento)
        HorizontalDivider(modifier = Modifier.weight(1f),
            color = acento.copy(alpha = 0.4f))
    }
}

private data class TramoMensajeVisible(val id: String, val alto: Int,
                                       val inicio: Int, val fin: Int)
private class CoberturaMensaje(val alto: Int) {
    private val tramos = mutableListOf<Pair<Int, Int>>()
    fun agregar(inicio: Int, fin: Int): Boolean {
        tramos.add(inicio to fin)
        val ordenados = tramos.sortedBy { it.first }
        val unidos = mutableListOf<Pair<Int, Int>>()
        ordenados.forEach { tramo ->
            val ultimo = unidos.lastOrNull()
            if (ultimo == null || tramo.first > ultimo.second) { unidos.add(tramo)
            } else { unidos[unidos.lastIndex] =
                ultimo.first to maxOf(ultimo.second, tramo.second) } }
        tramos.clear()
        tramos.addAll(unidos)
        return unidos.size == 1 && unidos.first().first == 0 && unidos.first().second >= alto
    }
}