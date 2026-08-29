package com.example.chatfamiliar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatfamiliar.ui.theme.ChatFamiliarTheme
import com.example.chatfamiliar.ui.auth.PantallaRegistro
import com.example.chatfamiliar.ui.auth.PantallaLogin
import com.example.chatfamiliar.ui.home.PantallaHome
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatFamiliarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppMensajeriaFamiliar()
                }
            }
        }
    }
}

@Composable
fun AppMensajeriaFamiliar() {
    val navController = rememberNavController()
    val usuarioActual = FirebaseAuth.getInstance().currentUser
    val destinoInicial = if (usuarioActual != null) "home" else "login"

    NavHost(navController = navController, startDestination = destinoInicial) {

        composable("login") {
            PantallaLogin(
                alNavegarHome = { navController.navigate("home") },
                alNavegarRegistro = { navController.navigate("registro") }
            )
        }

        composable("registro") {
            PantallaRegistro(
                alNavegarLogin = { navController.navigate("login") }
            )
        }

        composable("home") {
            PantallaHome(
                alCerrarSesion = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}