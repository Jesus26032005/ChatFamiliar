package com.example.chatfamiliar.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.chatfamiliar.R
import com.example.chatfamiliar.model.Familia
import com.example.chatfamiliar.model.MiembroFamilia
import com.example.chatfamiliar.model.Usuario
import com.example.chatfamiliar.ui.home.components.CodigoInvitacionFamilia
import com.example.chatfamiliar.ui.home.components.DialogoNombreInicial
import com.example.chatfamiliar.ui.home.components.EstadoCargaHome
import com.example.chatfamiliar.ui.home.components.SelectorFamiliaActiva
import com.example.chatfamiliar.ui.home.components.TarjetaPerfilUsuario
import com.example.chatfamiliar.ui.language.ControlIdiomaCompacto
import com.example.chatfamiliar.ui.language.IdiomaViewModel
import com.example.chatfamiliar.ui.language.TraducirTexto
import com.example.chatfamiliar.ui.language.recordarIdiomaEfectivo
import com.example.chatfamiliar.ui.language.recordarTextosApp

@Composable
fun PantallaHome(
    idiomaViewModel: IdiomaViewModel,
    viewModel: HomeViewModel,
    alGestionarFamilia: () -> Unit,
    alCerrarSesion: () -> Unit
) {
    LaunchedEffect(Unit) {
        if (viewModel.usuario == null && !viewModel.cargando) {
            viewModel.cargarUsuario() } }
    val idiomaEfectivo = recordarIdiomaEfectivo(
        idiomaViewModel.idiomaSeleccionado)

    ContenidoHome(
        usuario = viewModel.usuario,
        familias = viewModel.familias,
        familiaActiva = viewModel.familiaActiva,
        membresiaActiva = viewModel.membresiaActiva,
        nombreNuevo = viewModel.nombreNuevo,
        necesitaNombre = viewModel.necesitaNombre,
        cargando = viewModel.cargando,
        cargandoFamilia = viewModel.cargandoFamilia,
        guardandoNombre = viewModel.guardandoNombre,
        errorRecurso = viewModel.errorRecurso,
        idiomaSeleccionado = idiomaViewModel.idiomaSeleccionado,
        idiomaEfectivo = idiomaEfectivo,
        traducir = idiomaViewModel::traducir,
        alSeleccionarIdioma = idiomaViewModel::seleccionarIdioma,
        alCambiarNombre = viewModel::actualizarNombre,
        alGuardarNombre = viewModel::guardarNombre,
        alRecargar = viewModel::recargarHome,
        alSeleccionarFamilia = viewModel::seleccionarFamilia,
        alGestionarFamilia = alGestionarFamilia,
        alCerrarSesion = { viewModel.cerrarSesion(alCerrarSesion = alCerrarSesion) }
    )
}

