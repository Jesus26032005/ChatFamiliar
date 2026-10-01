package com.example.chatfamiliar.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.R
import com.example.chatfamiliar.model.ConversacionResumen
import com.example.chatfamiliar.ui.chat.EstadoConversacionInicio
import com.example.chatfamiliar.ui.comun.AvatarIdentidad
import com.example.chatfamiliar.ui.theme.funcional
import com.example.chatfamiliar.ui.language.TraducirTexto
import com.example.chatfamiliar.ui.language.recordarTextosApp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ItemConversacion(
    estado: EstadoConversacionInicio,
    uidActual: String,
    textoFecha: String,
    idiomaEfectivo: String,
    traducir: TraducirTexto,
    alAbrir: () -> Unit,
    alReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textos = recordarTextosApp(
        idiomaEfectivo = idiomaEfectivo,
        traducir = traducir
    )
    val colores = MaterialTheme.colorScheme
    val resumen = estado.resumen
    val esFamilia = resumen.tipo == ConversacionResumen.TIPO_FAMILIA
    val cantidad = estado.mensajesNoLeidosVisibles
    val tieneNoLeidos = (cantidad ?: 0) > 0

    val funcional = MaterialTheme.funcional

    val formaTarjeta = RoundedCornerShape(22.dp)

    val titulo = when {
        resumen.titulo.isNotBlank() -> resumen.titulo
        estado.cargandoNombre ->
            textos.texto(R.string.chat_item_loading_name)

        esFamilia ->
            textos.texto(R.string.chat_item_unnamed_family)

        else ->
            textos.texto(R.string.chat_item_unknown_user)
    }
    val errorUltimoMensaje = estado.errorUltimoMensajeRecurso
    val vistaPrevia = when {
        estado.cargandoUltimoMensaje &&
                resumen.ultimoMensaje.isBlank() -> {
            textos.texto(R.string.chat_item_loading_message)
        }
        resumen.ultimoMensaje.isBlank() &&
                errorUltimoMensaje != null -> {
            textos.texto(errorUltimoMensaje)
        }

        resumen.ultimoMensaje.isBlank() -> {
            textos.texto(R.string.chat_item_empty)
        }

        uidActual.isNotBlank() &&
                resumen.ultimoRemitenteId == uidActual -> {
            "${textos.texto(R.string.chat_item_you)}: " +
                    resumen.ultimoMensaje
        }

        esFamilia && resumen.nombreUltimoRemitente.isNotBlank() -> {
            "${resumen.nombreUltimoRemitente}: ${resumen.ultimoMensaje}"
        }

        else -> resumen.ultimoMensaje
    }

    val errorRecurso = estado.errorUltimoMensajeRecurso
        ?: estado.errorNombreRecurso
        ?: estado.errorConteoRecurso

    val etiquetaAbrir = textos.texto(R.string.chat_item_open)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(formaTarjeta)
            .background(
                if (tieneNoLeidos) {
                    funcional.tarjeta
                } else {
                    funcional.tarjetaInterior
                }
            )
            .border(
                width = 1.dp,
                color = if (tieneNoLeidos) {
                    funcional.bordePendiente
                } else {
                    colores.outlineVariant
                },
                shape = formaTarjeta
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    role = Role.Button,
                    onClickLabel = etiquetaAbrir,
                    onClick = alAbrir
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            AvatarIdentidad(
                // Privados: el uid de la persona, para que conserve su tono
                // también en "Nuevo mensaje" y en la cabecera del chat.
                semilla = if (esFamilia) {
                    resumen.id
                } else {
                    resumen.otroUsuarioId.ifBlank { resumen.id }
                },
                nombre = resumen.titulo,
                icono = if (esFamilia) Icons.Filled.Groups else null,
                esGrupo = esFamilia,
                idioma = idiomaEfectivo
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (tieneNoLeidos) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    },
                    color = colores.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Etiqueta neutra: el tipo se distingue por icono y texto,
                // el color de identidad queda para el avatar.
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = funcional.etiquetaNeutra,
                    contentColor = funcional.sobreEtiquetaNeutra
                ) {
                    Row(
                        modifier = Modifier.padding(
                            start = 6.dp,
                            end = 8.dp,
                            top = 3.dp,
                            bottom = 3.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (esFamilia) {
                                Icons.Filled.Groups
                            } else {
                                Icons.Filled.Person
                            },
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = textos.texto(
                                if (esFamilia) {
                                    R.string.chat_item_family
                                } else {
                                    R.string.chat_item_private
                                }
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = vistaPrevia
                        .replace(Regex("\\s+"), " ")
                        .trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colores.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (
                    estado.conteoConfirmado &&
                    (
                            estado.actualizandoConteo ||
                                    estado.errorConteoRecurso != null
                            )
                ) {
                    val recursoEstado = if (estado.actualizandoConteo) {
                        R.string.chat_item_updating
                    } else {
                        R.string.chat_item_stale_count
                    }

                    Text(
                        text = textos.texto(recursoEstado),
                        style = MaterialTheme.typography.labelSmall,
                        color = colores.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.widthIn(max = 84.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (textoFecha.isNotBlank()) {
                    Text(
                        text = textoFecha,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (tieneNoLeidos) {
                            funcional.acento
                        } else {
                            colores.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (cantidad != null && cantidad > 0) {
                    val cantidadFormateada = NumberFormat
                        .getIntegerInstance(
                            Locale.forLanguageTag(idiomaEfectivo)
                        )
                        .format(cantidad)

                    val textoContador = if (cantidad > 99) {
                        textos.texto(R.string.chat_item_many_unread)
                    } else {
                        cantidadFormateada
                    }

                    val descripcion =
                        "${textos.texto(R.string.chat_item_unread)}: " +
                                cantidadFormateada

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = funcional.insignia,
                        contentColor = funcional.sobreInsignia,
                        modifier = Modifier.clearAndSetSemantics {
                            contentDescription = descripcion
                        }
                    ) {
                        Text(
                            text = textoContador,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 4.dp
                            )
                        )
                    }
                } else if (
                    !estado.conteoConfirmado &&
                    estado.errorConteoRecurso == null
                ) {
                    val descripcion = textos.texto(
                        R.string.chat_item_loading_unread
                    )

                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(18.dp)
                            .semantics {
                                contentDescription = descripcion
                            },
                        strokeWidth = 2.dp,
                        color = funcional.acento
                    )
                }
            }
        }

        if (errorRecurso != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                val errorYaVisible =
                    errorRecurso == estado.errorUltimoMensajeRecurso &&
                            resumen.ultimoMensaje.isBlank() &&
                            !estado.cargandoUltimoMensaje

                if (!errorYaVisible) {
                    Text(
                        text = textos.texto(errorRecurso),
                        style = MaterialTheme.typography.bodySmall,
                        color = colores.error
                    )
                }

                TextButton(
                    onClick = alReintentar,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = textos.texto(R.string.chat_item_retry)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}