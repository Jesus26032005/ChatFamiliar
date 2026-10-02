package com.example.chatfamiliar.ui.llamada

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.R
import com.example.chatfamiliar.ui.comun.AvatarIdentidad
import com.example.chatfamiliar.ui.language.TextosApp
import com.example.chatfamiliar.ui.theme.funcional
import io.getstream.video.android.compose.theme.VideoTheme
import io.getstream.video.android.compose.ui.components.video.VideoRenderer
import io.getstream.video.android.compose.ui.components.video.VideoScalingType
import io.getstream.video.android.compose.ui.components.video.config.videoRenderConfig
import io.getstream.video.android.core.Call
import io.getstream.video.android.core.ParticipantState
import kotlinx.coroutines.delay
import java.util.Locale

// La llamada siempre se ve sobre fondo oscuro (como cualquier app de
// video), sin importar el tema de la app. Por eso estos colores son fijos.
private val FondoPantalla = Color(0xFF0D0F14)
private val FondoMosaico = Color(0xFF15171F)
private val FondoCapsula = Color(0xB314161F)
private val FondoDock = Color(0xD1181A26)
private val FondoEtiqueta = Color(0x990F1018)
private val FondoBoton = Color.White.copy(alpha = 0.14f)
private val FondoBotonApagado = Color.White
private val SobreBotonApagado = Color(0xFF1B1C22)
private val RojoColgar = Color(0xFFE5484D)
private val BordeHablando = Color(0xFF7C8BFF)
private val VerdeConectado = Color(0xFF4ADE80)
private val TextoSecundario = Color(0xFFD6D9E6)

private val FormaMosaico = RoundedCornerShape(22.dp)
private val FormaRecuadro = RoundedCornerShape(20.dp)

/**
 * Pantalla de la llamada en curso.
 * - 1 a 1: la otra persona a pantalla completa y tú en un recuadro.
 * - Familiar: cuadrícula de mosaicos redondeados.
 * Encima: cápsula con avatar, nombre y duración. Abajo: barra flotante
 * con micrófono, cámara y colgar (el único botón rojo).
 * Si alguien apaga la cámara se ve su avatar de identidad.
 * Colgar al quedarse solo lo decide LlamadaViewModel, no esta pantalla.
 */