@Composable
private fun ContenidoHome(
    usuario: Usuario?,
    familias: List<Familia>,
    familiaActiva: Familia?,
    membresiaActiva: MiembroFamilia?,
    nombreNuevo: String,
    necesitaNombre: Boolean,
    cargando: Boolean,
    cargandoFamilia: Boolean,
    guardandoNombre: Boolean,
    @StringRes errorRecurso: Int?,
    idiomaSeleccionado: String?,
    idiomaEfectivo: String,
    traducir: TraducirTexto,
    alSeleccionarIdioma: (String?) -> Unit,
    alCambiarNombre: (String) -> Unit,
    alGuardarNombre: () -> Unit,
    alRecargar: () -> Unit,
    alSeleccionarFamilia: (Familia) -> Unit,
    alGestionarFamilia: () -> Unit,
    alCerrarSesion: () -> Unit
) {
    val textos = recordarTextosApp(
        idiomaEfectivo = idiomaEfectivo,
        traducir = traducir
    )
    val mensajeErrorGeneral =
        errorRecurso?.let { recurso -> textos.texto(recurso)
        }
    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()
    ) {
        if (cargando && usuario == null) {
            EstadoCargaHome(
                titulo = textos.texto(R.string.home_loading_title),
                descripcion = textos.texto(
                    R.string.home_loading_description))
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Filled.Forum,
                                contentDescription = null,
                                modifier = Modifier.size(27.dp),
                                tint = MaterialTheme.colorScheme.primary)
                        } }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)
                    ) {
                        Text(text = textos.texto(R.string.home_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = usuario?.correo.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(
                        onClick = alRecargar,
                        enabled = !cargando
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = textos.texto(
                                    R.string.home_refresh),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    ControlIdiomaCompacto(
                        idiomaSeleccionado = idiomaSeleccionado,
                        idiomaEfectivo = idiomaEfectivo,
                        traducir = traducir,
                        alSeleccionarIdioma = alSeleccionarIdioma)
                }

                Spacer(modifier = Modifier.height(28.dp))
                if (cargando && usuario != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(modifier = Modifier.padding(
                                horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = textos.texto(
                                    R.string.home_refreshing),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Card(modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(22.dp)
                    ) {
                        Text(text = if (usuario?.nombre.isNullOrBlank()) {
                                textos.texto(R.string.home_welcome)
                            } else {
                                "${textos.texto(R.string.home_welcome)}," +
                                        " ${usuario?.nombre}" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = textos.texto(R.string.home_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                usuario?.let { perfil ->
                    TarjetaPerfilUsuario(
                        nombre = perfil.nombre,
                        correo = perfil.correo)
                }
                Spacer(modifier = Modifier.height(20.dp))
                if (mensajeErrorGeneral != null &&
                    !necesitaNombre
                ) {
                    Surface(modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(text = mensajeErrorGeneral,
                            modifier = Modifier.padding(14.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                when {
                    cargandoFamilia && familias.isEmpty() -> {
                        Column(modifier = Modifier
                                .fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = textos.texto(
                                R.string.home_family_loading),
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    familias.isEmpty() -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme
                                    .colorScheme.surfaceContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = textos.texto(
                                        R.string.home_no_family_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = textos.texto(
                                        R.string
                                            .home_no_family_description),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(18.dp))
                                OutlinedButton(
                                    onClick = alGestionarFamilia,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(text = textos.texto(R.string.home_manage_family),
                                        fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    familiaActiva != null -> {
                        val textoRol =
                            when (membresiaActiva?.rol) {
                                MiembroFamilia.ROL_ADMINISTRADOR ->
                                    textos.texto(R.string.home_role_admin)
                                MiembroFamilia.ROL_MIEMBRO ->
                                    textos.texto(R.string.home_role_member)
                                else -> null
                            }
                        SelectorFamiliaActiva(
                            familias = familias,
                            familiaActiva = familiaActiva,
                            titulo = textos.texto(
                                R.string.home_family_active),
                            textoRol = textoRol,
                            cargando = cargandoFamilia,
                            alSeleccionarFamilia = alSeleccionarFamilia)
                        Spacer(modifier = Modifier.height(16.dp))
                        CodigoInvitacionFamilia(
                            codigo = familiaActiva.codigoInvitacion,
                            titulo = textos.texto(
                                R.string.home_invitation_code_title),
                            textoCopiar = textos.texto(R.string.home_invitation_code_copy),
                            textoCopiado = textos.texto(R.string.home_invitation_code_copied))
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = alGestionarFamilia,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text(text = textos.texto(
                            R.string.home_manage_family),
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedButton(
                    onClick = alCerrarSesion,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Logout,
                        contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = textos.texto(R.string.home_logout),
                        fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    DialogoNombreInicial(
        visible = necesitaNombre,
        nombre = nombreNuevo,
        titulo = textos.texto(R.string.home_name_title),
        descripcion = textos.texto(R.string.home_name_description),
        etiquetaNombre = textos.texto(R.string.home_name_label),
        textoGuardar = textos.texto(R.string.home_name_save),
        mensajeError = if (necesitaNombre) { mensajeErrorGeneral
        } else { null },
        guardando = guardandoNombre,
        alCambiarNombre = alCambiarNombre,
        alGuardarNombre = alGuardarNombre
    )
}
