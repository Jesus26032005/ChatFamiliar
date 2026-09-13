package com.example.chatfamiliar.ui.home.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun CodigoInvitacionFamilia(
    codigo: String,
    titulo: String,
    textoCopiar: String,
    textoCopiado: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically
            ) { Icon(imageVector = Icons.Filled.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.padding(6.dp))
            Text(text = codigo,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.padding(6.dp))
            OutlinedButton(
                onClick = { copiarCodigo(context = context, codigo = codigo)
                    Toast.makeText(context,
                        textoCopiado, Toast.LENGTH_SHORT).show() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = textoCopiar, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}


private fun copiarCodigo(
    context: Context,
    codigo: String
) {
    val portapapeles = context.getSystemService(Context.CLIPBOARD_SERVICE
    ) as ClipboardManager

    val clip = ClipData.newPlainText("Código de invitación", codigo)
    portapapeles.setPrimaryClip(clip
    )
}


@Preview(
    showBackground = true,
    name = "Código de invitación"
)
@Composable
private fun CodigoInvitacionFamiliaPreview() {
    MaterialTheme {
        CodigoInvitacionFamilia(
            codigo = "FAM-A1B2C3",
            titulo = "Código de invitación",
            textoCopiar = "Copiar código",
            textoCopiado = "Código copiado",
            modifier = Modifier.padding(16.dp)
        )
    }
}