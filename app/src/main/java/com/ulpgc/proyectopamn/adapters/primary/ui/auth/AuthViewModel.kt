package com.ulpgc.proyectopamn.adapters.primary.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ulpgc.proyectopamn.R
import com.ulpgc.proyectopamn.domain.model.AuthResult
import com.ulpgc.proyectopamn.domain.model.UserProfile
import com.ulpgc.proyectopamn.domain.ports.input.LoginInputPort
import com.ulpgc.proyectopamn.domain.ports.input.RegisterInputPort
import kotlinx.coroutines.launch

class AuthViewModel(
    private val login: LoginInputPort,
    private val register: RegisterInputPort,
) : ViewModel() {

    var uiState by mutableStateOf(AuthUiState())
        private set

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
                val newUser = UserProfile(username = state.name, email = state.email)
                register.execute(newUser, state.password)
            } else {
                login.execute(state.email, state.password)
            }

            uiState = when (result) {
                is AuthResult.Success -> uiState.copy(isLoading = false, password = "", loggedUser = result.data)
                is AuthResult.Error -> uiState.copy(isLoading = false, errorMessage = R.string.auth_error_invalid_credentials)
            }
        }
    }
}
