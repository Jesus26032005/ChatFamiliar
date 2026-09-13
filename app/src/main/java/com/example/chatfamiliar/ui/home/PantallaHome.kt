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
import com.example.chatfamiliar.ui.home.components.DialogoEliminarFamilia
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatfamiliar.R
import com.example.chatfamiliar.model.Familia
import com.example.chatfamiliar.model.MiembroFamilia
import com.example.chatfamiliar.model.MiembroFamiliaDetalle
import com.example.chatfamiliar.model.Usuario
import com.example.chatfamiliar.ui.home.components.AccionesFamilia
import com.example.chatfamiliar.ui.home.components.DialogoCrearFamilia
import com.example.chatfamiliar.ui.home.components.DialogoNombreInicial
import com.example.chatfamiliar.ui.home.components.DialogoUnirseFamilia
import com.example.chatfamiliar.ui.home.components.EstadoSinFamilia
import com.example.chatfamiliar.ui.home.components.PanelGestionFamilia
import com.example.chatfamiliar.ui.home.components.SelectorFamiliaActiva
import com.example.chatfamiliar.ui.home.components.TarjetaPerfilUsuario
import com.example.chatfamiliar.ui.language.ControlIdiomaCompacto
import com.example.chatfamiliar.ui.language.IdiomaViewModel
import com.example.chatfamiliar.ui.language.TraducirTexto
import com.example.chatfamiliar.ui.language.recordarIdiomaEfectivo
import com.example.chatfamiliar.ui.language.recordarTextosApp
import com.example.chatfamiliar.ui.home.components.DialogoEditarFamilia
import com.example.chatfamiliar.ui.home.components.DialogoAbandonarFamilia
import com.example.chatfamiliar.ui.home.components.PanelMiembrosFamilia
import com.example.chatfamiliar.ui.home.components.DialogoExpulsarMiembro
import com.example.chatfamiliar.ui.home.components.DialogoQuitarAdministrador
import com.example.chatfamiliar.ui.home.components.PanelMiembrosFamilia
import com.example.chatfamiliar.ui.home.components.CodigoInvitacionFamilia

@Composable
fun PantallaHome(
    idiomaViewModel: IdiomaViewModel,
    viewModel: HomeViewModel = viewModel(),
    alCerrarSesion: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.cargarUsuario() }

    val idiomaEfectivo = recordarIdiomaEfectivo(idiomaViewModel.idiomaSeleccionado)

    ContenidoHome(
        usuario = viewModel.usuario,
        familias = viewModel.familias,
        familiaActiva = viewModel.familiaActiva,
        membresiaActiva = viewModel.membresiaActiva,
        nombreNuevo = viewModel.nombreNuevo,
        nombreFamiliaNueva = viewModel.nombreFamiliaNueva,
        codigoInvitacion = viewModel.codigoInvitacion,
        necesitaNombre = viewModel.necesitaNombre,
        miembrosFamilia = viewModel.miembrosFamilia,
        cargandoMiembros = viewModel.cargandoMiembros,
        actualizandoRolUid = viewModel.actualizandoRolUid,
        errorMiembrosRecurso = viewModel.errorMiembrosRecurso,
        alHacerAdministrador = viewModel::hacerAdministrador,
        cargando = viewModel.cargando,
        cargandoFamilia = viewModel.cargandoFamilia,
        guardandoNombre = viewModel.guardandoNombre,
        creandoFamilia = viewModel.creandoFamilia,
        nombreFamiliaEdicion = viewModel.nombreFamiliaEdicion,
        editandoFamilia = viewModel.editandoFamilia,
        abandonandoFamilia = viewModel.abandonandoFamilia,
        uniendoFamilia = viewModel.uniendoFamilia,
        errorRecurso = viewModel.errorRecurso,
        errorFamiliaRecurso = viewModel.errorFamiliaRecurso,
        idiomaSeleccionado = idiomaViewModel.idiomaSeleccionado,
        expulsandoMiembroUid = viewModel.expulsandoMiembroUid,
        eliminandoFamilia = viewModel.eliminandoFamilia,
        idiomaEfectivo = idiomaEfectivo,
        traducir = idiomaViewModel::traducir,
        alSeleccionarIdioma = idiomaViewModel::seleccionarIdioma,
        alCambiarNombre = viewModel::actualizarNombre,
        alGuardarNombre = viewModel::guardarNombre,
        alSeleccionarFamilia = viewModel::seleccionarFamilia,
        alCambiarNombreFamilia = viewModel::actualizarNombreFamilia,
        alCrearFamilia = { alTenerExito -> viewModel
            .crearFamilia(alTenerExito = alTenerExito) },
        alCambiarCodigo = viewModel::actualizarCodigoInvitacion,
        alUnirseFamilia = { alTenerExito -> viewModel
                .unirseAFamilia(alTenerExito = alTenerExito) },
        alLimpiarErrorFamilia = viewModel::limpiarErrorFamilia,
        alPrepararEdicionFamilia = viewModel::prepararEdicionFamilia,
        alCambiarNombreFamiliaEdicion = viewModel::actualizarNombreFamiliaEdicion,
        alEditarFamilia = { alTenerExito -> viewModel
            .editarFamilia(alTenerExito = alTenerExito) },
        alAbandonarFamilia = { alTenerExito -> viewModel
            .abandonarFamilia(alTenerExito = alTenerExito) },
        alCerrarSesion = { viewModel
            .cerrarSesion(alCerrarSesion = alCerrarSesion) },
        alQuitarAdministrador = viewModel::quitarAdministrador,
        alExpulsarMiembro = { miembro, alTenerExito ->
            viewModel.expulsarMiembro(miembro = miembro,
                alTenerExito = alTenerExito) },
        alLimpiarErrorMiembros = viewModel::limpiarErrorMiembros,
        alEliminarFamilia = { alTenerExito -> viewModel.eliminarFamilia(
            alTenerExito = alTenerExito) },
    )
}

