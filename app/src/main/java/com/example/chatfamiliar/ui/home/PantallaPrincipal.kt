package com.example.chatfamiliar.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.chatfamiliar.R
import com.example.chatfamiliar.model.ConversacionResumen
import com.example.chatfamiliar.ui.chat.PantallaInicioConversaciones
import com.example.chatfamiliar.ui.chat.components.EstadoCargaConversaciones
import com.example.chatfamiliar.ui.chat.components.EstadoErrorConversaciones
import com.example.chatfamiliar.ui.family.PantallaListadoFamilias
import com.example.chatfamiliar.ui.home.components.DialogoNombreInicial
import com.example.chatfamiliar.ui.language.IdiomaViewModel
import com.example.chatfamiliar.ui.language.recordarIdiomaEfectivo
import com.example.chatfamiliar.ui.language.recordarTextosApp
import com.example.chatfamiliar.ui.profile.PantallaPerfil
import com.example.chatfamiliar.ui.theme.funcional

@Composable
fun PantallaPrincipal(
    idiomaViewModel: IdiomaViewModel,
    homeViewModel: HomeViewModel,
    alAbrirConversacion: (ConversacionResumen) -> Unit,
    alNuevoMensaje: () -> Unit,
    alGestionarFamilia: (String?) -> Unit,
    alCerrarSesion: () -> Unit
) {
    val idiomaEfectivo = recordarIdiomaEfectivo(
        idiomaViewModel.idiomaSeleccionado)
    val textos = recordarTextosApp(idiomaEfectivo = idiomaEfectivo,
        traducir = idiomaViewModel::traducir)
    LaunchedEffect(homeViewModel) {
        if (homeViewModel.usuario == null &&
            !homeViewModel.cargando) {
            homeViewModel.cargarUsuario() } }
    val usuario = homeViewModel.usuario
    if (usuario == null) {
        val error = homeViewModel.errorRecurso
        Box(modifier = Modifier.fillMaxSize()
            .safeDrawingPadding(),
            contentAlignment = Alignment.Center) {
            if (homeViewModel.cargando || error == null) {
                EstadoCargaConversaciones(
                    titulo = textos.texto(R.string.home_loading_title),
                    descripcion = textos.texto(R.string.home_loading_description))
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    EstadoErrorConversaciones(
                        titulo = textos.texto(R.string.main_profile_error_title),
                        mensaje = textos.texto(error),
                        textoReintentar = textos.texto(R.string.chat_item_retry),
                        alReintentar = homeViewModel::recargarHome)
                    TextButton(onClick = {
                        homeViewModel.cerrarSesion(alCerrarSesion = alCerrarSesion) }) {
                        Text(text = textos.texto(R.string.home_logout)) } } } }
        return }
    // Error del nombre: lo usan el diálogo inicial y el de editar en Perfil.
    val errorNombre = homeViewModel.errorRecurso?.takeIf {
        it == R.string.home_error_empty_name || it == R.string.home_error_save_name }
    val textoErrorNombre = errorNombre?.let { recurso -> textos.texto(recurso) }
    var seccionSeleccionada by rememberSaveable(usuario.uid) {
        mutableStateOf(SeccionPrincipal.INICIO) }
    Scaffold(
        bottomBar = {
            val coloresPestana = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.funcional.encabezado,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer)
            NavigationBar {
                NavigationBarItem(
                    selected = seccionSeleccionada == SeccionPrincipal.INICIO,
                    onClick = { seccionSeleccionada = SeccionPrincipal.INICIO },
                    colors = coloresPestana,
                    icon = { Icon(imageVector = Icons.Filled.Forum, contentDescription = null) },
                    label = { Text(text = textos.texto(R.string.main_tab_home)) })
                NavigationBarItem(
                    selected = seccionSeleccionada == SeccionPrincipal.FAMILIAS,
                    onClick = { seccionSeleccionada = SeccionPrincipal.FAMILIAS },
                    colors = coloresPestana,
                    icon = { Icon(imageVector = Icons.Filled.Groups, contentDescription = null) },
                    label = { Text(text = textos.texto(
                        R.string.families_section_title))})
                NavigationBarItem(
                    selected = seccionSeleccionada == SeccionPrincipal.PERFIL,
                    onClick = { seccionSeleccionada = SeccionPrincipal.PERFIL },
                    colors = coloresPestana,
                    icon = { Icon(imageVector = Icons.Filled.Person, contentDescription = null) },
                    label = { Text(text = textos.texto(R.string.profile_title)) }) } }
    ) { espacios ->
        Box(modifier = Modifier.fillMaxSize()
            .padding(espacios).consumeWindowInsets(espacios)) {
            when (seccionSeleccionada) {
                SeccionPrincipal.INICIO -> {
                    PantallaInicioConversaciones(
                        uidActual = usuario.uid,
                        familiasConfirmadas = homeViewModel.familiasConfirmadas,
                        cargandoFamilias = homeViewModel.cargandoListadoFamilias ||
                                (homeViewModel.familiasConfirmadas == null &&
                                        homeViewModel.cargando),
                        errorFamiliasRecurso = homeViewModel.errorFamiliasRecurso,
                        idiomaSeleccionado = idiomaViewModel.idiomaSeleccionado,
                        idiomaEfectivo = idiomaEfectivo,
                        traducir = idiomaViewModel::traducir,
                        alSeleccionarIdioma = idiomaViewModel::seleccionarIdioma,
                        alAbrirConversacion = alAbrirConversacion,
                        alReintentarFamilias = homeViewModel::recargarFamilias,
                        alVerFamilias = { seccionSeleccionada = SeccionPrincipal.FAMILIAS },
                        alNuevoMensaje = alNuevoMensaje) }
                SeccionPrincipal.FAMILIAS -> {
                    PantallaListadoFamilias(
                        familiasConfirmadas = homeViewModel.familiasConfirmadas,
                        cargando = homeViewModel.cargandoListadoFamilias,
                        errorRecurso = homeViewModel.errorFamiliasRecurso,
                        idiomaSeleccionado = idiomaViewModel.idiomaSeleccionado,
                        idiomaEfectivo = idiomaEfectivo,
                        traducir = idiomaViewModel::traducir,
                        alSeleccionarIdioma = idiomaViewModel::seleccionarIdioma,
                        alGestionarFamilia = { familia ->
                            homeViewModel.seleccionarFamilia(familia)
                            alGestionarFamilia(familia.id) },
                        alCrearOUnirse = { alGestionarFamilia(null) },
                        alRecargar = homeViewModel::recargarFamilias) }
                SeccionPrincipal.PERFIL -> {
                    PantallaPerfil(usuario = usuario,
                        idiomaSeleccionado = idiomaViewModel.idiomaSeleccionado,
                        idiomaEfectivo = idiomaEfectivo,
                        traducir = idiomaViewModel::traducir,
                        alSeleccionarIdioma = idiomaViewModel::seleccionarIdioma,
                        editandoNombre = homeViewModel.editandoNombre,
                        nombreEditado = homeViewModel.nombreNuevo,
                        guardandoNombre = homeViewModel.guardandoNombre,
                        mensajeErrorNombre = textoErrorNombre,
                        alEditarNombre = homeViewModel::empezarEdicionNombre,
                        alCambiarNombre = homeViewModel::actualizarNombre,
                        alGuardarNombre = homeViewModel::guardarNombre,
                        alCancelarEdicionNombre = homeViewModel::cancelarEdicionNombre,
                        alCerrarSesion = { homeViewModel.cerrarSesion(
                            alCerrarSesion = alCerrarSesion) }) } } } }
    DialogoNombreInicial(
        visible = homeViewModel.necesitaNombre,
        nombre = homeViewModel.nombreNuevo,
        titulo = textos.texto(R.string.home_name_title),
        descripcion = textos.texto(R.string.home_name_description),
        etiquetaNombre = textos.texto(R.string.home_name_label),
        textoGuardar = textos.texto(R.string.home_name_save),
        mensajeError = textoErrorNombre,
        guardando = homeViewModel.guardandoNombre,
        alCambiarNombre = homeViewModel::actualizarNombre,
        alGuardarNombre = homeViewModel::guardarNombre)
}

private enum class SeccionPrincipal {
    INICIO,
    FAMILIAS,
    PERFIL
}