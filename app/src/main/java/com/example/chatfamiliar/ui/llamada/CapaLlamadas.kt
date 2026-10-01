package com.example.chatfamiliar.ui.llamada

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.R
import com.example.chatfamiliar.data.llamada.DatosLlamada
import com.example.chatfamiliar.ui.comun.AvatarIdentidad
import com.example.chatfamiliar.ui.language.IdiomaViewModel
import com.example.chatfamiliar.ui.language.TextosApp
import com.example.chatfamiliar.ui.language.recordarIdiomaEfectivo
import com.example.chatfamiliar.ui.language.recordarTextosApp
import com.example.chatfamiliar.ui.theme.funcional
import io.getstream.video.android.core.Call
import io.getstream.video.android.core.RingingState

/**
 * Capa que se dibuja encima de toda la navegación y decide qué
 * pantalla de llamada mostrar:
 * - activeCall (en curso)        -> PantallaLlamadaActiva
 * - ringingCall + Incoming       -> PantallaLlamadaEntrante
 * - ringingCall + Outgoing       -> PantallaLlamadaSaliente
 * Si no hay llamada no dibuja nada y la app se usa normal.
 */
@Composable
fun CapaLlamadas(
    llamadaViewModel: LlamadaViewModel,
    idiomaViewModel: IdiomaViewModel
) {
    val idioma = recordarIdiomaEfectivo(idiomaViewModel.idiomaSeleccionado)
    val textos = recordarTextosApp(idiomaEfectivo = idioma,
        traducir = idiomaViewModel::traducir)
    val contexto = LocalContext.current

    // Avisos breves: errores al llamar o "no contestó".
    val textoAviso = llamadaViewModel.avisoRecurso?.let { textos.texto(it) }
    LaunchedEffect(textoAviso) {
        if (textoAviso != null) {
            Toast.makeText(contexto, textoAviso, Toast.LENGTH_LONG).show()
            llamadaViewModel.limpiarAviso()
        }
    }

    val cliente = llamadaViewModel.cliente.collectAsState().value ?: return
    val activa = cliente.state.activeCall.collectAsState().value
    val sonando = cliente.state.ringingCall.collectAsState().value

    if (activa != null) {
        PantallaLlamadaActiva(llamada = activa, miUid = cliente.userId,
            textos = textos, llamadaViewModel = llamadaViewModel)
        return
    }
    if (sonando == null) return

    val estado = sonando.state.ringingState.collectAsState().value
    when (estado) {
        is RingingState.Incoming -> PantallaLlamadaEntrante(
            llamada = sonando, textos = textos, idioma = idioma,
            conectando = estado.acceptedByMe || llamadaViewModel.conectando,
            aceptarAlMostrar = llamadaViewModel.aceptarAlMostrar,
            alAceptar = { llamadaViewModel.aceptar(sonando) },
            alRechazar = { llamadaViewModel.rechazar(sonando) })
        is RingingState.Outgoing -> PantallaLlamadaSaliente(
            llamada = sonando, miUid = cliente.userId, textos = textos,
            idioma = idioma, conectando = estado.acceptedByCallee,
            alCancelar = { llamadaViewModel.cancelar(sonando) })
        else -> Unit
    }
}

@Composable
private fun PantallaLlamadaEntrante(
    llamada: Call,
    textos: TextosApp,
    idioma: String,
    conectando: Boolean,
    aceptarAlMostrar: Boolean,
    alAceptar: () -> Unit,
    alRechazar: () -> Unit
) {
    val creador = llamada.state.createdBy.collectAsState().value
    val nombreCreador = creador?.userNameOrId
        ?: textos.texto(R.string.chat_item_unknown_user)
    // En llamadas familiares se muestra la familia y, debajo, quién llama.
    val familia = infoFamilia(llamada)
    val nombreFamilia = familia?.nombre?.ifBlank {
        textos.texto(R.string.chat_item_unnamed_family) }
    var pidiendoPermisos by rememberSaveable(llamada.id) { mutableStateOf(false) }

    // Si se tocó "Contestar" en la notificación, contestamos al abrir.
    LaunchedEffect(aceptarAlMostrar) {
        if (aceptarAlMostrar) pidiendoPermisos = true
    }

    val semilla = familia?.familiaId ?: creador?.id.orEmpty()
    FondoLlamada(semilla = semilla) {
        EncabezadoLlamada(semilla = semilla,
            nombre = nombreFamilia ?: nombreCreador,
            estado = textos.texto(when {
                conectando -> R.string.llamada_conectando
                familia != null -> R.string.llamada_entrante_familia
                else -> R.string.llamada_entrante }),
            detalle = if (familia != null) nombreCreador else null,
            esGrupo = familia != null,
            idioma = idioma, animar = !conectando)
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly) {
            BotonCircularLlamada(
                icono = Icons.Filled.CallEnd,
                etiqueta = textos.texto(R.string.llamada_rechazar),
                contenedor = MaterialTheme.funcional.destructivo,
                contenido = MaterialTheme.funcional.sobreDestructivo,
                habilitado = !conectando,
                alPulsar = alRechazar)
            BotonCircularLlamada(
                icono = Icons.Filled.Call,
                etiqueta = textos.texto(R.string.llamada_aceptar),
                contenedor = MaterialTheme.funcional.accionPrincipal,
                contenido = MaterialTheme.funcional.sobreAccionPrincipal,
                habilitado = !conectando,
                cargando = conectando,
                alPulsar = { pidiendoPermisos = true })
        }
    }

    if (pidiendoPermisos) {
        SolicitudPermisosLlamada(textos = textos,
            alPermisosConcedidos = {
                pidiendoPermisos = false
                alAceptar()
            },
            alCancelar = { pidiendoPermisos = false })
    }
}

