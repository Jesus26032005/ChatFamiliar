package com.example.chatfamiliar.ui.llamada

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.chatfamiliar.R
import com.example.chatfamiliar.ui.language.TextosApp

private enum class EstadoPermisosLlamada {
    COMPROBANDO,
    EXPLICACION,
    SOLICITANDO,
    DENEGADOS,
    AJUSTES,
    CONCEDIDOS,
    FINALIZADO
}

@Composable
fun SolicitudPermisosLlamada(
    textos: TextosApp,
    alPermisosConcedidos: () -> Unit,
    alCancelar: () -> Unit
) {
    val contexto = LocalContext.current

    val alConcedidosActual by rememberUpdatedState(
        alPermisosConcedidos
    )

    val alCancelarActual by rememberUpdatedState(
        alCancelar
    )

    var estado by rememberSaveable {
        mutableStateOf(EstadoPermisosLlamada.COMPROBANDO)
    }

    val lanzadorPermisos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        estado = if (tienePermisosLlamada(contexto)) {
            EstadoPermisosLlamada.CONCEDIDOS
        } else {
            EstadoPermisosLlamada.DENEGADOS
        }
    }

    val lanzadorAjustes = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        estado = if (tienePermisosLlamada(contexto)) {
            EstadoPermisosLlamada.CONCEDIDOS
        } else {
            EstadoPermisosLlamada.DENEGADOS
        }
    }

    val cancelar: () -> Unit = {
        estado = EstadoPermisosLlamada.FINALIZADO
        alCancelarActual()
    }

    val solicitarPermisos: () -> Unit = {
        val pendientes = permisosLlamadaPendientes(contexto)

        if (pendientes.isEmpty()) {
            estado = EstadoPermisosLlamada.CONCEDIDOS
        } else {
            estado = EstadoPermisosLlamada.SOLICITANDO
            lanzadorPermisos.launch(pendientes)
        }
    }

    LaunchedEffect(estado) {
        when (estado) {
            EstadoPermisosLlamada.COMPROBANDO -> {
                estado = if (tienePermisosLlamada(contexto)) {
                    EstadoPermisosLlamada.CONCEDIDOS
                } else {
                    EstadoPermisosLlamada.EXPLICACION
                }
            }

            EstadoPermisosLlamada.CONCEDIDOS -> {
                if (tienePermisosLlamada(contexto)) {
                    estado = EstadoPermisosLlamada.FINALIZADO
                    alConcedidosActual()
                } else {
                    estado = EstadoPermisosLlamada.DENEGADOS
                }
            }

            else -> Unit
        }
    }

    when (estado) {
        EstadoPermisosLlamada.EXPLICACION -> {
            AlertDialog(
                onDismissRequest = cancelar,
                title = {
                    Text(
                        text = textos.texto(
                            R.string.llamada_permisos_titulo
                        )
                    )
                },
                text = {
                    Text(
                        text = textos.texto(
                            R.string.llamada_permisos_explicacion
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = solicitarPermisos
                    ) {
                        Text(
                            text = textos.texto(
                                R.string.llamada_permisos_continuar
                            )
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = cancelar
                    ) {
                        Text(
                            text = textos.texto(
                                R.string.llamada_permisos_cancelar
                            )
                        )
                    }
                }
            )
        }

        EstadoPermisosLlamada.DENEGADOS -> {
            AlertDialog(
                onDismissRequest = cancelar,
                title = {
                    Text(
                        text = textos.texto(
                            R.string.llamada_permisos_denegados_titulo
                        )
                    )
                },
                text = {
                    Text(
                        text = textos.texto(
                            R.string.llamada_permisos_denegados_mensaje
                        )
                    )
                },
                confirmButton = {
                    Column {
                        TextButton(
                            onClick = solicitarPermisos
                        ) {
                            Text(
                                text = textos.texto(
                                    R.string.llamada_permisos_reintentar
                                )
                            )
                        }

                        TextButton(
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts(
                                        "package",
                                        contexto.packageName,
                                        null
                                    )
                                )

                                estado = EstadoPermisosLlamada.AJUSTES
                                lanzadorAjustes.launch(intent)
                            }
                        ) {
                            Text(
                                text = textos.texto(
                                    R.string.llamada_permisos_abrir_ajustes
                                )
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = cancelar
                    ) {
                        Text(
                            text = textos.texto(
                                R.string.llamada_permisos_cerrar
                            )
                        )
                    }
                }
            )
        }

        else -> Unit
    }
}

private fun tienePermisosLlamada(
    contexto: Context
): Boolean {
    return permisosLlamadaPendientes(contexto).isEmpty()
}

private fun permisosLlamadaPendientes(
    contexto: Context
): Array<String> {
    return arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    ).filter { permiso ->
        ContextCompat.checkSelfPermission(
            contexto,
            permiso
        ) != PackageManager.PERMISSION_GRANTED
    }.toTypedArray()
}