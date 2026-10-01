package com.example.chatfamiliar.ui.comun

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.ui.theme.ChatFamiliarTheme


@Composable
private fun MuestraPaleta() {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AvatarIdentidad(semilla = "familia-1", icono = Icons.Filled.Groups, esGrupo = true)
            AvatarIdentidad(semilla = "uid-laura", nombre = "Laura Gómez")
            AvatarIdentidad(semilla = "uid-carlos", nombre = "Carlos Ruiz")
            AvatarIdentidad(semilla = "uid-rosa", nombre = "Rosa Martínez")
            AvatarIdentidad(semilla = "familia-2", icono = Icons.Filled.Groups, esGrupo = true)
        }
        BotonDestructivo(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Icon(imageVector = Icons.Filled.DeleteOutline, contentDescription = null)
            Text(text = "Expulsar", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Preview(name = "Paleta - claro", showBackground = true)
@Composable
private fun MuestraPaletaClaroPreview() {
    ChatFamiliarTheme(darkTheme = false) { MuestraPaleta() }
}

@Preview(name = "Paleta - oscuro", showBackground = true)
@Composable
private fun MuestraPaletaOscuroPreview() {
    ChatFamiliarTheme(darkTheme = true) { MuestraPaleta() }
}