@Composable
fun PantallaLlamadaActiva(
    llamada: Call,
    miUid: String,
    textos: TextosApp,
    idioma: String,
    llamadaViewModel: LlamadaViewModel
) {
    BackHandler(enabled = true) { }

    // Mantiene la pantalla encendida durante la llamada.
    val vista = LocalView.current
    DisposableEffect(vista) {
        vista.keepScreenOn = true
        onDispose { vista.keepScreenOn = false }
    }

    val microfonoEncendido = llamada.microphone.isEnabled.collectAsState().value
    val camaraEncendida = llamada.camera.isEnabled.collectAsState().value
    val remotos = llamada.state.remoteParticipants.collectAsState().value
    val yo = llamada.state.me.collectAsState().value
    val miembros = llamada.state.members.collectAsState().value
    val otro = miembros.firstOrNull { it.user.id != miUid }?.user
    // En llamada familiar el título es la familia; en 1 a 1, la otra persona.
    val familia = infoFamilia(llamada)
    val nombre = familia?.nombre?.ifBlank {
        textos.texto(R.string.chat_item_unnamed_family) }
        ?: otro?.userNameOrId
        ?: textos.texto(R.string.chat_item_unknown_user)
    val textoTu = textos.texto(R.string.llamada_tu)
    val textoCamaraApagada = textos.texto(R.string.llamada_estado_camara_apagada)

    // Cronómetro: arranca cuando entra alguien más.
    var huboRemoto by remember(llamada.id) { mutableStateOf(false) }
    LaunchedEffect(remotos.isEmpty()) {
        if (remotos.isNotEmpty()) huboRemoto = true
    }
    var segundos by remember(llamada.id) { mutableIntStateOf(0) }
    LaunchedEffect(huboRemoto) {
        if (huboRemoto) {
            while (true) {
                delay(1_000)
                segundos++
            }
        }
    }
    val estadoLlamada = if (huboRemoto) formatoDuracion(segundos)
    else textos.texto(R.string.llamada_esperando)

    Box(modifier = Modifier.fillMaxSize().background(FondoPantalla)) {
        // Los renderizadores de video del SDK necesitan su VideoTheme.
        VideoTheme(isInDarkMode = true) {
            if (familia == null) {
                VistaUnoAUno(llamada = llamada, remoto = remotos.firstOrNull(), yo = yo,
                    semillaOtro = otro?.id.orEmpty(), nombreOtro = nombre,
                    textoCamaraApagada = textoCamaraApagada, idioma = idioma)
            } else {
                // Tú al final de la cuadrícula.
                val todos = remotos + listOfNotNull(yo)
                CuadriculaFamilia(llamada = llamada, participantes = todos,
                    textoTu = textoTu, idioma = idioma)
            }
        }

        CapsulaSuperior(
            nombre = nombre,
            // En familia: cuántos están conectados (contándote) y la duración.
            estado = if (familia != null && huboRemoto) {
                "${remotos.size + 1} · $estadoLlamada" } else estadoLlamada,
            semilla = familia?.familiaId ?: otro?.id.orEmpty(),
            esGrupo = familia != null,
            idioma = idioma,
            modifier = Modifier.align(Alignment.TopStart))

        BarraControles(
            textos = textos,
            microfonoEncendido = microfonoEncendido,
            camaraEncendida = camaraEncendida,
            alAlternarMicrofono = { llamadaViewModel.alternarMicrofono(llamada) },
            alAlternarCamara = { llamadaViewModel.alternarCamara(llamada) },
            alColgar = { llamadaViewModel.colgar(llamada) },
            modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 1 a 1: la otra persona ocupa todo; tú en un recuadro arriba a la derecha. */
@Composable
private fun VistaUnoAUno(
    llamada: Call,
    remoto: ParticipantState?,
    yo: ParticipantState?,
    semillaOtro: String,
    nombreOtro: String,
    textoCamaraApagada: String,
    idioma: String
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (remoto != null) {
            MosaicoParticipante(llamada = llamada, participante = remoto,
                nombre = nombreOtro, idioma = idioma,
                modifier = Modifier.fillMaxSize(), forma = RoundedCornerShape(0.dp),
                tamanoAvatar = 116.dp, mostrarEtiqueta = false,
                textoCamaraApagada = textoCamaraApagada)
        } else {
            // Aún no entra: su avatar de identidad mientras se conecta.
            FondoSinCamara(semilla = semillaOtro, nombre = nombreOtro, idioma = idioma,
                tamanoAvatar = 116.dp, textoEstado = null)
        }
        if (yo != null) {
            MosaicoParticipante(llamada = llamada, participante = yo,
                nombre = nombreOtro, idioma = idioma, esLocal = true,
                modifier = Modifier.align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 64.dp, end = 14.dp)
                    .size(width = 104.dp, height = 150.dp)
                    .border(2.dp, Color.White.copy(alpha = 0.85f), FormaRecuadro),
                forma = FormaRecuadro, tamanoAvatar = 48.dp,
                mostrarEtiqueta = false, mostrarMicApagado = true,
                textoCamaraApagada = null)
        }
    }
}

/**
 * Familiar: cuadrícula que se ajusta al número de personas.
 * 1-2 en una columna, 3 o más en dos columnas; hasta 3 filas visibles
 * y si hay más se puede desplazar.
 */
@Composable
private fun CuadriculaFamilia(
    llamada: Call,
    participantes: List<ParticipantState>,
    textoTu: String,
    idioma: String
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(start = 10.dp, end = 10.dp, top = 64.dp, bottom = 116.dp)) {
        val columnas = if (participantes.size <= 2) 1 else 2
        val filas = ((participantes.size + columnas - 1) / columnas).coerceIn(1, 3)
        val separacion = 8.dp
        val altoMosaico = (maxHeight - separacion * (filas - 1)) / filas
        LazyVerticalGrid(columns = GridCells.Fixed(columnas),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(separacion),
            verticalArrangement = Arrangement.spacedBy(separacion),
            contentPadding = PaddingValues(0.dp)) {
            items(items = participantes, key = { it.sessionId }) { participante ->
                val esLocal = participante.isLocal
                val nombreRemoto = participante.userNameOrId.collectAsState().value
                MosaicoParticipante(llamada = llamada, participante = participante,
                    nombre = if (esLocal) textoTu else nombreRemoto,
                    idioma = idioma, esLocal = esLocal,
                    modifier = Modifier.fillMaxWidth().height(altoMosaico),
                    forma = FormaMosaico, tamanoAvatar = 70.dp,
                    mostrarEtiqueta = true, marcarHablando = true,
                    textoCamaraApagada = null)
            }
        }
    }
}

