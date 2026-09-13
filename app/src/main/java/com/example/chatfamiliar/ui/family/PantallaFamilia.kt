package com.example.chatfamiliar.ui.family

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.chatfamiliar.R
import com.example.chatfamiliar.model.Familia
import com.example.chatfamiliar.model.MiembroFamilia
import com.example.chatfamiliar.model.MiembroFamiliaDetalle
import com.example.chatfamiliar.ui.family.components.AccionesFamilia
import com.example.chatfamiliar.ui.family.components.DialogoAbandonarFamilia
import com.example.chatfamiliar.ui.family.components.DialogoCrearFamilia
import com.example.chatfamiliar.ui.family.components.DialogoEditarFamilia
import com.example.chatfamiliar.ui.family.components.DialogoEliminarFamilia
import com.example.chatfamiliar.ui.family.components.DialogoExpulsarMiembro
import com.example.chatfamiliar.ui.family.components.DialogoQuitarAdministrador
import com.example.chatfamiliar.ui.family.components.DialogoUnirseFamilia
import com.example.chatfamiliar.ui.family.components.EstadoSinFamilia
import com.example.chatfamiliar.ui.family.components.PanelGestionFamilia
import com.example.chatfamiliar.ui.family.components.PanelMiembrosFamilia
import com.example.chatfamiliar.ui.language.IdiomaViewModel
import com.example.chatfamiliar.ui.language.TraducirTexto
import com.example.chatfamiliar.ui.language.recordarIdiomaEfectivo
import com.example.chatfamiliar.ui.language.recordarTextosApp

@Composable
fun PantallaFamilia(
    familiaIdInicial: String?,
    idiomaViewModel: IdiomaViewModel,
    viewModel: FamiliaViewModel,
    alVolver: (String?) -> Unit
) {
    LaunchedEffect(familiaIdInicial) {
        viewModel.cargarPantalla(
            familiaIdInicial = familiaIdInicial
        )
    }
    val idiomaEfectivo =
        recordarIdiomaEfectivo(idiomaViewModel.idiomaSeleccionado)
    val volver = { alVolver(viewModel.familiaActiva?.id) }
    BackHandler { volver() }
    ContenidoFamilia(
        uidUsuarioActual = viewModel.uidUsuarioActual,
        familiaActiva = viewModel.familiaActiva,
        membresiaActiva = viewModel.membresiaActiva,
        miembrosFamilia = viewModel.miembrosFamilia,
        nombreFamiliaNueva = viewModel.nombreFamiliaNueva,
        codigoInvitacion = viewModel.codigoInvitacion,
        nombreFamiliaEdicion = viewModel.nombreFamiliaEdicion,
        cargando = viewModel.cargando,
        cargandoMiembros = viewModel.cargandoMiembros,
        creandoFamilia = viewModel.creandoFamilia,
        uniendoFamilia = viewModel.uniendoFamilia,
        editandoFamilia = viewModel.editandoFamilia,
        abandonandoFamilia = viewModel.abandonandoFamilia,
        eliminandoFamilia = viewModel.eliminandoFamilia,
        actualizandoRolUid = viewModel.actualizandoRolUid,
        expulsandoMiembroUid = viewModel.expulsandoMiembroUid,
        errorFamiliaRecurso = viewModel.errorFamiliaRecurso,
        errorMiembrosRecurso = viewModel.errorMiembrosRecurso,
        idiomaEfectivo = idiomaEfectivo,
        traducir = idiomaViewModel::traducir,
        alVolver = volver,
        alCambiarNombreFamilia = viewModel::actualizarNombreFamilia,
        alCrearFamilia = { alTenerExito ->
            viewModel.crearFamilia(alTenerExito = alTenerExito) },
        alCambiarCodigo = viewModel::actualizarCodigoInvitacion,
        alUnirseFamilia = { alTenerExito ->
            viewModel.unirseAFamilia(alTenerExito = alTenerExito) },
        alLimpiarErrorFamilia = viewModel::limpiarErrorFamilia,
        alPrepararEdicionFamilia = viewModel::prepararEdicionFamilia,
        alCambiarNombreFamiliaEdicion = viewModel::actualizarNombreFamiliaEdicion,
        alEditarFamilia = { alTenerExito -> viewModel.editarFamilia(
            alTenerExito = alTenerExito) },
        alAbandonarFamilia = { alTenerExito ->
            viewModel.abandonarFamilia(alTenerExito = alTenerExito) },
        alEliminarFamilia = { alTenerExito ->
            viewModel.eliminarFamilia(
                alTenerExito = alTenerExito) },
        alHacerAdministrador = viewModel::hacerAdministrador,
        alQuitarAdministrador = viewModel::quitarAdministrador,
        alExpulsarMiembro = { miembro, alTenerExito ->
            viewModel.expulsarMiembro(miembro = miembro, alTenerExito = alTenerExito) },
        alLimpiarErrorMiembros = viewModel::limpiarErrorMiembros
    )
}

