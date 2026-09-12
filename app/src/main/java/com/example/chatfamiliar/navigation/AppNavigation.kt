package com.example.chatfamiliar.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatfamiliar.data.auth.AuthRepository
import com.example.chatfamiliar.ui.auth.login.PantallaLogin
import com.example.chatfamiliar.ui.auth.register.PantallaRegistro
import com.example.chatfamiliar.ui.auth.verification.PantallaVerificacion
import com.example.chatfamiliar.ui.home.PantallaHome
import com.example.chatfamiliar.ui.language.IdiomaViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.chatfamiliar.ui.language.IndicadorTraduccion

@Composable
fun AppNavigation() {

    val navController =
        rememberNavController()

    val authRepository =
        remember { AuthRepository() }

    val idiomaViewModel: IdiomaViewModel =
        viewModel()


    val usuarioActual = authRepository.obtenerUsuarioActual()

    val rutaInicial =
        when {
            usuarioActual == null -> { Rutas.LOGIN }
            usuarioActual.isEmailVerified -> { Rutas.HOME }
            else -> { Rutas.VERIFICACION }
        }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = rutaInicial
        ) {
            composable(route = Rutas.LOGIN) {
                PantallaLogin(
                    idiomaViewModel = idiomaViewModel,

                    alNavegarHome = {
                        navController.navigate(Rutas.HOME) {
                            popUpTo(Rutas.LOGIN) {
                                inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    alNavegarRegistro = {
                        navController.navigate(Rutas.REGISTRO
                        ) { launchSingleTop = true }
                    },

                    alNavegarVerificacion = {
                        navController.navigate(Rutas.VERIFICACION) {
                            popUpTo(Rutas.LOGIN) {
                                inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(route = Rutas.REGISTRO) {
                PantallaRegistro(
                    idiomaViewModel = idiomaViewModel,
                    alNavegarVerificacion = {
                        navController.navigate(Rutas.VERIFICACION
                        ) {
                            popUpTo(Rutas.LOGIN) {
                                inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    alNavegarLogin = { navController.popBackStack() }
                )
            }
            composable(route = Rutas.VERIFICACION) {
                PantallaVerificacion(idiomaViewModel = idiomaViewModel,
                    alNavegarHome = {
                        navController.navigate(Rutas.HOME) {
                            popUpTo(Rutas.VERIFICACION) {
                                inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    alNavegarLogin = {
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(route = Rutas.HOME) {
                PantallaHome(
                    idiomaViewModel = idiomaViewModel,
                    alCerrarSesion = {
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(Rutas.HOME) {
                                inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
        IndicadorTraduccion(
            visible =
                idiomaViewModel.traduciendo
        )
    }
}