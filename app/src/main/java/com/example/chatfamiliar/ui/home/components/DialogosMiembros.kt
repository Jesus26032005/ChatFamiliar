package com.example.chatfamiliar.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier


@Composable
fun DialogoQuitarAdministrador(
    visible: Boolean,
    nombreMiembro: String,

    titulo: String,
    descripcion: String,

    textoConfirmar: String,
    textoCancelar: String,

    mensajeError: String?,

    cargando: Boolean,

    alConfirmar: () -> Unit,
    alCancelar: () -> Unit
) {

    if (!visible) {
        return
    }


    DialogoConfirmacionMiembro(
        nombreMiembro =
            nombreMiembro,

        titulo =
            titulo,

        descripcion =
            descripcion,

        textoConfirmar =
            textoConfirmar,

        textoCancelar =
            textoCancelar,

        mensajeError =
            mensajeError,

        cargando =
            cargando,

        alConfirmar =
            alConfirmar,

        alCancelar =
            alCancelar
    )
}


@Composable
fun DialogoExpulsarMiembro(
    visible: Boolean,
    nombreMiembro: String,

    titulo: String,
    descripcion: String,

    textoConfirmar: String,
    textoCancelar: String,

    mensajeError: String?,

    cargando: Boolean,

    alConfirmar: () -> Unit,
    alCancelar: () -> Unit
) {

    if (!visible) {
        return
    }


    DialogoConfirmacionMiembro(
        nombreMiembro =
            nombreMiembro,

        titulo =
            titulo,

        descripcion =
            descripcion,

        textoConfirmar =
            textoConfirmar,

        textoCancelar =
            textoCancelar,

        mensajeError =
            mensajeError,

        cargando =
            cargando,

        alConfirmar =
            alConfirmar,

        alCancelar =
            alCancelar
    )
}


@Composable
private fun DialogoConfirmacionMiembro(
    nombreMiembro: String,
    titulo: String,
    descripcion: String,
    textoConfirmar: String,
    textoCancelar: String,
    mensajeError: String?,
    cargando: Boolean,
    alConfirmar: () -> Unit,
    alCancelar: () -> Unit
) {

    AlertDialog(
        onDismissRequest = { if (!cargando) { alCancelar() } },
        title = { Text(text = titulo, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(text = descripcion,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = nombreMiembro,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                if (mensajeError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = mensajeError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = alConfirmar,
                enabled = !cargando ){
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = textoConfirmar)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = alCancelar,
                enabled = !cargando
            ) {
                Text(text = textoCancelar)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Preview(
    showBackground = true,
    name = "Quitar administrador"
)
@Composable
private fun DialogoQuitarAdministradorPreview() {

    MaterialTheme {

        DialogoQuitarAdministrador(
            visible = true,
            nombreMiembro = "María",
            titulo = "Quitar administrador",
            descripcion =
                "El integrante conservará acceso a la familia, pero volverá a tener permisos de miembro.",
            textoConfirmar =
                "Quitar administrador",
            textoCancelar =
                "Cancelar",
            mensajeError =
                null,
            cargando =
                false,
            alConfirmar =
                {},
            alCancelar =
                {}
        )
    }
}


@Preview(
    showBackground = true,
    name = "Expulsar integrante"
)
@Composable
private fun DialogoExpulsarMiembroPreview() {

    MaterialTheme {

        DialogoExpulsarMiembro(
            visible = true,
            nombreMiembro = "Carlos",
            titulo = "Expulsar integrante",
            descripcion =
                "Este integrante dejará de pertenecer a la familia.",
            textoConfirmar =
                "Expulsar",
            textoCancelar =
                "Cancelar",
            mensajeError =
                null,
            cargando =
                false,
            alConfirmar =
                {},
            alCancelar =
                {}
        )
    }
}