package com.example.chatfamiliar.ui.chat.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.R
import com.example.chatfamiliar.ui.language.TraducirTexto
import com.example.chatfamiliar.ui.language.recordarTextosApp
import com.example.chatfamiliar.ui.theme.funcional
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BarraEscrituraMensaje(
    texto: String,
    puedeEnviar: Boolean,
    enviando: Boolean,
    idiomaEfectivo: String,
    traducir: TraducirTexto,
    alCambiarTexto: (String) -> Unit,
    alEnviar: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes errorRecurso: Int? = null,
    @StringRes motivoBloqueoRecurso: Int? = null,
    maximoCaracteres: Int = 2000
) {
    require(maximoCaracteres > 0)

    val textos = recordarTextosApp(
        idiomaEfectivo = idiomaEfectivo,
        traducir = traducir
    )

    val contenidoLimpio = texto.trim()
    val longitud = contenidoLimpio.length
    val excedeLimite = longitud > maximoCaracteres
    val mostrarConteo = longitud >= maximoCaracteres * 0.9

    val envioHabilitado =
        puedeEnviar &&
                !enviando &&
                contenidoLimpio.isNotEmpty() &&
                !excedeLimite

    val descripcionBoton = textos.texto(
        if (enviando) {
            R.string.chat_composer_sending
        } else {
            R.string.chat_composer_send
        }
    )

    val formatoNumero = NumberFormat.getIntegerInstance(
        Locale.forLanguageTag(idiomaEfectivo)
    )
    val etiquetaCampo = textos.texto(R.string.chat_composer_label)
    val colores = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colores.surface
    ) {
        Column {
            // Línea fina que separa la barra de los mensajes
            HorizontalDivider(color = colores.outlineVariant)

            Column(
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 10.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!puedeEnviar) {
                    Text(
                        text = textos.texto(
                            motivoBloqueoRecurso
                                ?: R.string.chat_composer_unavailable
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colores.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Campo relleno, sin contorno ni etiqueta flotante:
                    // el texto guía desaparece al escribir, como en las apps de chat.
                    TextField(
                        value = texto,
                        onValueChange = alCambiarTexto,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = etiquetaCampo },
                        enabled = puedeEnviar && !enviando,
                        minLines = 1,
                        maxLines = 5,
                        singleLine = false,
                        isError = excedeLimite,
                        shape = RoundedCornerShape(26.dp),
                        placeholder = {
                            Text(text = etiquetaCampo)
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colores.surfaceContainerHigh,
                            unfocusedContainerColor = colores.surfaceContainerHigh,
                            disabledContainerColor = colores.surfaceContainer,
                            errorContainerColor = colores.errorContainer,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization =
                                KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Default
                        )
                    )

                    FilledIconButton(
                        onClick = alEnviar,
                        enabled = envioHabilitado,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.funcional.accionPrincipal,
                            contentColor = MaterialTheme.funcional.sobreAccionPrincipal
                        ),
                        modifier = Modifier
                            .padding(bottom = 2.dp)
                            .size(52.dp)
                            .semantics {
                                contentDescription = descripcionBoton
                            }
                    ) {
                        if (enviando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = colores.onSurfaceVariant
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null
                            )
                        }
                    }
                }

                if (mostrarConteo) {
                    Text(
                        text =
                            "${formatoNumero.format(longitud)} / " +
                                    formatoNumero.format(maximoCaracteres),
                        modifier = Modifier.align(Alignment.End),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (excedeLimite) {
                            colores.error
                        } else {
                            colores.onSurfaceVariant
                        }
                    )
                }

                if (excedeLimite) {
                    Text(
                        text = textos.texto(
                            R.string.chat_composer_too_long
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = colores.error
                    )
                }

                if (errorRecurso != null) {
                    Text(
                        text = textos.texto(errorRecurso),
                        style = MaterialTheme.typography.bodySmall,
                        color = colores.error
                    )
                }
            }
        }
    }
}