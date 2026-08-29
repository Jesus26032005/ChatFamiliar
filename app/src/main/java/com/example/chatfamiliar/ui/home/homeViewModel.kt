package com.example.chatfamiliar.ui.home

import androidx.lifecycle.ViewModel
import com.example.chatfamiliar.data.auth.AuthRepository

class HomeViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    fun cerrarSesion() { authRepository.cerrarSesion() }
}