/**
 * Un participante: su video o, si tiene la cámara apagada, su avatar de
 * identidad sobre un degradado de su color.
 */
@Composable
private fun MosaicoParticipante(
    llamada: Call,
    participante: ParticipantState,
    nombre: String,
    idioma: String,
    modifier: Modifier,
    forma: Shape,
    tamanoAvatar: Dp,
    mostrarEtiqueta: Boolean,
    textoCamaraApagada: String?,
    esLocal: Boolean = false,
    marcarHablando: Boolean = false,
    mostrarMicApagado: Boolean = false
) {
    val video = participante.video.collectAsState().value
    val audioActivo = participante.audioEnabled.collectAsState().value
    val hablando = participante.speaking.collectAsState().value
    val uid = participante.userId.collectAsState().value
    // Quien está hablando se marca con un borde índigo (solo en familia).
    val borde = if (marcarHablando && hablando) {
        Modifier.border(3.dp, BordeHablando, forma) } else Modifier

    Box(modifier = modifier.clip(forma).background(FondoMosaico).then(borde)) {
        VideoRenderer(
            modifier = Modifier.fillMaxSize(),
            call = llamada,
            video = video,
            videoRendererConfig = videoRenderConfig {
                // Tu cámara frontal se ve como espejo, como en cualquier app.
                mirrorStream = esLocal
                videoScalingType = VideoScalingType.SCALE_ASPECT_FILL
                // Se dibuja cuando no hay video (cámara apagada o cargando).
                fallbackContent = {
                    FondoSinCamara(semilla = uid, nombre = nombre, idioma = idioma,
                        tamanoAvatar = tamanoAvatar, textoEstado = textoCamaraApagada)
                }
            })

        if (mostrarEtiqueta) {
            Row(modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                .background(FondoEtiqueta, RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                if (!audioActivo) {
                    Icon(imageVector = Icons.Filled.MicOff, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(text = nombre, color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        // En tu recuadro: círculo rojo si tienes el micrófono apagado.
        if (mostrarMicApagado && !audioActivo) {
            Box(modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                .size(24.dp).background(RojoColgar, CircleShape),
                contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Filled.MicOff, contentDescription = null,
                    tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

/** Avatar de identidad sobre un degradado de su color (cámara apagada). */
@Composable
private fun FondoSinCamara(
    semilla: String,
    nombre: String,
    idioma: String,
    tamanoAvatar: Dp,
    textoEstado: String?
) {
    val tono = MaterialTheme.funcional.tonoPara(semilla)
    Column(modifier = Modifier.fillMaxSize()
        .background(Brush.radialGradient(
            listOf(tono.contenedor.copy(alpha = 0.35f), FondoMosaico))),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        // Aro suave alrededor del avatar, como en la pantalla de timbre.
        Box(modifier = Modifier.size(tamanoAvatar * 1.3f)
            .background(tono.contenedor.copy(alpha = 0.22f), CircleShape),
            contentAlignment = Alignment.Center) {
            AvatarIdentidad(semilla = semilla, nombre = nombre,
                tamano = tamanoAvatar, idioma = idioma)
        }
        if (textoEstado != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = nombre, color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.VideocamOff, contentDescription = null,
                    tint = TextoSecundario, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = textoEstado, color = TextoSecundario,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** Cápsula superior: avatar (o icono de grupo), nombre y duración. */
@Composable
private fun CapsulaSuperior(
    nombre: String,
    estado: String,
    semilla: String,
    esGrupo: Boolean,
    idioma: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.statusBarsPadding()
        .padding(start = 14.dp, top = 10.dp, end = 130.dp)
        .background(FondoCapsula, RoundedCornerShape(50))
        .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically) {
        AvatarIdentidad(semilla = semilla, nombre = nombre,
            icono = if (esGrupo) Icons.Filled.Groups else null,
            esGrupo = esGrupo, tamano = 36.dp, idioma = idioma)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = nombre, color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(7.dp).background(VerdeConectado, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = estado, color = TextoSecundario,
                    style = MaterialTheme.typography.labelMedium, maxLines = 1)
            }
        }
    }
}

/** Barra flotante: micrófono, cámara y colgar. */
@Composable
private fun BarraControles(
    textos: TextosApp,
    microfonoEncendido: Boolean,
    camaraEncendida: Boolean,
    alAlternarMicrofono: () -> Unit,
    alAlternarCamara: () -> Unit,
    alColgar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 14.dp, vertical = 20.dp)
        .background(FondoDock, RoundedCornerShape(30.dp))
        .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top) {
        // Apagado = botón blanco, para que se note a simple vista.
        BotonControl(
            icono = if (microfonoEncendido) Icons.Filled.Mic else Icons.Filled.MicOff,
            etiqueta = textos.texto(if (microfonoEncendido) R.string.llamada_silenciar
            else R.string.llamada_activar_microfono),
            fondo = if (microfonoEncendido) FondoBoton else FondoBotonApagado,
            contenido = if (microfonoEncendido) Color.White else SobreBotonApagado,
            alPulsar = alAlternarMicrofono)
        BotonControl(
            icono = if (camaraEncendida) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
            etiqueta = textos.texto(if (camaraEncendida) R.string.llamada_apagar_camara
            else R.string.llamada_encender_camara),
            fondo = if (camaraEncendida) FondoBoton else FondoBotonApagado,
            contenido = if (camaraEncendida) Color.White else SobreBotonApagado,
            alPulsar = alAlternarCamara)
        BotonControl(
            icono = Icons.Filled.CallEnd,
            etiqueta = textos.texto(R.string.llamada_colgar),
            fondo = RojoColgar, contenido = Color.White,
            ancho = 72.dp, alPulsar = alColgar)
    }
}

/** Botón de la barra: círculo (o cápsula para colgar) con etiqueta debajo. */
@Composable
private fun BotonControl(
    icono: ImageVector,
    etiqueta: String,
    fondo: Color,
    contenido: Color,
    alPulsar: () -> Unit,
    ancho: Dp = 54.dp
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(onClick = alPulsar, shape = RoundedCornerShape(27.dp),
            color = fondo, contentColor = contenido,
            modifier = Modifier.size(width = ancho, height = 54.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icono, contentDescription = etiqueta,
                    modifier = Modifier.size(26.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = etiqueta, color = Color(0xFFE5E7F0),
            style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

private fun formatoDuracion(segundosTotales: Int): String {
    val horas = segundosTotales / 3600
    val minutos = (segundosTotales % 3600) / 60
    val segundos = segundosTotales % 60
    return if (horas > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", horas, minutos, segundos)
    } else {
        String.format(Locale.ROOT, "%02d:%02d", minutos, segundos)
    }
}