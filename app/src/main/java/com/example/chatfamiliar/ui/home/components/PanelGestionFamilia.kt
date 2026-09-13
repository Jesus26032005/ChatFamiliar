package com.example.chatfamiliar.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun PanelGestionFamilia(
    esAdministrador: Boolean,
    esAdministradorPrincipal: Boolean,
    titulo: String,
    textoEditar: String,
    textoAbandonar: String,
    textoEliminar: String,
    alEditar: () -> Unit,
    alAbandonar: () -> Unit,
    alEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp)
        ) {
            Text(text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (esAdministrador) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = alEditar,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Edit,
                        contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = textoEditar)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = alAbandonar,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Filled.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = textoAbandonar,
                    color = MaterialTheme.colorScheme.error)
            }

            if (esAdministradorPrincipal) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(onClick = alEliminar,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Filled.DeleteForever,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = textoEliminar,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


@Preview(
    showBackground = true,
    name = "Administrador principal"
)
@Composable
private fun PanelGestionFamiliaPrincipalPreview() {
    MaterialTheme {
        PanelGestionFamilia(
            esAdministrador = true,
            esAdministradorPrincipal = true,
            titulo = "Configuración de familia",
            textoEditar = "Editar familia",
            textoAbandonar = "Abandonar familia",
            textoEliminar = "Eliminar familia",
            alEditar = {},
            alAbandonar = {},
            alEliminar = {},
            modifier =
                Modifier.padding(16.dp)
        )
    }
}