package com.example.chatfamiliar.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AccionesFamilia(
    textoCrear: String,
    textoUnirse: String,
    alCrearFamilia: () -> Unit,
    alUnirseFamilia: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(modifier = modifier.fillMaxWidth())
    {
        OutlinedButton(
            onClick = alCrearFamilia,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = textoCrear,
                fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = alUnirseFamilia,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = textoUnirse, fontWeight = FontWeight.SemiBold)
        }
    }
}
@Preview(
    showBackground = true,
    name = "Acciones de familia"
)
@Composable
private fun AccionesFamiliaPreview() {
    MaterialTheme {
        AccionesFamilia(
            textoCrear =
                "Crear otra familia",
            textoUnirse =
                "Unirme a otra familia",
            alCrearFamilia =
                {},
            alUnirseFamilia =
                {},
            modifier =
                Modifier.padding(16.dp)
        )
    }
}