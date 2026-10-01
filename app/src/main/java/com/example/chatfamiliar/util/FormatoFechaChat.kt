package com.example.chatfamiliar.util

import android.text.format.DateFormat
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

fun formatearFechaConversacion(
    fecha: Timestamp?, idiomaEfectivo: String,
    formato24Horas: Boolean, textoAyer: String,
    ahoraMillis: Long = System.currentTimeMillis(),
    zonaHoraria: TimeZone = TimeZone.getDefault()): String {
    if (fecha == null) { return "" }

    val locale = Locale.forLanguageTag(idiomaEfectivo)
        .takeIf { it.language.isNotBlank() }
        ?: Locale.getDefault()
    val fechaMensaje = fecha.toDate()
    val calendarioMensaje = Calendar.getInstance(
        zonaHoraria,
        locale).apply { time = fechaMensaje }
    val calendarioActual = Calendar.getInstance(
        zonaHoraria,
        locale).apply { timeInMillis = ahoraMillis }
    val calendarioAyer = (calendarioActual.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -1) }
    val patronBase = when {
        esMismoDia(calendarioMensaje, calendarioActual) -> {
            if (formato24Horas) { "Hm"
            } else { "hm" } }
        esMismoDia(calendarioMensaje, calendarioAyer) -> {
            return textoAyer }
        esMismoAnio(calendarioMensaje, calendarioActual) -> {
            "MMMd" }
        else -> { "yMMMd" }
    }
    val patronLocalizado = DateFormat.getBestDateTimePattern(
        locale, patronBase)
    return SimpleDateFormat(patronLocalizado, locale).apply {
        timeZone = zonaHoraria }.format(fechaMensaje)
}

private fun esMismoDia(primero: Calendar, segundo: Calendar): Boolean {
    return esMismoAnio(primero, segundo) && primero.get(Calendar.DAY_OF_YEAR) ==
            segundo.get(Calendar.DAY_OF_YEAR)
}
private fun esMismoAnio(primero: Calendar, segundo: Calendar): Boolean {
    return primero.get(Calendar.ERA) == segundo.get(Calendar.ERA) &&
            primero.get(Calendar.YEAR) == segundo.get(Calendar.YEAR)
}