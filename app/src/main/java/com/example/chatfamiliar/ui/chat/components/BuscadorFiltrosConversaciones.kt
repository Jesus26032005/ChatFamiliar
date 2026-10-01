package com.example.chatfamiliar.ui.chat.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.R
import com.example.chatfamiliar.ui.chat.FiltroConversaciones
import com.example.chatfamiliar.ui.language.TraducirTexto
import com.example.chatfamiliar.ui.language.recordarTextosApp
import com.example.chatfamiliar.ui.theme.funcional

@Composable
fun BuscadorFiltrosConversaciones(
    busqueda: String, filtroSeleccionado: FiltroConversaciones,
    hayConteosPendientes: Boolean, idiomaEfectivo: String,
    traducir: TraducirTexto, alCambiarBusqueda: (String) -> Unit,
    alLimpiarBusqueda: () -> Unit,
    alSeleccionarFiltro: (FiltroConversaciones) -> Unit, modifier: Modifier = Modifier) {
    val textos = recordarTextosApp(idiomaEfectivo = idiomaEfectivo,
        traducir = traducir)
    val focusManager = LocalFocusManager.current
    val etiquetaBusqueda = textos.texto(R.string.chat_search_conversations)
    val descripcionFiltros = textos.texto(R.string.chat_filters_description)
    val funcional = MaterialTheme.funcional
    Column(modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = busqueda,
            onValueChange = alCambiarBusqueda,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            label = { Text(text = etiquetaBusqueda) },
            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (busqueda.isNotEmpty()) {
                    IconButton(onClick = alLimpiarBusqueda
                    ) {
                        Icon(imageVector = Icons.Filled.Close,
                            contentDescription = textos.texto(
                                R.string.chat_search_clear)) } } },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { focusManager.clearFocus() }))
        Row(modifier = Modifier
            .fillMaxWidth().horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp).semantics {
                contentDescription = descripcionFiltros },
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FiltroConversaciones.entries.forEach { filtro ->
                val seleccionado = filtro == filtroSeleccionado
                FilterChip(
                    selected = seleccionado,
                    onClick = { alSeleccionarFiltro(filtro)
                        focusManager.clearFocus() },
                    label = { Text(text = textos.texto(filtro.textoRecurso))
                    },
                    leadingIcon = { if (seleccionado) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = null) } },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = funcional.filtroSeleccionado,
                        selectedLabelColor = funcional.sobreFiltroSeleccionado,
                        selectedLeadingIconColor = funcional.sobreFiltroSeleccionado),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true, selected = seleccionado,
                        selectedBorderColor = funcional.bordeFiltroSeleccionado,
                        selectedBorderWidth = 1.dp)) } }
        if (filtroSeleccionado == FiltroConversaciones.NO_LEIDOS &&
            hayConteosPendientes) {
            Text(text = textos.texto(R.string.chat_unread_pending),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = funcional.acento) }
    }
}