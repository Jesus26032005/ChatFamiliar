package com.example.chatfamiliar.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chatfamiliar.data.auth.AuthRepository
import com.example.chatfamiliar.model.ConversacionResumen
import com.example.chatfamiliar.ui.auth.login.PantallaLogin
import com.example.chatfamiliar.ui.auth.register.PantallaRegistro
import com.example.chatfamiliar.ui.auth.verification.PantallaVerificacion
import com.example.chatfamiliar.ui.chat.PantallaChatFamilia
import com.example.chatfamiliar.ui.chat.PantallaChatPrivado
import com.example.chatfamiliar.ui.chat.PantallaNuevoMensaje
import com.example.chatfamiliar.ui.family.FamiliaViewModel
import com.example.chatfamiliar.ui.family.PantallaFamilia
import com.example.chatfamiliar.ui.home.HomeViewModel
import com.example.chatfamiliar.ui.home.PantallaPrincipal
import com.example.chatfamiliar.ui.language.IdiomaViewModel
import com.example.chatfamiliar.ui.language.IndicadorTraduccion

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authRepository = remember { AuthRepository() }
    val idiomaViewModel: IdiomaViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val rutaInicial = remember(authRepository) {
        val usuarioActual = authRepository.obtenerUsuarioActual()
        when {usuarioActual == null -> Rutas.LOGIN
            usuarioActual.isEmailVerified -> Rutas.HOME
            else -> Rutas.VERIFICACION } }
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController,
            startDestination = rutaInicial) {
            composable(route = Rutas.LOGIN) {
                PantallaLogin(idiomaViewModel = idiomaViewModel,
                    alNavegarHome = {
                        navController.navigate(Rutas.HOME) {
                            popUpTo(navController.graph.id) { inclusive = false }
                            launchSingleTop = true } },
                    alNavegarRegistro = {
                        navController.navigate(Rutas.REGISTRO) {
                            launchSingleTop = true } },
                    alNavegarVerificacion = {
                        navController.navigate(Rutas.VERIFICACION) {
                            popUpTo(navController.graph.id) { inclusive = false }
                            launchSingleTop = true } }) }
            composable(route = Rutas.REGISTRO) {
                PantallaRegistro(idiomaViewModel = idiomaViewModel,
                    alNavegarVerificacion = {
                        navController.navigate(Rutas.VERIFICACION) {
                            popUpTo(navController.graph.id) { inclusive = false }
                            launchSingleTop = true } },
                    alNavegarLogin = { navController.popBackStack() }) }
            composable(route = Rutas.VERIFICACION) {
                PantallaVerificacion(
                    idiomaViewModel = idiomaViewModel,
                    alNavegarHome = { navController.navigate(Rutas.HOME) {
                            popUpTo(navController.graph.id) { inclusive = false }
                            launchSingleTop = true } },
                    alNavegarLogin = { navController.navigate(Rutas.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = false }
                            launchSingleTop = true } }) }
            composable(route = Rutas.HOME) {
                PantallaPrincipal(idiomaViewModel = idiomaViewModel,
                    homeViewModel = homeViewModel,
                    alAbrirConversacion = { conversacion ->
                        when (conversacion.tipo) {
                            ConversacionResumen.TIPO_FAMILIA -> {
                                val familiaId = conversacion.familiaId.ifBlank {
                                        conversacion.id }
                                navController.navigate(
                                    Rutas.crearRutaChatFamilia(
                                        familiaId = familiaId,
                                        nombre = conversacion.titulo)) {
                                    launchSingleTop = true } }
                            ConversacionResumen.TIPO_PRIVADO -> {
                                navController.navigate(
                                    Rutas.crearRutaChatPrivado(
                                        conversacion.id)) {
                                    launchSingleTop = true } } } },
                    alNuevoMensaje = {
                        navController.navigate(Rutas.NUEVO_MENSAJE) {
                            launchSingleTop = true } },
                    alGestionarFamilia = { familiaId ->
                        navController.navigate(
                            Rutas.crearRutaGestionFamilia(familiaId)
                        ) { launchSingleTop = true } },
                    alCerrarSesion = { navController.navigate(Rutas.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = false }
                            launchSingleTop = true } }) }
            composable(route = Rutas.GESTION_FAMILIA_CON_ARGUMENTOS,
                arguments = listOf(
                    navArgument("familiaId") { type = NavType.StringType
                        nullable = true
                        defaultValue = null })) { entrada ->
                val familiaViewModel: FamiliaViewModel = viewModel()
                val familiaId = entrada.arguments?.getString("familiaId")
                    ?.takeIf { it.isNotBlank() }
                PantallaFamilia(familiaIdInicial = familiaId,
                    idiomaViewModel = idiomaViewModel,
                    viewModel = familiaViewModel,
                    alVolver = { familiaIdFinal ->
                        homeViewModel.sincronizarFamiliasDesdeGestion(
                            familiaPreferidaId = familiaIdFinal)
                        navController.popBackStack() }) }
            composable(route = Rutas.NUEVO_MENSAJE) {
                PantallaNuevoMensaje(idiomaViewModel = idiomaViewModel,
                    alVolver = { navController.popBackStack() },
                    alAbrirChatPrivado = { conversacionId ->
                        navController.navigate(
                            Rutas.crearRutaChatPrivado(conversacionId)) {
                            popUpTo(Rutas.NUEVO_MENSAJE) { inclusive = true }
                            launchSingleTop = true } }) }
            composable(route = Rutas.CHAT_FAMILIA,
                arguments = listOf(navArgument("familiaId") {
                        type = NavType.StringType },
                    navArgument("nombre") { type = NavType.StringType
                        defaultValue = "" })) { entrada ->
                val familiaId = entrada.arguments
                    ?.getString("familiaId").orEmpty()
                val nombreFamilia = entrada.arguments
                    ?.getString("nombre").orEmpty()
                PantallaChatFamilia(familiaId = familiaId,
                    nombreFamilia = nombreFamilia, idiomaViewModel = idiomaViewModel,
                    alVolver = { navController.popBackStack() }) }
            composable(route = Rutas.CHAT_PRIVADO, arguments = listOf(
                    navArgument("conversacionId") { type = NavType.StringType })
            ) { entrada ->
                val conversacionId = entrada.arguments
                    ?.getString("conversacionId").orEmpty()
                PantallaChatPrivado(conversacionId = conversacionId,
                    idiomaViewModel = idiomaViewModel,
                    alVolver = { navController.popBackStack() }) } }
        IndicadorTraduccion(visible = idiomaViewModel.traduciendo)
    }
}