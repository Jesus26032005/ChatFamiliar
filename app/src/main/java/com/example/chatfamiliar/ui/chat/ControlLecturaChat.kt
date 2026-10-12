package com.example.chatfamiliar.ui.chat

import com.example.chatfamiliar.model.LecturaConversacion
import com.example.chatfamiliar.model.Mensaje
import com.google.firebase.Timestamp

/**
 * Decide hasta qué mensaje puede avanzar el marcador de lectura.
 * Solo avanza sobre mensajes confirmados por el servidor y se detiene en el
 * primer mensaje recibido que el usuario todavía no ha visto en pantalla.
 */
class ControlLecturaChat {

    private val mensajesVistos = mutableMapOf<String, Timestamp>()

    /** Anota los mensajes que el usuario ya vio en pantalla. */
    fun registrarMensajesVisibles(idsVisibles: Set<String>,
        mensajesCargados: List<Mensaje>) { mensajesCargados.forEach { mensaje ->
            val fecha = mensaje.fechaEnvio

            if (mensaje.id.isNotBlank() &&
                mensaje.id in idsVisibles &&
                fecha != null) { mensajesVistos[mensaje.id] = fecha } } }

    /** Devuelve el mensaje más lejano que se puede marcar como leído, o null si no hay avance. */
    fun buscarSiguienteLectura(uidActual: String,
        lecturaActual: LecturaConversacion,
        mensajesCargados: List<Mensaje>,
        historialCompleto: Boolean): Mensaje? { if (uidActual.isBlank() ||
            lecturaActual.uid != uidActual ||
            mensajesCargados.isEmpty()) { return null }

        val cursorId = lecturaActual.ultimoMensajeLeidoId
        val cursorFecha = lecturaActual.fechaUltimoMensajeLeido

        val tieneId = cursorId.isNotBlank()
        val tieneFecha = cursorFecha != null

        if (tieneId != tieneFecha) { return null }

        // Los mensajes sin fecha confirmada no pueden hacer
        // avanzar el marcador.
        val confirmados = mensajesCargados.takeWhile { it.fechaEnvio != null }

        if (confirmados.isEmpty()) { return null }

        // No aceptamos una lista con mensajes confirmados
        // colocados después de otros sin fecha.
        if (mensajesCargados
                .drop(confirmados.size)
                .any { it.fechaEnvio != null }) { return null }

        if (confirmados.any { it.id.isBlank() || it.remitenteId.isBlank() } ||
            confirmados.map { it.id }.distinct().size !=
            confirmados.size) { return null }

        // El orden debe coincidir con Firestore:
        // primero fecha de envío y después identificador.
        val ordenCorrecto = confirmados.zipWithNext().all { (anterior, siguiente) ->
            comparar(anterior.fechaEnvio!!,
                anterior.id,
                siguiente.fechaEnvio!!,
                siguiente.id) < 0 }

        if (!ordenCorrecto) { return null }

        val cursorPresente = if (cursorFecha != null) { confirmados.any {
                it.id == cursorId && it.fechaEnvio == cursorFecha } } else { false }

        // Necesitamos haber llegado al comienzo del historial
        // o tener dentro del tramo cargado el marcador anterior.
        if (!historialCompleto && !cursorPresente) { return null }

        var candidato: Mensaje? = null

        for (mensaje in confirmados) { val fecha = mensaje.fechaEnvio!!

            if (cursorFecha != null &&
                comparar(fecha,
                    mensaje.id,
                    cursorFecha,
                    cursorId) <= 0) { continue }

            val esPropio = mensaje.remitenteId == uidActual
            val fueVisto = mensajesVistos[mensaje.id] == fecha

            // Un mensaje recibido que todavía no se ha visto
            // impide avanzar a los siguientes.
            if (!esPropio && !fueVisto) { break }

            candidato = mensaje }

        return candidato }

    /** Olvida los mensajes vistos que ya quedan cubiertos por la lectura guardada. */
    fun descartarHastaLecturaConfirmada(lectura: LecturaConversacion) {
        val fecha = lectura.fechaUltimoMensajeLeido ?: return
        val id = lectura.ultimoMensajeLeidoId

        if (id.isBlank()) return

        val iterador = mensajesVistos.entries.iterator()

        while (iterador.hasNext()) { val visto = iterador.next()

            if (comparar(visto.value,
                    visto.key,
                    fecha,
                    id) <= 0) { iterador.remove() } } }

    /** Olvida todos los mensajes vistos. */
    fun reiniciar() { mensajesVistos.clear() }

    /** Compara dos mensajes por fecha y, si empatan, por id (mismo orden que Firestore). */
    private fun comparar(fechaPrimera: Timestamp,
        idPrimero: String,
        fechaSegunda: Timestamp,
        idSegundo: String): Int { val comparacionFecha = fechaPrimera.compareTo(fechaSegunda)

        return if (comparacionFecha != 0) { comparacionFecha } else { idPrimero.compareTo(idSegundo) } } }