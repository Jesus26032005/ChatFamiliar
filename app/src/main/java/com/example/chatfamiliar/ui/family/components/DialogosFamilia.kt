package com.example.chatfamiliar.ui.family.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun DialogoCrearFamilia(
    visible: Boolean,
    nombre: String,
    titulo: String,
    descripcion: String,
    etiqueta: String,
    textoConfirmar: String,
    textoCancelar: String,
    mensajeError: String?,
    cargando: Boolean,
    alCambiarNombre: (String) -> Unit,
    alConfirmar: () -> Unit,
    alCancelar: () -> Unit
){
    if (!visible) { return }
    DialogoEntradaFamilia(
        titulo = titulo,
        descripcion = descripcion,
        valor = nombre,
        etiqueta = etiqueta,
        textoConfirmar = textoConfirmar,
        textoCancelar = textoCancelar,
        mensajeError = mensajeError,
        cargando = cargando,
        alCambiarValor = alCambiarNombre,
        alConfirmar = alConfirmar,
        alCancelar = alCancelar)
}

@Composable
fun DialogoUnirseFamilia(
    visible: Boolean,
    codigo: String,
    titulo: String,
    descripcion: String,
    etiqueta: String,
    textoConfirmar: String,
    textoCancelar: String,
    mensajeError: String?,
    cargando: Boolean,
    alCambiarCodigo: (String) -> Unit,
    alConfirmar: () -> Unit,
    alCancelar: () -> Unit
) {
    if (!visible) { return }
    DialogoEntradaFamilia(
        titulo = titulo,
        descripcion = descripcion,
        valor = codigo,
        etiqueta = etiqueta,
        textoConfirmar = textoConfirmar,
        textoCancelar = textoCancelar,
        mensajeError = mensajeError,
        cargando = cargando,
        alCambiarValor = alCambiarCodigo,
        alConfirmar = alConfirmar,
        alCancelar = alCancelar
    )
}

@Composable
private fun DialogoEntradaFamilia(
    titulo: String,
    descripcion: String,
    valor: String,
    etiqueta: String,
    textoConfirmar: String,
    textoCancelar: String,
    mensajeError: String?,
    cargando: Boolean,
    alCambiarValor: (String) -> Unit,
    alConfirmar: () -> Unit,
    alCancelar: () -> Unit
) {
    AlertDialog(onDismissRequest = {
            if (!cargando) { alCancelar() }
        },
        title = { Text(text = titulo, fontWeight = FontWeight.Bold)
        },
        text = { Column {
                Text(text = descripcion)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = valor,
                    onValueChange = alCambiarValor,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(etiqueta) },
                    enabled = !cargando,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp))
                if (mensajeError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = mensajeError,
                        color = MaterialTheme.colorScheme
                                .error)
                }
            }
        },
        confirmButton = {
            Button(onClick = alConfirmar, enabled = !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(textoConfirmar)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = alCancelar, enabled = !cargando
            ) { Text(textoCancelar) } },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun DialogoEditarFamilia(
    visible: Boolean,
    nombre: String,
    titulo: String,
    descripcion: String,
    etiqueta: String,
    textoConfirmar: String,
    textoCancelar: String,
    mensajeError: String?,
    cargando: Boolean,
    alCambiarNombre: (String) -> Unit,
    alConfirmar: () -> Unit,
    alCancelar: () -> Unit
) {
    if (!visible) { return }
    DialogoEntradaFamilia(
        titulo = titulo,
        descripcion = descripcion,
        valor = nombre,
        etiqueta = etiqueta,
        textoConfirmar = textoConfirmar,
        textoCancelar = textoCancelar,
        mensajeError = mensajeError,
        cargando = cargando,
        alCambiarValor = alCambiarNombre,
        alConfirmar = alConfirmar,
        alCancelar = alCancelar
    )
}

