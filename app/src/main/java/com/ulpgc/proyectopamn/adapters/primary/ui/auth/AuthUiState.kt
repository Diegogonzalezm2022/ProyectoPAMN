package com.ulpgc.proyectopamn.adapters.primary.ui.auth

import androidx.annotation.StringRes
import com.ulpgc.proyectopamn.domain.model.User

/** Todo lo que la pantalla de acceso necesita para pintarse. */
data class AuthUiState(
    val isRegisterMode: Boolean = false,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    @StringRes val errorMessage: Int? = null,
    val loggedUser: User? = null,
)