@Composable
private fun ContenidoFamilia(
    uidUsuarioActual: String,
    familiaActiva: Familia?,
    membresiaActiva: MiembroFamilia?,
    miembrosFamilia: List<MiembroFamiliaDetalle>,
    nombreFamiliaNueva: String,
    codigoInvitacion: String,
    nombreFamiliaEdicion: String,
    cargando: Boolean,
    cargandoMiembros: Boolean,
    creandoFamilia: Boolean,
    uniendoFamilia: Boolean,
    editandoFamilia: Boolean,
    abandonandoFamilia: Boolean,
    eliminandoFamilia: Boolean,
    actualizandoRolUid: String?,
    expulsandoMiembroUid: String?,
    @StringRes errorFamiliaRecurso: Int?,
    @StringRes errorMiembrosRecurso: Int?,
    idiomaEfectivo: String,
    traducir: TraducirTexto,
    alVolver: () -> Unit,
    alCambiarNombreFamilia: (String) -> Unit,
    alCrearFamilia: (() -> Unit) -> Unit,
    alCambiarCodigo: (String) -> Unit,
    alUnirseFamilia: (() -> Unit) -> Unit,
    alLimpiarErrorFamilia: () -> Unit,
    alPrepararEdicionFamilia: () -> Unit,
    alCambiarNombreFamiliaEdicion: (String) -> Unit,
    alEditarFamilia: (() -> Unit) -> Unit,
    alAbandonarFamilia: (() -> Unit) -> Unit,
    alEliminarFamilia: (() -> Unit) -> Unit,
    alHacerAdministrador: (MiembroFamiliaDetalle) -> Unit,
    alQuitarAdministrador: (MiembroFamiliaDetalle) -> Unit,
    alExpulsarMiembro: (MiembroFamiliaDetalle, () -> Unit) -> Unit,
    alLimpiarErrorMiembros: () -> Unit
) {
    val textos = recordarTextosApp(
        idiomaEfectivo = idiomaEfectivo, traducir = traducir)
    val mensajeErrorFamilia =
        errorFamiliaRecurso?.let { recurso -> textos.texto(recurso) }
    val mensajeErrorMiembros =
        errorMiembrosRecurso?.let { recurso -> textos.texto(recurso) }
    var mostrarCrearFamilia by
        rememberSaveable { mutableStateOf(false) }
    var mostrarUnirseFamilia by
        rememberSaveable { mutableStateOf(false) }
    var mostrarEditarFamilia by
        rememberSaveable { mutableStateOf(false) }
    var mostrarAbandonarFamilia by
        rememberSaveable { mutableStateOf(false) }
    var mostrarEliminarFamilia by
        rememberSaveable { mutableStateOf(false) }
    var uidQuitarAdministrador by
        rememberSaveable { mutableStateOf<String?>(null) }
    var uidExpulsarMiembro by
        rememberSaveable { mutableStateOf<String?>(null) }
    val miembroQuitarAdministrador =
        miembrosFamilia.firstOrNull { it.uid == uidQuitarAdministrador }
    val miembroExpulsar =
        miembrosFamilia.firstOrNull { it.uid == uidExpulsarMiembro }
    Column(modifier = Modifier.fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = alVolver) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column { Text(text = textos.texto(R.string.family_management_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold)
                Text(text = textos.texto(
                    R.string.family_management_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp).align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(16.dp))
        } else if (familiaActiva == null) {
            EstadoSinFamilia(
                titulo = textos.texto(R.string.home_no_family_title),
                descripcion = textos.texto(
                    R.string.home_no_family_description),
                textoCrear = textos.texto(R.string.home_family_create),
                textoUnirse = textos.texto(R.string.home_family_join),
                alCrearFamilia = {
                    alLimpiarErrorFamilia()
                    mostrarCrearFamilia = true },
                alUnirseFamilia = {
                    alLimpiarErrorFamilia()
                    mostrarUnirseFamilia = true }
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Icon(imageVector = Icons.Filled.Groups,
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(text = textos.texto(R.string.home_family_active),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(text = familiaActiva.nombre,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        val rol =
                            if (membresiaActiva?.rol ==
                                MiembroFamilia.ROL_ADMINISTRADOR) {
                                textos.texto(R.string.home_role_admin)
                            } else {
                                textos.texto(R.string.home_role_member) }
                        Text(text = rol,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            PanelMiembrosFamilia(
                miembros = miembrosFamilia,
                uidUsuarioActual = uidUsuarioActual,
                uidAdministradorPrincipal = familiaActiva.creadoPor,
                esAdministradorActual =
                    membresiaActiva?.rol == MiembroFamilia.ROL_ADMINISTRADOR,
                titulo = textos.texto(
                    R.string.home_members_title),
                textoRolAdministrador = textos.texto(
                    R.string.home_role_admin),
                textoRolMiembro = textos.texto(R.string.home_role_member),
                textoAdministradorPrincipal = textos.texto(
                    R.string.home_member_owner),
                textoTu = textos.texto(R.string.home_member_you),
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
                alExpulsar = { miembro ->
                    alLimpiarErrorMiembros()
                    uidExpulsarMiembro = miembro.uid })
            Spacer(modifier = Modifier.height(20.dp))
            PanelGestionFamilia(
                esAdministrador = membresiaActiva?.rol ==
                        MiembroFamilia.ROL_ADMINISTRADOR,
                esAdministradorPrincipal =
                    uidUsuarioActual == familiaActiva.creadoPor,
                titulo = textos.texto(
                    R.string.home_family_settings),
                textoEditar = textos.texto(
                    R.string.home_family_edit),
                textoAbandonar = textos.texto(
                    R.string.home_family_leave),
                textoEliminar = textos.texto(
                    R.string.home_family_delete),
                alEditar = { alPrepararEdicionFamilia()
                    alLimpiarErrorFamilia()
                    mostrarEditarFamilia = true },
                alAbandonar = {
                    alLimpiarErrorFamilia()
                    mostrarAbandonarFamilia = true },
                alEliminar = {
                    alLimpiarErrorFamilia()
                    mostrarEliminarFamilia = true })
        }
        Spacer(modifier = Modifier.height(20.dp))
        AccionesFamilia(textoCrear =
            textos.texto(R.string
                .home_family_create_another),
            textoUnirse = textos.texto(
                R.string.home_family_join_another),
            alCrearFamilia = {
                alLimpiarErrorFamilia()
                mostrarCrearFamilia = true },
            alUnirseFamilia = {
                alLimpiarErrorFamilia()
                mostrarUnirseFamilia = true })
        if (mensajeErrorFamilia != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(text = mensajeErrorFamilia,
                    modifier = Modifier.padding(14.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
    DialogoCrearFamilia(
        visible = mostrarCrearFamilia,
        nombre = nombreFamiliaNueva,
        titulo = textos.texto(R.string.home_create_family_title),
        descripcion = textos.texto(
            R.string.home_create_family_description),
        etiqueta = textos.texto(R.string.home_family_name_label),
        textoConfirmar = textos.texto(R.string.home_family_create),
        textoCancelar = textos.texto(R.string.home_family_cancel),
        mensajeError = mensajeErrorFamilia,
        cargando = creandoFamilia,
        alCambiarNombre = alCambiarNombreFamilia,
        alConfirmar = { alCrearFamilia { mostrarCrearFamilia = false } },
        alCancelar = { mostrarCrearFamilia = false
            alLimpiarErrorFamilia()
        }
    )
    DialogoUnirseFamilia(
        visible = mostrarUnirseFamilia,
        codigo = codigoInvitacion,
        titulo = textos.texto(R.string.home_join_family_title),
        descripcion = textos.texto(R.string.home_join_family_description),
        etiqueta = textos.texto(R.string.home_family_code_label),
        textoConfirmar = textos.texto(R.string.home_family_join),
        textoCancelar = textos.texto(R.string.home_family_cancel),
        mensajeError = mensajeErrorFamilia,
        cargando = uniendoFamilia,
        alCambiarCodigo = alCambiarCodigo,
        alConfirmar = { alUnirseFamilia { mostrarUnirseFamilia = false } },
        alCancelar = { mostrarUnirseFamilia = false
            alLimpiarErrorFamilia() })
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
        alConfirmar = { alEditarFamilia { mostrarEditarFamilia = false } },
        alCancelar = { mostrarEditarFamilia = false
            alLimpiarErrorFamilia() })
    DialogoAbandonarFamilia(
        visible = mostrarAbandonarFamilia,
        nombreFamilia = familiaActiva?.nombre.orEmpty(),
        titulo = textos.texto(R.string.home_leave_family_title),
        descripcion = textos.texto(R.string.home_leave_family_description),
        textoConfirmar = textos.texto(R.string.home_leave_family_confirm),
        textoCancelar = textos.texto(R.string.home_family_cancel),
        mensajeError = mensajeErrorFamilia,
        cargando = abandonandoFamilia,
        alConfirmar = { alAbandonarFamilia { mostrarAbandonarFamilia = false } },
        alCancelar = { mostrarAbandonarFamilia = false
            alLimpiarErrorFamilia() })
    DialogoQuitarAdministrador(
        visible = miembroQuitarAdministrador != null,
        nombreMiembro = nombreVisible(miembroQuitarAdministrador),
        titulo = textos.texto(R.string.home_remove_admin_title),
        descripcion = textos.texto(R.string.home_remove_admin_description),
        textoConfirmar = textos.texto(R.string.home_remove_admin_confirm),
        textoCancelar = textos.texto(R.string.home_family_cancel),
        mensajeError = mensajeErrorMiembros,
        cargando = miembroQuitarAdministrador != null &&
                actualizandoRolUid == miembroQuitarAdministrador.uid,
        alConfirmar = { miembroQuitarAdministrador?.let { miembro ->
                alQuitarAdministrador(miembro)
                uidQuitarAdministrador = null } },
        alCancelar = { uidQuitarAdministrador = null
            alLimpiarErrorMiembros() })
    DialogoExpulsarMiembro(
        visible = miembroExpulsar != null,
        nombreMiembro = nombreVisible(miembroExpulsar),
        titulo = textos.texto(R.string.home_remove_member_title),
        descripcion = textos.texto(R.string.home_remove_member_description),
        textoConfirmar = textos.texto(R.string.home_remove_member_confirm),
        textoCancelar = textos.texto(R.string.home_family_cancel),
        mensajeError = mensajeErrorMiembros,
        cargando = miembroExpulsar != null &&
                expulsandoMiembroUid == miembroExpulsar.uid,
        alConfirmar = {
            miembroExpulsar?.let { miembro ->
                alExpulsarMiembro(miembro) { uidExpulsarMiembro = null } } },
        alCancelar = { uidExpulsarMiembro = null
            alLimpiarErrorMiembros() })
    DialogoEliminarFamilia(
        visible = mostrarEliminarFamilia,
        nombreFamilia = familiaActiva?.nombre.orEmpty(),
        titulo = textos.texto(R.string.home_delete_family_title),
        descripcion = textos.texto(R.string.home_delete_family_description),
        textoConfirmar = textos.texto(R.string.home_delete_family_confirm),
        textoCancelar = textos.texto(R.string.home_family_cancel),
        mensajeError = mensajeErrorFamilia,
        cargando = eliminandoFamilia,
        alConfirmar = { alEliminarFamilia { mostrarEliminarFamilia = false } },
        alCancelar = { mostrarEliminarFamilia = false
            alLimpiarErrorFamilia() })
}

private fun nombreVisible(
    miembro: MiembroFamiliaDetalle?
): String {
    if (miembro == null) return ""
    return miembro.nombre.ifBlank { miembro.correo }
}
