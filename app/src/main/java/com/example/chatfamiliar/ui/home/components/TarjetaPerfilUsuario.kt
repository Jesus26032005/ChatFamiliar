package com.example.chatfamiliar.ui.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.chatfamiliar.ui.family.components.EstadoSinFamilia

@Composable
fun TarjetaPerfilUsuario(
    nombre: String,
    correo: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme
                .surfaceContainer
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth()
            .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme
                    .secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = MaterialTheme.colorScheme
                            .onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = nombre.ifBlank { correo },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Text(text = correo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme
                            .onSurfaceVariant)
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "Usuario sin familia"
)
@Composable
private fun EstadoSinFamiliaPreview() {
    MaterialTheme {
        EstadoSinFamilia(
            titulo = "Aún no perteneces a una familia",
            descripcion = "Crea una nueva familia o únete a una existente mediante su código de invitación.",
            textoCrear = "Crear familia",
            textoUnirse = "Unirme con código",
            alCrearFamilia = {},
            alUnirseFamilia = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}