@Composable
private fun ContenidoHome(
    usuario: Usuario?,
    familias: List<Familia>,
    familiaActiva: Familia?,
    membresiaActiva: MiembroFamilia?,
    nombreNuevo: String,
    nombreFamiliaNueva: String,
    codigoInvitacion: String,
    nombreFamiliaEdicion: String,
    expulsandoMiembroUid: String?,
    necesitaNombre: Boolean,
    cargando: Boolean,
    cargandoFamilia: Boolean,
    eliminandoFamilia: Boolean,
    guardandoNombre: Boolean,
    creandoFamilia: Boolean,
    uniendoFamilia: Boolean,
    miembrosFamilia: List<MiembroFamiliaDetalle>,
    cargandoMiembros: Boolean,
    actualizandoRolUid: String?,
    @StringRes errorMiembrosRecurso: Int?,
    editandoFamilia: Boolean,
    abandonandoFamilia: Boolean,
    @StringRes errorRecurso: Int?,
    @StringRes errorFamiliaRecurso: Int?,
    idiomaSeleccionado: String?,
    idiomaEfectivo: String,
    traducir: TraducirTexto,
    alSeleccionarIdioma: (String?) -> Unit,
    alCambiarNombre: (String) -> Unit,
    alGuardarNombre: () -> Unit,
    alSeleccionarFamilia: (Familia) -> Unit,
    alCambiarNombreFamilia: (String) -> Unit,
    alCrearFamilia: (() -> Unit) -> Unit,
    alCambiarCodigo: (String) -> Unit,
    alUnirseFamilia: (() -> Unit) -> Unit,
    alEliminarFamilia: (() -> Unit) -> Unit,
    alPrepararEdicionFamilia: () -> Unit,
    alCambiarNombreFamiliaEdicion: (String) -> Unit,
    alEditarFamilia: (() -> Unit) -> Unit,
    alAbandonarFamilia: (() -> Unit) -> Unit,
    alLimpiarErrorFamilia: () -> Unit,
    alCerrarSesion: () -> Unit,
    alHacerAdministrador: (MiembroFamiliaDetalle) -> Unit,
    alQuitarAdministrador: (MiembroFamiliaDetalle) -> Unit,
    alExpulsarMiembro: (MiembroFamiliaDetalle, () -> Unit) -> Unit,
    alLimpiarErrorMiembros: () -> Unit,
) {
    var mostrarCrearFamilia by
    rememberSaveable { mutableStateOf(false) }
    var mostrarUnirseFamilia by
    rememberSaveable { mutableStateOf(false) }
    var mostrarEditarFamilia by rememberSaveable {
        mutableStateOf(false) }
    var mostrarAbandonarFamilia by rememberSaveable {
        mutableStateOf(false) }
    val textos = recordarTextosApp(idiomaEfectivo = idiomaEfectivo,
            traducir = traducir)
    val mensajeErrorGeneral = errorRecurso?.let { recurso ->
            textos.texto(recurso) }
    val mensajeErrorFamilia =
        errorFamiliaRecurso?.let { recurso ->
            textos.texto(recurso) }
    var mostrarEliminarFamilia by rememberSaveable {
        mutableStateOf(false) }
    val mensajeErrorMiembros =
        errorMiembrosRecurso?.let { recurso -> textos.texto(recurso) }
    var uidQuitarAdministrador by
    rememberSaveable { mutableStateOf<String?>(null) }
    var uidExpulsarMiembro by
    rememberSaveable { mutableStateOf<String?>(null) }

    val miembroQuitarAdministrador =
        miembrosFamilia.firstOrNull { it.uid == uidQuitarAdministrador }
    val miembroExpulsar =
        miembrosFamilia.firstOrNull { it.uid == uidExpulsarMiembro }
    Box(modifier = Modifier.fillMaxSize()
            .safeDrawingPadding()
    ) {
        // Carga inicial
        if (cargando && usuario == null) {
            Column(modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = textos.texto(R.string.home_description),
                    color = MaterialTheme
                        .colorScheme.onSurfaceVariant) }
        } else {
            // Contenido principal
            Column(modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                // Encabezado
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
                                tint = MaterialTheme
                                    .colorScheme.primary) }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)
                    ) {
                        Text(text = textos.texto(R.string.home_title),
                            style = MaterialTheme
                                .typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme
                                .primary)
                        Text(text = usuario?.correo.orEmpty(),
                            style = MaterialTheme.typography
                                .bodySmall,
                            color = MaterialTheme.colorScheme
                                .onSurfaceVariant) }
                    ControlIdiomaCompacto(idiomaSeleccionado = idiomaSeleccionado,
                        idiomaEfectivo = idiomaEfectivo,
                        traducir = traducir,
                        alSeleccionarIdioma = alSeleccionarIdioma)
                }
                Spacer(modifier = Modifier.height(28.dp))

                // Bienvenida
                Card(modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor =
                        MaterialTheme.colorScheme
                            .primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text(
                            text = if (usuario?.nombre.isNullOrBlank()) {
                                    textos.texto(R.string.home_welcome)
                                } else {
                                    "${textos.texto(R.string.home_welcome)}," +
                                            " ${usuario?.nombre}"
                                       },
                            style = MaterialTheme
                                .typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = textos.texto(R.string.home_description),
                            style = MaterialTheme
                                .typography.bodyMedium,
                            color = MaterialTheme
                                .colorScheme.onPrimaryContainer)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                // Perfil general
                usuario?.let { perfil ->
                    TarjetaPerfilUsuario(
                        nombre = perfil.nombre,
                        correo = perfil.correo)
                }
                Spacer(modifier = Modifier.height(20.dp))

                // Error general
                if (mensajeErrorGeneral != null &&
                    !necesitaNombre
                ) {
                    Surface(modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = mensajeErrorGeneral,
                            modifier = Modifier.padding(14.dp),
                            color = MaterialTheme
                                .colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center) }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Seccion de familias
                when {
                    cargandoFamilia && familias.isEmpty() -> {
                        Column(modifier = Modifier.fillMaxWidth()
                            .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = textos.texto(
                                R.string.home_family_loading),
                                color = MaterialTheme.colorScheme
                                    .onSurfaceVariant)
                        }
                    }

                    // Si no hay familias
                    familias.isEmpty() -> {
                        EstadoSinFamilia(
                            titulo = textos.texto(R.string
                                .home_no_family_title),
                            descripcion = textos.texto(R.string
                                .home_no_family_description),
                            textoCrear = textos.texto(R.string
                                .home_family_create),
                            textoUnirse = textos.texto(R.string
                                .home_family_join),
                            alCrearFamilia = {
                                alLimpiarErrorFamilia()
                                mostrarCrearFamilia = true },
                            alUnirseFamilia = {
                                alLimpiarErrorFamilia()
                                mostrarUnirseFamilia = true }
                        )
                    }
                    familiaActiva != null -> {
                        val textoRol =
                            when (membresiaActiva?.rol) {
                                MiembroFamilia.ROL_ADMINISTRADOR -> {
                                    textos.texto(R.string
                                        .home_role_admin) }
                                MiembroFamilia.ROL_MIEMBRO -> {
                                    textos.texto(R.string
                                        .home_role_member) }
                                else -> null }
                        SelectorFamiliaActiva(
                            familias = familias,
                            familiaActiva = familiaActiva,
                            titulo = textos.texto(R.string.home_family_active),
                            textoRol = textoRol,
                            cargando = cargandoFamilia,
                            alSeleccionarFamilia = alSeleccionarFamilia)
                        Spacer(modifier = Modifier.height(16.dp))
                        CodigoInvitacionFamilia(
                            codigo = familiaActiva.codigoInvitacion,
                            titulo = textos.texto(
                                R.string.home_invitation_code_title),
                            textoCopiar = textos.texto(
                                R.string.home_invitation_code_copy),
                            textoCopiado = textos.texto(
                                R.string.home_invitation_code_copied))
                        Spacer(modifier = Modifier.height(20.dp))
                        PanelMiembrosFamilia(
                            miembros = miembrosFamilia,
                            uidUsuarioActual = usuario?.uid.orEmpty(),
                            uidAdministradorPrincipal = familiaActiva.creadoPor,
                            esAdministradorActual = membresiaActiva?.rol ==
                                        MiembroFamilia.ROL_ADMINISTRADOR,
                            titulo = textos.texto(
                                    R.string.home_members_title),
                            textoRolAdministrador = textos.texto(
                                    R.string.home_role_admin),
                            textoRolMiembro = textos.texto(
                                    R.string.home_role_member),
                            textoAdministradorPrincipal = textos.texto(
                                    R.string.home_member_owner),
                            textoTu = textos.texto(
                                    R.string.home_member_you),
                            textoHacerAdministrador = textos.texto(
                                    R.string.home_member_make_admin),
                            textoQuitarAdministrador = textos.texto(
                                    R.string.home_member_remove_admin),
                            textoExpulsar = textos.texto(
                                R.string.home_member_remove),
                            textoVacio = textos.texto(
                                R.string.home_members_empty),
                            cargando = cargandoMiembros,
                            actualizandoRolUid = actualizandoRolUid,
                            expulsandoMiembroUid = expulsandoMiembroUid,
                            mensajeError = mensajeErrorMiembros,
                            alHacerAdministrador = alHacerAdministrador,
                            alQuitarAdministrador = { miembro ->
                                alLimpiarErrorMiembros()
                                uidQuitarAdministrador = miembro.uid },
                            alExpulsar = { miembro -> alLimpiarErrorMiembros()
                                uidExpulsarMiembro = miembro.uid }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AccionesFamilia(
                            textoCrear = textos.texto(R.string
                                .home_family_create_another),
                            textoUnirse = textos.texto(R.string
                                .home_family_join_another),
                            alCrearFamilia = {
                                alLimpiarErrorFamilia()
                                mostrarCrearFamilia = true },
                            alUnirseFamilia = {
                                alLimpiarErrorFamilia()
                                mostrarUnirseFamilia = true
                            }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        PanelGestionFamilia(
                            esAdministrador = membresiaActiva?.rol ==
                                    MiembroFamilia.ROL_ADMINISTRADOR,
                            esAdministradorPrincipal = usuario?.uid
                                    == familiaActiva.creadoPor,
                            titulo = textos.texto(
                                R.string.home_family_settings),
                            textoEditar = textos.texto(
                                R.string.home_family_edit),
                            textoAbandonar = textos.texto(
                                R.string.home_family_leave),
                            textoEliminar = textos.texto(
                                R.string.home_family_delete),
                            alEditar = {
                                alPrepararEdicionFamilia()
                                alLimpiarErrorFamilia()
                                mostrarEditarFamilia = true
                            },
                            alAbandonar = { alLimpiarErrorFamilia()
                                mostrarAbandonarFamilia = true
                            },
                            alEliminar = { alLimpiarErrorFamilia()
                                mostrarEliminarFamilia = true
                            })
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))

                // Cerrar sesion
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


    // Dialogo inicial
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


    // Dialogo para crear una familia
    DialogoCrearFamilia(
        visible = mostrarCrearFamilia,
        nombre = nombreFamiliaNueva,
        titulo = textos.texto(R.string.home_create_family_title),
        descripcion = textos.texto(R.string
            .home_create_family_description),
        etiqueta = textos.texto(R.string.home_family_name_label),
        textoConfirmar = textos.texto(R.string.home_family_create),
        textoCancelar = textos.texto(R.string.home_family_cancel),
        mensajeError = mensajeErrorFamilia,
        cargando = creandoFamilia,
        alCambiarNombre = alCambiarNombreFamilia,
        alConfirmar = { alCrearFamilia { mostrarCrearFamilia = false } },
        alCancelar = { mostrarCrearFamilia = false
            alLimpiarErrorFamilia() }
    )


    // Dialogo para uniser a una familia
    DialogoUnirseFamilia(
        visible = mostrarUnirseFamilia,
        codigo = codigoInvitacion,
        titulo = textos.texto(R.string.home_join_family_title),
        descripcion = textos.texto(R.string
            .home_join_family_description),
        etiqueta = textos.texto(R.string.home_family_code_label),
        textoConfirmar = textos.texto(R.string.home_family_join),
        textoCancelar = textos.texto(R.string.home_family_cancel),
        mensajeError = mensajeErrorFamilia,
        cargando = uniendoFamilia,
        alCambiarCodigo = alCambiarCodigo,
        alConfirmar = { alUnirseFamilia { mostrarUnirseFamilia = false } },
        alCancelar = { mostrarUnirseFamilia = false
            alLimpiarErrorFamilia() }
    )

    DialogoEditarFamilia(
        visible = mostrarEditarFamilia,
        nombre = nombreFamiliaEdicion,
        titulo = textos.texto(R.string.home_edit_family_title),
        descripcion = textos.texto(R.string.home_edit_family_description),
        etiqueta = textos.texto(R.string.home_family_name_label),
        textoConfirmar = textos.texto(R.string.home_edit_family_save),
        textoCancelar = textos.texto(R.string.home_family_cancel),
        mensajeError = mensajeErrorFamilia,
        cargando = editandoFamilia,
        alCambiarNombre = alCambiarNombreFamiliaEdicion,
        alConfirmar = { alEditarFamilia {
                mostrarEditarFamilia = false
            } },
        alCancelar = {
            mostrarEditarFamilia = false
            alLimpiarErrorFamilia() }
    )
    DialogoAbandonarFamilia(
        visible = mostrarAbandonarFamilia,
        nombreFamilia = familiaActiva?.nombre.orEmpty(),
        titulo = textos.texto(R.string.home_leave_family_title),
        descripcion = textos.texto(R.string.home_leave_family_description),
        textoConfirmar = textos.texto(R.string.home_leave_family_confirm),
        textoCancelar = textos.texto(R.string.home_family_cancel),
        mensajeError = mensajeErrorFamilia,
        cargando = abandonandoFamilia,
        alConfirmar = { alAbandonarFamilia {
                mostrarAbandonarFamilia = false } },
        alCancelar = { mostrarAbandonarFamilia = false
            alLimpiarErrorFamilia() }
    )
    DialogoQuitarAdministrador(
        visible = miembroQuitarAdministrador != null,
        nombreMiembro = miembroQuitarAdministrador?.nombre?.ifBlank {
                    miembroQuitarAdministrador.correo }.orEmpty(),
        titulo = textos.texto(R.string.home_remove_admin_title),
        descripcion = textos.texto(R.string.home_remove_admin_description),
        textoConfirmar = textos.texto(R.string.home_remove_admin_confirm),
        textoCancelar = textos.texto(R.string.home_family_cancel),
        mensajeError = mensajeErrorMiembros,
        cargando = miembroQuitarAdministrador != null &&
                actualizandoRolUid == miembroQuitarAdministrador.uid,
        alConfirmar = {
            miembroQuitarAdministrador?.let { miembro ->
                    alQuitarAdministrador(miembro)
                uidQuitarAdministrador = null } },
        alCancelar = { uidQuitarAdministrador = null
            alLimpiarErrorMiembros() }
    )
    DialogoExpulsarMiembro(
        visible = miembroExpulsar != null,
        nombreMiembro = miembroExpulsar?.nombre?.ifBlank {
                    miembroExpulsar.correo }.orEmpty(),
        titulo = textos.texto(R.string.home_remove_member_title),
        descripcion = textos.texto(R.string.home_remove_member_description),
        textoConfirmar = textos.texto(R.string.home_remove_member_confirm),
        textoCancelar = textos.texto(R.string.home_family_cancel),
        mensajeError = mensajeErrorMiembros,
        cargando =
            miembroExpulsar != null &&
                    expulsandoMiembroUid == miembroExpulsar.uid,
        alConfirmar = {
            miembroExpulsar?.let { miembro -> alExpulsarMiembro(miembro) {
                uidExpulsarMiembro = null } } },
        alCancelar = {
            uidExpulsarMiembro = null
            alLimpiarErrorMiembros() }
    )

    DialogoEliminarFamilia(
        visible = mostrarEliminarFamilia,
        nombreFamilia = familiaActiva?.nombre.orEmpty(),
        titulo = textos.texto(R.string.home_delete_family_title),
        descripcion = textos.texto(R.string
            .home_delete_family_description),
        textoConfirmar = textos.texto(R.string.home_delete_family_confirm),
        textoCancelar = textos.texto(R.string.home_family_cancel),
        mensajeError = mensajeErrorFamilia,
        cargando = eliminandoFamilia,
        alConfirmar = {
            alEliminarFamilia { mostrarEliminarFamilia = false } },
        alCancelar = { mostrarEliminarFamilia = false
            alLimpiarErrorFamilia() })

}