@Composable
fun DialogoAbandonarFamilia(
    visible: Boolean,
    nombreFamilia: String,
    titulo: String,
    descripcion: String,
    textoConfirmar: String,
    textoCancelar: String,
    mensajeError: String?,
    cargando: Boolean,
    alConfirmar: () -> Unit,
    alCancelar: () -> Unit
) {
    if (!visible) { return }
    AlertDialog(
        onDismissRequest = { if (!cargando) { alCancelar() } },
        title = {
            Text(text = titulo, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(text = descripcion)
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = nombreFamilia,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                if (mensajeError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = mensajeError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        },

        confirmButton = {
            Button(onClick = alConfirmar, enabled = !cargando) {
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
            OutlinedButton(onClick = alCancelar, enabled = !cargando) {
                Text(text = textoCancelar) }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun DialogoEliminarFamilia(
    visible: Boolean,
    nombreFamilia: String,
    titulo: String,
    descripcion: String,
    textoConfirmar: String,
    textoCancelar: String,
    mensajeError: String?,
    cargando: Boolean,
    alConfirmar: () -> Unit,
    alCancelar: () -> Unit
) {
    if (!visible) { return }
    AlertDialog(
        onDismissRequest = { if (!cargando) { alCancelar() } },
        title = { Text(
                text = titulo,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error)
        },
        text = {
            Column {
                Text(text = descripcion,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = nombreFamilia,
                    fontWeight = FontWeight.Bold)
                if (mensajeError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = mensajeError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = alConfirmar,
                enabled = !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = textoConfirmar)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = alCancelar,
                enabled = !cargando) {
                Text(text = textoCancelar)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}


@Preview(
    showBackground = true,
    name = "Diálogo crear familia"
)
@Composable
private fun DialogoCrearFamiliaPreview() {

    MaterialTheme {

        DialogoCrearFamilia(
            visible = true,
            nombre = "Familia Martínez",
            titulo = "Crear una familia",
            descripcion = "Escribe un nombre para identificar a tu familia.",
            etiqueta = "Nombre de la familia",
            textoConfirmar = "Crear familia",
            textoCancelar = "Cancelar",
            mensajeError = null,
            cargando = false,
            alCambiarNombre = {},
            alConfirmar = {},
            alCancelar = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "Diálogo unirse a familia"
)
@Composable
private fun DialogoUnirseFamiliaPreview() {
    MaterialTheme {
        DialogoUnirseFamilia(
            visible = true,
            codigo = "FAM-A1B2C3",
            titulo = "Unirme a una familia",
            descripcion = "Ingresa el código de invitación proporcionado" +
                    " por un integrante de la familia.",
            etiqueta = "Código de invitación",
            textoConfirmar = "Unirme con código",
            textoCancelar = "Cancelar",
            mensajeError = null,
            cargando = false,
            alCambiarCodigo = {},
            alConfirmar = {},
            alCancelar = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "Editar familia"
)
@Composable
private fun DialogoEditarFamiliaPreview() {

    MaterialTheme {

        DialogoEditarFamilia(
            visible = true,
            nombre = "Familia Martínez",
            titulo = "Editar familia",
            descripcion = "Modifica el nombre de tu familia.",
            etiqueta = "Nombre de la familia",
            textoConfirmar = "Guardar cambios",
            textoCancelar = "Cancelar",
            mensajeError = null,
            cargando = false,
            alCambiarNombre = {},
            alConfirmar = {},
            alCancelar = {}
        )
    }
}


@Preview(
    showBackground = true,
    name = "Abandonar familia"
)
@Composable
private fun DialogoAbandonarFamiliaPreview() {
    MaterialTheme {
        DialogoAbandonarFamilia(
            visible = true,
            nombreFamilia = "Familia Martínez",
            titulo = "Abandonar familia",
            descripcion =
                "Dejarás de tener acceso al chat y contenido de esta familia.",
            textoConfirmar = "Abandonar",
            textoCancelar = "Cancelar",
            cargando = false,
            alConfirmar = {},
            alCancelar = {},
            mensajeError = null,
        )
    }
}