package com.example.chatfamiliar.ui.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.R
import com.example.chatfamiliar.model.Mensaje
import com.example.chatfamiliar.ui.comun.AvatarIdentidad
import com.example.chatfamiliar.ui.language.TraducirTexto
import com.example.chatfamiliar.ui.language.recordarTextosApp
import com.example.chatfamiliar.ui.theme.funcional

private val EsquinaGrande = 20.dp
private val EsquinaChica = 6.dp
private val TamanoAvatarRemitente = 30.dp
private val EspacioEnGrupo = 6.dp
private val EspacioEntreGrupos = 14.dp

@Composable
fun BurbujaMensaje(mensaje: Mensaje, esPropio: Boolean,
    mostrarRemitente: Boolean, hora: String,
    idiomaEfectivo: String, traducir: TraducirTexto,
    modifier: Modifier = Modifier, primeroDelGrupo: Boolean = true,
    ultimoDelGrupo: Boolean = true) {
    val textos = recordarTextosApp(idiomaEfectivo = idiomaEfectivo, traducir = traducir)
    val funcional = MaterialTheme.funcional
    val colorFondo = if (esPropio) { funcional.burbujaPropia
    } else { funcional.burbujaRecibida }
    val colorTexto = if (esPropio) { funcional.sobreBurbujaPropia
    } else { funcional.sobreBurbujaRecibida }
    val colorSecundario = if (esPropio) { funcional.metaBurbujaPropia
    } else { funcional.metaBurbujaRecibida }
    val colorRemitente = funcional.tonoPara(mensaje.remitenteId).texto
    val nombreRemitente = mensaje.nombreRemitente.ifBlank {
        textos.texto(R.string.chat_message_unknown_sender) }
    val esquinaArriba = if (primeroDelGrupo) EsquinaGrande else EsquinaChica
    val forma = if (esPropio) {
        RoundedCornerShape(topStart = EsquinaGrande, topEnd = esquinaArriba,
            bottomEnd = EsquinaChica, bottomStart = EsquinaGrande)
    } else {
        RoundedCornerShape(topStart = esquinaArriba, topEnd = EsquinaGrande,
            bottomEnd = EsquinaGrande, bottomStart = EsquinaChica) }
    val conAvatar = mostrarRemitente && !esPropio

    Row(modifier = modifier.fillMaxWidth()
        .padding(horizontal = 12.dp)
        .padding(top = if (primeroDelGrupo) EspacioEntreGrupos else EspacioEnGrupo)
        .padding(start = if (esPropio) 48.dp else 0.dp,
            end = if (esPropio) 0.dp else 48.dp),
        horizontalArrangement = if (esPropio) { Arrangement.End
        } else { Arrangement.Start },
        verticalAlignment = Alignment.Bottom
    ) {
        if (conAvatar) {
            if (ultimoDelGrupo) {
                AvatarIdentidad(semilla = mensaje.remitenteId,
                    nombre = nombreRemitente,
                    tamano = TamanoAvatarRemitente,
                    idioma = idiomaEfectivo)
            } else {
                Spacer(modifier = Modifier.width(TamanoAvatarRemitente)) }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Surface(modifier = Modifier.widthIn(max = 360.dp),
            shape = forma, color = colorFondo, contentColor = colorTexto) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (mostrarRemitente && !esPropio && primeroDelGrupo) {
                    Text(text = nombreRemitente,
                        style = MaterialTheme.typography.labelLarge,
                        color = colorRemitente) }
                SelectionContainer {
                    Text(text = mensaje.contenido,
                        style = MaterialTheme.typography.bodyLarge, color = colorTexto) }
                Text(text = hora.ifBlank {
                    textos.texto(R.string.chat_message_time_pending) },
                    modifier = Modifier.align(Alignment.End),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorSecundario
                )
            }
        }
    }
}