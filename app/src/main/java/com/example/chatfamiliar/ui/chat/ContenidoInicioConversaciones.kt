package com.example.chatfamiliar.ui.chat

import android.text.format.DateFormat
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.R
import com.example.chatfamiliar.model.ConversacionResumen
import com.example.chatfamiliar.ui.chat.components.BuscadorFiltrosConversaciones
import com.example.chatfamiliar.ui.chat.components.EstadoCargaConversaciones
import com.example.chatfamiliar.ui.chat.components.EstadoErrorConversaciones
import com.example.chatfamiliar.ui.chat.components.EstadoVacioConversaciones
import com.example.chatfamiliar.ui.chat.components.ItemConversacion
import com.example.chatfamiliar.ui.language.ControlIdiomaCompacto
import com.example.chatfamiliar.ui.language.TraducirTexto
import com.example.chatfamiliar.ui.language.recordarTextosApp
import com.example.chatfamiliar.util.formatearFechaConversacion
import com.example.chatfamiliar.ui.theme.funcional
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun ContenidoInicioConversaciones(
    uidActual: String, conversaciones: List<EstadoConversacionInicio>,
    conversacionesFiltradas: List<EstadoConversacionInicio>, busqueda: String,
    filtroSeleccionado: FiltroConversaciones, hayConteosPendientes: Boolean,
    hayFamilias: Boolean, cargandoFamilias: Boolean,
    cargandoPrivados: Boolean, @StringRes errorFamiliasRecurso: Int?,
    @StringRes errorPrivadosRecurso: Int?, idiomaSeleccionado: String?,
    idiomaEfectivo: String, traducir: TraducirTexto,
    alSeleccionarIdioma: (String?) -> Unit, alCambiarBusqueda: (String) -> Unit,
    alLimpiarBusqueda: () -> Unit, alSeleccionarFiltro: (FiltroConversaciones) -> Unit,
    alAbrirConversacion: (ConversacionResumen) -> Unit,
    alReintentarConversacion: (ConversacionResumen) -> Unit,
    alReintentarFamilias: () -> Unit, alReintentarPrivados: () -> Unit,
    alVerFamilias: () -> Unit, alNuevoMensaje: () -> Unit, modifier: Modifier = Modifier
) {
    val textos = recordarTextosApp(
        idiomaEfectivo = idiomaEfectivo,
        traducir = traducir)
    val funcional = MaterialTheme.funcional
    val contexto = LocalContext.current
    val estadoLista = rememberLazyListState()

    var ahoraMillis by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            ahoraMillis = System.currentTimeMillis()
            delay(60_000L)
        }
    }

    LaunchedEffect(busqueda, filtroSeleccionado) {
        estadoLista.scrollToItem(0)
    }

    // Si estás arriba y llega un chat nuevo (o cambia cuál es el más
    // reciente), la lista se queda arriba para que lo veas. Si bajaste
    // tú mismo, se respeta tu posición.
    val primeraClave = conversacionesFiltradas.firstOrNull()?.clave
    LaunchedEffect(primeraClave) {
        if (primeraClave != null && estadoLista.firstVisibleItemIndex <= 1) {
            estadoLista.scrollToItem(0)
        }
    }

    val formato24Horas = DateFormat.is24HourFormat(contexto)
    val textoAyer = textos.texto(R.string.chat_date_yesterday)

    val incluyeFamilias =
        filtroSeleccionado != FiltroConversaciones.PRIVADOS
    val incluyePrivados =
        filtroSeleccionado != FiltroConversaciones.FAMILIAS

    val cargandoFuentes =
        (incluyeFamilias && cargandoFamilias) ||
                (incluyePrivados && cargandoPrivados)

    val hayErrorFuentes =
        (incluyeFamilias && errorFamiliasRecurso != null) ||
                (incluyePrivados && errorPrivadosRecurso != null)

    val conversacionesDelTipo = conversaciones.filter { estado ->
        when (filtroSeleccionado) {
            FiltroConversaciones.FAMILIAS ->
                estado.resumen.tipo == ConversacionResumen.TIPO_FAMILIA

            FiltroConversaciones.PRIVADOS ->
                estado.resumen.tipo == ConversacionResumen.TIPO_PRIVADO

            else -> true
        }
    }

    val nombresPendientes = conversacionesDelTipo.any {
        it.cargandoNombre || it.errorNombreRecurso != null
    }

    val hayErroresDeConteo = conversaciones.any {
        it.errorConteoRecurso != null
    }

    val hayErroresDeNombre = conversacionesDelTipo.any {
        it.errorNombreRecurso != null
    }

    val reintentarConteos: () -> Unit = {
        conversaciones
            .filter { it.errorConteoRecurso != null }
            .forEach { alReintentarConversacion(it.resumen) }
    }

    val reintentarNombres: () -> Unit = {
        conversacionesDelTipo
            .filter { it.errorNombreRecurso != null }
            .forEach { alReintentarConversacion(it.resumen) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = textos.texto(R.string.chat_home_title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = funcional.encabezado
            )

            ControlIdiomaCompacto(
                idiomaSeleccionado = idiomaSeleccionado,
                idiomaEfectivo = idiomaEfectivo,
                traducir = traducir,
                alSeleccionarIdioma = alSeleccionarIdioma
            )
        }

        TarjetaBienvenida(
            titulo = textos.texto(R.string.chat_home_intro),
            descripcion = textos.texto(R.string.chat_home_intro_description)
        )

        BuscadorFiltrosConversaciones(
            busqueda = busqueda,
            filtroSeleccionado = filtroSeleccionado,
            hayConteosPendientes = hayConteosPendientes,
            idiomaEfectivo = idiomaEfectivo,
            traducir = traducir,
            alCambiarBusqueda = alCambiarBusqueda,
            alLimpiarBusqueda = alLimpiarBusqueda,
            alSeleccionarFiltro = alSeleccionarFiltro
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = estadoLista,
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = 100.dp
                )
            ) {
                if (incluyeFamilias && errorFamiliasRecurso != null) {
                    item(key = "error_familias") {
                        EstadoErrorConversaciones(
                            titulo = textos.texto(
                                R.string.chat_home_family_error
                            ),
                            mensaje = textos.texto(errorFamiliasRecurso),
                            textoReintentar = textos.texto(
                                R.string.chat_item_retry
                            ),
                            alReintentar = alReintentarFamilias
                        )
                    }
                }

                if (incluyePrivados && errorPrivadosRecurso != null) {
                    item(key = "error_privados") {
                        EstadoErrorConversaciones(
                            titulo = textos.texto(
                                R.string.chat_home_private_error
                            ),
                            mensaje = textos.texto(errorPrivadosRecurso),
                            textoReintentar = textos.texto(
                                R.string.chat_item_retry
                            ),
                            alReintentar = alReintentarPrivados
                        )
                    }
                }

                // El indicador de carga va ARRIBA de la lista. Si quedaba abajo,
                // la lista se "anclaba" a él y al llegar los chats se veía
                // el final en lugar del primero.
                if (cargandoFuentes) {
                    item(key = "carga_fuentes") {
                        EstadoCargaConversaciones(
                            titulo = textos.texto(
                                R.string.chat_list_loading_title
                            ),
                            descripcion = textos.texto(
                                R.string.chat_list_loading_description
                            )
                        )
                    }
                }

                items(
                    items = conversacionesFiltradas,
                    key = { it.clave }
                ) { estado ->
                    val fechaVisible = formatearFechaConversacion(
                        fecha = estado.resumen.fechaUltimoMensaje,
                        idiomaEfectivo = idiomaEfectivo,
                        formato24Horas = formato24Horas,
                        textoAyer = textoAyer,
                        ahoraMillis = ahoraMillis
                    )

                    ItemConversacion(
                        estado = estado,
                        uidActual = uidActual,
                        textoFecha = fechaVisible,
                        idiomaEfectivo = idiomaEfectivo,
                        traducir = traducir,
                        alAbrir = {
                            alAbrirConversacion(estado.resumen)
                        },
                        alReintentar = {
                            alReintentarConversacion(estado.resumen)
                        }
                    )
                }

                if (
                    conversacionesFiltradas.isEmpty() &&
                    !cargandoFuentes &&
                    !hayErrorFuentes
                ) {
                    item(key = "estado_sin_resultados") {
                        when {
                            filtroSeleccionado ==
                                    FiltroConversaciones.NO_LEIDOS &&
                                    hayConteosPendientes -> {
                                EstadoVacioConversaciones(
                                    titulo = textos.texto(
                                        R.string.chat_home_pending_title
                                    ),
                                    descripcion = textos.texto(
                                        R.string.chat_unread_pending
                                    ),
                                    textoAccion = if (hayErroresDeConteo) {
                                        textos.texto(R.string.chat_item_retry)
                                    } else {
                                        null
                                    },
                                    alAccion = if (hayErroresDeConteo) {
                                        reintentarConteos
                                    } else {
                                        null
                                    }
                                )
                            }

                            busqueda.isNotBlank() && nombresPendientes -> {
                                EstadoVacioConversaciones(
                                    titulo = textos.texto(
                                        R.string.chat_home_search_incomplete_title
                                    ),
                                    descripcion = textos.texto(
                                        R.string.chat_home_search_incomplete_description
                                    ),
                                    textoAccion = if (hayErroresDeNombre) {
                                        textos.texto(R.string.chat_item_retry)
                                    } else {
                                        null
                                    },
                                    alAccion = if (hayErroresDeNombre) {
                                        reintentarNombres
                                    } else {
                                        null
                                    }
                                )
                            }

                            busqueda.isNotBlank() -> {
                                EstadoVacioConversaciones(
                                    titulo = textos.texto(
                                        R.string.chat_list_no_results_title
                                    ),
                                    descripcion = textos.texto(
                                        R.string.chat_list_no_results_description
                                    ),
                                    textoAccion = textos.texto(
                                        R.string.chat_search_clear
                                    ),
                                    alAccion = alLimpiarBusqueda
                                )
                            }

                            filtroSeleccionado ==
                                    FiltroConversaciones.NO_LEIDOS -> {
                                EstadoVacioConversaciones(
                                    titulo = textos.texto(
                                        R.string.chat_list_no_unread_title
                                    ),
                                    descripcion = textos.texto(
                                        R.string.chat_list_no_unread_description
                                    )
                                )
                            }

                            filtroSeleccionado ==
                                    FiltroConversaciones.FAMILIAS -> {
                                EstadoVacioConversaciones(
                                    titulo = textos.texto(
                                        R.string.chat_list_no_families_title
                                    ),
                                    descripcion = textos.texto(
                                        R.string.chat_list_no_families_description
                                    ),
                                    textoAccion = textos.texto(
                                        R.string.chat_list_view_families
                                    ),
                                    alAccion = alVerFamilias
                                )
                            }

                            filtroSeleccionado ==
                                    FiltroConversaciones.PRIVADOS -> {
                                EstadoVacioConversaciones(
                                    titulo = textos.texto(
                                        R.string.chat_list_no_private_title
                                    ),
                                    descripcion = textos.texto(
                                        R.string.chat_list_no_private_description
                                    ),
                                    textoAccion = textos.texto(
                                        if (hayFamilias) {
                                            R.string.chat_list_new_message
                                        } else {
                                            R.string.chat_list_view_families
                                        }
                                    ),
                                    alAccion = if (hayFamilias) {
                                        alNuevoMensaje
                                    } else {
                                        alVerFamilias
                                    }
                                )
                            }

                            else -> {
                                EstadoVacioConversaciones(
                                    titulo = textos.texto(
                                        R.string.chat_list_empty_title
                                    ),
                                    descripcion = textos.texto(
                                        R.string.chat_list_empty_description
                                    ),
                                    textoAccion = textos.texto(
                                        R.string.chat_list_view_families
                                    ),
                                    alAccion = alVerFamilias
                                )
                            }
                        }
                    }
                }
            }

            ExtendedFloatingActionButton(
                onClick = if (hayFamilias) {
                    alNuevoMensaje
                } else {
                    alVerFamilias
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                containerColor = funcional.accionPrincipal,
                contentColor = funcional.sobreAccionPrincipal
            ) {
                Icon(
                    imageVector = if (hayFamilias) {
                        Icons.AutoMirrored.Filled.Chat
                    } else {
                        Icons.Filled.Groups
                    },
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = textos.texto(
                        if (hayFamilias) {
                            R.string.chat_list_new_message
                        } else {
                            R.string.chat_list_view_families
                        }
                    ),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Tarjeta de bienvenida compacta: icono más chico, menos relleno y
 * descripción en una sola línea para que ocupe poco alto.
 */
@Composable
private fun TarjetaBienvenida(
    titulo: String,
    descripcion: String
) {
    val funcional = MaterialTheme.funcional

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        color = funcional.tarjeta
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = funcional.accionPrincipal,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    tint = funcional.sobreAccionPrincipal,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}