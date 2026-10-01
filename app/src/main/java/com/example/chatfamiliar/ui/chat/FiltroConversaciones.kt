package com.example.chatfamiliar.ui.chat

import androidx.annotation.StringRes
import com.example.chatfamiliar.R

enum class FiltroConversaciones(
    @get:StringRes val textoRecurso: Int
) {
    TODOS(R.string.chat_filter_all),
    NO_LEIDOS(R.string.chat_filter_unread),
    FAMILIAS(R.string.chat_filter_families),
    PRIVADOS(R.string.chat_filter_private)
}