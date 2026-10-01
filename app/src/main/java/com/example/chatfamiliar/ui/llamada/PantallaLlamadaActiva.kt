package com.example.chatfamiliar.ui.llamada

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.R
import com.example.chatfamiliar.ui.language.TextosApp
import com.example.chatfamiliar.ui.theme.funcional
import io.getstream.video.android.compose.theme.VideoTheme
import io.getstream.video.android.compose.ui.components.call.renderer.ParticipantsLayout
import io.getstream.video.android.core.Call
import kotlinx.coroutines.delay
import java.util.Locale

// La llamada siempre se ve sobre fondo oscuro (como cualquier app de
// video), por eso los controles usan blanco translúcido en ambos temas.
private val FondoControl = Color.White.copy(alpha = 0.16f)
private val FondoControlApagado = Color.White
private val SobreControlApagado = Color(0xFF1B1B1F)
private val SombraBarras = Color.Black.copy(alpha = 0.6f)

@Composable
fun PantallaLlamadaActiva(
    llamada: Call,
    miUid: String,
    textos: TextosApp,
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
    val miembros = llamada.state.members.collectAsState().value
    val otro = miembros.firstOrNull { it.user.id != miUid }?.user
    // En llamada familiar el título es la familia; en 1 a 1, la otra persona.
    val familia = infoFamilia(llamada)
    val nombre = familia?.nombre?.ifBlank {
        textos.texto(R.string.chat_item_unnamed_family) }
        ?: otro?.userNameOrId
        ?: textos.texto(R.string.chat_item_unknown_user)

    // Si ya hubo alguien más y ahora no queda nadie, colgamos también.
    // En 1 a 1 pasa cuando la otra persona cuelga; en familia, cuando
    // se van todos (mientras quede alguien, la llamada sigue).
    var huboRemoto by remember(llamada.id) { mutableStateOf(false) }
    LaunchedEffect(remotos.isEmpty()) {
        if (remotos.isNotEmpty()) {
            huboRemoto = true
        } else if (huboRemoto) {
            llamadaViewModel.colgar(llamada)
        }
    }

    // Cronómetro: empieza cuando la otra persona se conecta.
    var segundos by remember(llamada.id) { mutableIntStateOf(0) }
    LaunchedEffect(huboRemoto) {
        if (huboRemoto) {
            while (true) {
                delay(1_000)
                segundos++
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Video de los participantes (componente del SDK de Stream).
        VideoTheme(isInDarkMode = true) {
            ParticipantsLayout(call = llamada, modifier = Modifier.fillMaxSize())
        }

        // Nombre y duración sobre un degradado para que se lean encima del video.
        Column(modifier = Modifier.align(Alignment.TopStart).fillMaxWidth()
            .background(Brush.verticalGradient(listOf(SombraBarras, Color.Transparent)))
            .statusBarsPadding()
            .padding(start = 20.dp, end = 140.dp, top = 12.dp, bottom = 28.dp)) {
            Text(text = nombre, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold, color = Color.White,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = if (huboRemoto) formatoDuracion(segundos)
            else textos.texto(R.string.llamada_esperando),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f))
        }

        // Controles
        Row(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, SombraBarras)))
            .navigationBarsPadding()
            .padding(top = 32.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly) {
            BotonCircularLlamada(
                icono = if (microfonoEncendido) Icons.Filled.Mic else Icons.Filled.MicOff,
                etiqueta = textos.texto(if (microfonoEncendido) R.string.llamada_silenciar
                else R.string.llamada_activar_microfono),
                contenedor = if (microfonoEncendido) FondoControl else FondoControlApagado,
                contenido = if (microfonoEncendido) Color.White else SobreControlApagado,
                tamano = 56.dp, colorEtiqueta = Color.White,
                alPulsar = { llamadaViewModel.alternarMicrofono(llamada) })
            BotonCircularLlamada(
                icono = if (camaraEncendida) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                etiqueta = textos.texto(if (camaraEncendida) R.string.llamada_apagar_camara
                else R.string.llamada_encender_camara),
                contenedor = if (camaraEncendida) FondoControl else FondoControlApagado,
                contenido = if (camaraEncendida) Color.White else SobreControlApagado,
                tamano = 56.dp, colorEtiqueta = Color.White,
                alPulsar = { llamadaViewModel.alternarCamara(llamada) })
            BotonCircularLlamada(
                icono = Icons.Filled.Cameraswitch,
                etiqueta = textos.texto(R.string.llamada_cambiar_camara),
                contenedor = FondoControl, contenido = Color.White,
                habilitado = camaraEncendida,
                tamano = 56.dp, colorEtiqueta = Color.White,
                alPulsar = { llamadaViewModel.voltearCamara(llamada) })
            BotonCircularLlamada(
                icono = Icons.Filled.CallEnd,
                etiqueta = textos.texto(R.string.llamada_colgar),
                contenedor = MaterialTheme.funcional.destructivo,
                contenido = MaterialTheme.funcional.sobreDestructivo,
                tamano = 56.dp, colorEtiqueta = Color.White,
                alPulsar = { llamadaViewModel.colgar(llamada) })
        }
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