package com.ulpgc.proyectopamn.adapters.primary.ui.auth

import androidx.annotation.StringRes
import com.ulpgc.proyectopamn.domain.model.UserProfile

/** Todo lo que la pantalla de acceso necesita para pintarse. */
data class AuthUiState(
    val isRegisterMode: Boolean = false,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    @StringRes val errorMessage: Int? = null,
    val loggedUser: UserProfile? = null,
)