@Composable
private fun PantallaLlamadaSaliente(
    llamada: Call,
    miUid: String,
    textos: TextosApp,
    idioma: String,
    conectando: Boolean,
    alCancelar: () -> Unit
) {
    val miembros = llamada.state.members.collectAsState().value
    val otro = miembros.firstOrNull { it.user.id != miUid }?.user
    val familia = infoFamilia(llamada)
    val nombre = familia?.nombre?.ifBlank {
        textos.texto(R.string.chat_item_unnamed_family) }
        ?: otro?.userNameOrId
        ?: textos.texto(R.string.chat_item_unknown_user)
    val semilla = familia?.familiaId ?: otro?.id.orEmpty()

    FondoLlamada(semilla = semilla) {
        EncabezadoLlamada(semilla = semilla, nombre = nombre,
            estado = textos.texto(when {
                conectando -> R.string.llamada_conectando
                familia != null -> R.string.llamada_saliente_familia
                else -> R.string.llamada_saliente }),
            esGrupo = familia != null,
            idioma = idioma, animar = !conectando)
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center) {
            BotonCircularLlamada(
                icono = Icons.Filled.CallEnd,
                etiqueta = textos.texto(R.string.llamada_cancelar),
                contenedor = MaterialTheme.funcional.destructivo,
                contenido = MaterialTheme.funcional.sobreDestructivo,
                habilitado = !conectando,
                alPulsar = alCancelar)
        }
    }
}

/**
 * Fondo de pantalla completa con un degradado suave del tono de
 * identidad de la otra persona. Bloquea el botón atrás para no
 * navegar por debajo de la llamada.
 */
@Composable
private fun FondoLlamada(semilla: String, contenido: @Composable () -> Unit) {
    BackHandler(enabled = true) { }
    val tono = MaterialTheme.funcional.tonoPara(semilla)
    val superficie = MaterialTheme.colorScheme.surface
    Surface(modifier = Modifier.fillMaxSize(), color = superficie) {
        Column(modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(tono.suave, superficie)))
            .safeDrawingPadding()
            .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween) {
            contenido()
        }
    }
}

@Composable
private fun EncabezadoLlamada(semilla: String, nombre: String, estado: String,
                              idioma: String, animar: Boolean,
                              detalle: String? = null, esGrupo: Boolean = false) {
    val tono = MaterialTheme.funcional.tonoPara(semilla)
    // Aro que "late" alrededor del avatar mientras suena.
    val transicion = rememberInfiniteTransition(label = "timbre")
    val escala by transicion.animateFloat(initialValue = 1f, targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing),
            RepeatMode.Reverse), label = "escalaAro")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(32.dp))
        Box(contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(140.dp)
                .scale(if (animar) escala else 1f)
                .background(tono.contenedor.copy(alpha = 0.45f), CircleShape))
            AvatarIdentidad(semilla = semilla, nombre = nombre,
                icono = if (esGrupo) Icons.Filled.Groups else null,
                esGrupo = esGrupo, tamano = 112.dp, idioma = idioma)
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(text = nombre, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
            color = MaterialTheme.funcional.encabezado,
            maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = estado, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (detalle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = detalle, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Familia a la que pertenece la llamada (null si es 1 a 1). */
internal data class InfoLlamadaFamilia(val familiaId: String, val nombre: String)

/**
 * Lee los datos extra (custom) que quien llamó guardó en la llamada.
 * Así quien recibe sabe que es una llamada familiar y de qué familia.
 */
@Composable
internal fun infoFamilia(llamada: Call): InfoLlamadaFamilia? {
    val datos = llamada.state.custom.collectAsState().value
    if (datos[DatosLlamada.CLAVE_TIPO_CHAT] != DatosLlamada.TIPO_FAMILIA) return null
    val familiaId = datos[DatosLlamada.CLAVE_FAMILIA_ID] as? String ?: return null
    val nombre = datos[DatosLlamada.CLAVE_NOMBRE_FAMILIA] as? String
    return InfoLlamadaFamilia(familiaId = familiaId, nombre = nombre.orEmpty())
}

/** Botón redondo grande con su etiqueta debajo. */
@Composable
internal fun BotonCircularLlamada(
    icono: ImageVector,
    etiqueta: String,
    contenedor: Color,
    contenido: Color,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    tamano: Dp = 72.dp,
    colorEtiqueta: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        FilledIconButton(onClick = alPulsar, enabled = habilitado,
            modifier = Modifier.size(tamano), shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = contenedor, contentColor = contenido,
                disabledContainerColor = contenedor.copy(alpha = 0.5f),
                disabledContentColor = contenido.copy(alpha = 0.7f))) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.size(tamano * 0.4f),
                    color = contenido, strokeWidth = 2.dp)
            } else {
                Icon(imageVector = icono, contentDescription = etiqueta,
                    modifier = Modifier.size(tamano * 0.42f))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = etiqueta, style = MaterialTheme.typography.labelLarge,
            color = colorEtiqueta)
    }
}