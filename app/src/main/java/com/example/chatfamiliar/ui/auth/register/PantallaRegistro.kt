package com.example.chatfamiliar.ui.auth.register

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatfamiliar.R
import com.example.chatfamiliar.data.language.IdiomaRepository
import com.example.chatfamiliar.ui.auth.components.BotonAuth
import com.example.chatfamiliar.ui.auth.components.CampoCorreo
import com.example.chatfamiliar.ui.auth.components.CampoPassword
import com.example.chatfamiliar.ui.auth.components.MensajeError
import com.example.chatfamiliar.ui.language.ControlIdiomaCompacto
import com.example.chatfamiliar.ui.language.IdiomaViewModel
import com.example.chatfamiliar.ui.language.TraducirTexto
import com.example.chatfamiliar.ui.language.recordarIdiomaEfectivo
import com.example.chatfamiliar.ui.language.recordarTextosApp


@Composable
fun PantallaRegistro(
    idiomaViewModel: IdiomaViewModel,
    viewModel: RegistroViewModel = viewModel(),
    alNavegarVerificacion: () -> Unit,
    alNavegarLogin: () -> Unit
) {
    val idiomaEfectivo = recordarIdiomaEfectivo(idiomaViewModel.idiomaSeleccionado)
    ContenidoRegistro(
        correo = viewModel.correo,
        password = viewModel.password,
        confirmarPassword = viewModel.confirmarPassword,
        errorRecurso = viewModel.errorRecurso,
        cargando = viewModel.cargando,
        idiomaSeleccionado = idiomaViewModel.idiomaSeleccionado,
        idiomaEfectivo = idiomaEfectivo,
        traducir = idiomaViewModel::traducir,
        alSeleccionarIdioma = idiomaViewModel::seleccionarIdioma,
        alCambiarCorreo = viewModel::actualizarCorreo,
        alCambiarPassword = viewModel::actualizarPassword,
        alCambiarConfirmarPassword = viewModel::actualizarConfirmarPassword,
        alRegistrar = { viewModel.crearCuenta(alTenerExito = alNavegarVerificacion)
        },
        alNavegarLogin = alNavegarLogin
    )
}

@Composable
fun ContenidoRegistro(
    correo: String,
    password: String,
    confirmarPassword: String,
    @StringRes
    errorRecurso: Int?,
    cargando: Boolean,
    idiomaSeleccionado: String?,
    idiomaEfectivo: String,
    traducir: TraducirTexto,
    alSeleccionarIdioma: (String?) -> Unit,
    alCambiarCorreo: (String) -> Unit,
    alCambiarPassword: (String) -> Unit,
    alCambiarConfirmarPassword: (String) -> Unit,
    alRegistrar: () -> Unit,
    alNavegarLogin: () -> Unit
) {
    val textos = recordarTextosApp(idiomaEfectivo = idiomaEfectivo,
        traducir = traducir)
    Box(modifier = Modifier.fillMaxSize()
            .safeDrawingPadding().imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ControlIdiomaCompacto(
                    idiomaSeleccionado = idiomaSeleccionado,
                    idiomaEfectivo = idiomaEfectivo,
                    traducir = traducir,
                    alSeleccionarIdioma = alSeleccionarIdioma
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            // Icono principal de Registro.
            Surface(modifier = Modifier.padding(4.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(imageVector = Icons.Filled.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.padding(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Nombre de la aplicación.
            Text(text = textos.texto(R.string.brand_name),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            // Título.
            Text(text = textos.texto(R.string.register_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            // Descripción.
            Text(text = textos.texto(R.string.register_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))

            //Card del formulario.
            Card(modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme
                        .surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)
                ) {
                    // Correo.
                    CampoCorreo(
                        valor = correo,
                        alCambiarValor = alCambiarCorreo,
                        etiqueta = textos.texto(R.string.auth_email_label),
                        habilitado = !cargando)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Contraseña.
                    CampoPassword(
                        valor = password,
                        alCambiarValor = alCambiarPassword,
                        etiqueta = textos.texto(R.string.auth_password_label),
                        descripcionMostrar = textos.texto(R.string.auth_show_password),
                        descripcionOcultar = textos.texto(R.string.auth_hide_password),
                        habilitado = !cargando
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirmación.
                    CampoPassword(
                        valor = confirmarPassword,
                        alCambiarValor = alCambiarConfirmarPassword,
                        etiqueta = textos.texto(R.string.register_confirm_password),
                        descripcionMostrar = textos.texto(R.string.auth_show_password),
                        descripcionOcultar = textos.texto(R.string.auth_hide_password),
                        habilitado = !cargando
                    )
                    // Mensaje de error traducido.
                    MensajeError(mensaje = errorRecurso?.let { recurso ->
                            textos.texto(recurso) })
                    Spacer(modifier = Modifier.height(16.dp))
                    // Registrar
                    BotonAuth(
                        texto = textos.texto(R.string.register_button),
                        cargando = cargando,
                        onClick = alRegistrar)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Volver al Login.
            TextButton(onClick = alNavegarLogin,
                enabled = !cargando
            ) {
                Text(text = textos.texto(R.string.register_have_account),
                    fontWeight = FontWeight.SemiBold)
            }
        }
    }
}


@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun PantallaRegistroPreview() {
    MaterialTheme {
        ContenidoRegistro(
            correo = "familia@correo.com",
            password = "12345678",
            confirmarPassword = "12345678",
            errorRecurso = null,
            cargando = false,
            idiomaSeleccionado = IdiomaRepository.ESPANOL,
            idiomaEfectivo = IdiomaRepository.ESPANOL,
            traducir = { texto, _, alCompletar ->
                alCompletar(Result.success(texto)) },
            alSeleccionarIdioma = {},
            alCambiarCorreo = {},
            alCambiarPassword = {},
            alCambiarConfirmarPassword = {},
            alRegistrar = {},
            alNavegarLogin = {}
        )
    }
}