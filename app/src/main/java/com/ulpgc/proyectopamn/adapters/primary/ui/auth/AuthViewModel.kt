package com.ulpgc.proyectopamn.adapters.primary.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ulpgc.proyectopamn.R
import com.ulpgc.proyectopamn.domain.model.AuthError
import com.ulpgc.proyectopamn.domain.ports.input.LoginInputPort
import com.ulpgc.proyectopamn.domain.ports.input.RegisterInputPort
import kotlinx.coroutines.launch

class AuthViewModel(
    private val login: LoginInputPort,
    private val register: RegisterInputPort,
) : ViewModel() {

    var uiState by mutableStateOf(AuthUiState())
        private set

    // Al escribir se borra el error: el usuario ya está corrigiéndolo.
    fun onNameChange(value: String) {
        uiState = uiState.copy(name = value, errorMessage = null)
    }

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, errorMessage = null)
    }

    fun onToggleMode() {
        uiState = uiState.copy(isRegisterMode = !uiState.isRegisterMode, errorMessage = null)
    }

    fun onSubmit() {
        if (uiState.isLoading) return
        val state = uiState
        uiState = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = if (state.isRegisterMode) {
                register.execute(state.name, state.email, state.password)
            } else {
                login.execute(state.email, state.password)
            }
            uiState = result.fold(
                // Formulario limpio: si luego cierra sesión, vuelve a un login vacío.
                onSuccess = { user -> AuthUiState(loggedUser = user) },
                onFailure = { error -> uiState.copy(isLoading = false, errorMessage = error.toMessage()) },
            )
        }
    }

    /** La pantalla ya ha avisado del login: se borra para no avisar dos veces. */
    fun onLoginHandled() {
        uiState = uiState.copy(loggedUser = null)
    }

    // Traducir errores del dominio a textos es cosa de la UI, no del dominio.
    private fun Throwable.toMessage(): Int = when (this) {
        AuthError.EmptyName -> R.string.auth_error_empty_name
        AuthError.InvalidEmail -> R.string.auth_error_invalid_email
        AuthError.WeakPassword -> R.string.auth_error_weak_password
        AuthError.EmailAlreadyInUse -> R.string.auth_error_email_in_use
        AuthError.InvalidCredentials -> R.string.auth_error_invalid_credentials
        else -> R.string.auth_error_unknown
    }
}
