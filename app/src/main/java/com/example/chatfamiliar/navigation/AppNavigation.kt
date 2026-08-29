package com.example.chatfamiliar.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatfamiliar.data.auth.AuthRepository
import com.example.chatfamiliar.ui.auth.login.PantallaLogin
import com.example.chatfamiliar.ui.auth.register.PantallaRegistro
import com.example.chatfamiliar.ui.auth.verification.PantallaVerificacion
import com.example.chatfamiliar.ui.home.PantallaHome


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authRepository = remember { AuthRepository() }

    val rutaInicial = remember {
        val usuarioActual = authRepository.obtenerUsuarioActual()
        when {
            usuarioActual == null -> Rutas.LOGIN
            usuarioActual.isEmailVerified -> Rutas.HOME
            else -> Rutas.VERIFICACION
        }
    }

    NavHost(navController = navController, startDestination = rutaInicial) {
        composable(route = Rutas.LOGIN) {
            PantallaLogin(
                alNavegarHome = { navController.navigate(Rutas.HOME) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    } },
                alNavegarRegistro = { navController.navigate(Rutas.REGISTRO) { launchSingleTop = true } },
                alNavegarVerificacion = { navController.navigate(Rutas.VERIFICACION) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    } }
            )
        }

        composable(route = Rutas.REGISTRO) {
            PantallaRegistro(
                alNavegarVerificacion = { navController.navigate(Rutas.VERIFICACION) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    } },

                alNavegarLogin = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.REGISTRO) { inclusive = true }
                        launchSingleTop = true
                    } }
            )
        }

        composable(route = Rutas.VERIFICACION) {
            PantallaVerificacion(
                alNavegarHome = {
                    navController.navigate(Rutas.HOME) {
                        popUpTo(Rutas.VERIFICACION) { inclusive = true }
                        launchSingleTop = true
                    } },
                alNavegarLogin = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    } }
            )
        }

        composable(route = Rutas.HOME) {
            PantallaHome(
                alCerrarSesion = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    } }
            )
        }
    }
}